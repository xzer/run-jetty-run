/*
 * $Id$
 * $HeadURL$
 *
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package runjettyrun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.management.MBeanServer;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.management.MBeanContainer;
import org.mortbay.util.Scanner;

/**
 * Started up by the plugin's runner. Starts Jetty.
 *
 * @author hillenius, jsynge, jumperchen
 */
public class Bootstrap {

	private static Server server;

	static WebAppContext web;

	/**
	 * Main function, starts the jetty server.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		boolean loggerparam = false;
		if (loggerparam) {
			String[] propkeys = new String[] { "rjrcontext", "rjrwebapp",
					"rjrport", "rjrsslport", "rjrkeystore", "rjrpassword",
					"rjrclasspath", "rjrkeypassword", "rjrscanintervalseconds",
					"rjrenablescanner", "rjrenablessl", "rjrenbaleJNDI" };
			for (String key : propkeys) {
				System.err.println("-D" + key + "=" + System.getProperty(key));
			}
		}
		String context = System.getProperty("rjrcontext");
		String webAppDir = System.getProperty("rjrwebapp");
		Integer port = Integer.getInteger("rjrport");
		Integer sslport = Integer.getInteger("rjrsslport");
		String keystore = System.getProperty("rjrkeystore");
		String password = System.getProperty("rjrpassword");
		final String webAppClassPath = resovleWebappClasspath();
		String keyPassword = System.getProperty("rjrkeypassword");
		Integer scanIntervalSeconds = Integer
				.getInteger("rjrscanintervalseconds");
		Boolean enablescanner = Boolean.getBoolean("rjrenablescanner");
		Boolean parentLoaderPriority = getBoolean("rjrparentloaderpriority",
				true);

		Boolean enablessl = Boolean.getBoolean("rjrenablessl");
		Boolean needClientAuth = Boolean.getBoolean("rjrneedclientauth");
		Boolean enableJNDI = Boolean.getBoolean("rjrenbaleJNDI");

		ArrayList<String> configuration = new ArrayList<String>();
		if (enableJNDI) {
			configuration.add("org.mortbay.jetty.webapp.WebInfConfiguration");
			configuration.add("org.mortbay.jetty.plus.webapp.EnvConfiguration");
			configuration.add("org.mortbay.jetty.plus.webapp.Configuration");
			configuration
					.add("org.mortbay.jetty.webapp.JettyWebXmlConfiguration");
			configuration.add("org.mortbay.jetty.webapp.TagLibConfiguration");
		}
		String rjrConfiguration = System.getProperty("rjrconfigurationclasses",
				"");
		if (!"".equals(rjrConfiguration)) {
			String[] configs = rjrConfiguration.split(";");
			for (String conf : configs) {
				configuration.add(conf);
			}
		}

		if (context == null) {
			throw new IllegalStateException(
					"you need to provide argument -Drjrcontext");
		}
		if (webAppDir == null) {
			throw new IllegalStateException(
					"you need to provide argument -Drjrwebapp");
		}
		if (port == null && sslport == null) {
			throw new IllegalStateException(
					"you need to provide argument -Drjrport and/or -Drjrsslport");
		}

		server = new Server();

		if (port != null) {
			if (!available(port)) {
				throw new IllegalStateException("port :" + port
						+ " already in use!");
			}
			SelectChannelConnector connector = new SelectChannelConnector();
			connector.setPort(port);

			if (enablessl && sslport != null) {
				connector.setConfidentialPort(sslport);
			}

			server.addConnector(connector);
		}

		if (enablessl && sslport != null) {
			if (!available(sslport)) {
				throw new IllegalStateException("SSL port :" + sslport
						+ " already in use!");
			}
			if (keystore == null) {
				throw new IllegalStateException(
						"you need to provide argument -Drjrkeystore with -Drjrsslport");
			}
			if (password == null) {
				throw new IllegalStateException(
						"you need to provide argument -Drjrpassword with -Drjrsslport");
			}
			if (keyPassword == null) {
				throw new IllegalStateException(
						"you need to provide argument -Drjrkeypassword with -Drjrsslport");
			}

			SslSocketConnector sslConnector = new SslSocketConnector();
			sslConnector.setKeystore(keystore);
			sslConnector.setPassword(password);
			sslConnector.setKeyPassword(keyPassword);

			if (needClientAuth) {
				System.err.println("Enable NeedClientAuth.");
				sslConnector.setNeedClientAuth(needClientAuth);
			}
			sslConnector.setMaxIdleTime(30000);
			sslConnector.setPort(sslport);

			server.addConnector(sslConnector);
		}

		web = new WebAppContext();

		if (parentLoaderPriority) {
			web.setParentLoaderPriority(true);
			System.err.println("ParentLoaderPriority enabled");
		}

		web.setContextPath(context);
		web.setWar(webAppDir);

		/**
		 * open a way to set the configuration classes
		 */
		if (configuration.size() != 0) {
			web.setConfigurationClasses(configuration.toArray(new String[0]));

			for (String conf : configuration) {
				System.err.println("Enable config class:" + conf);
			}
		}
		// Fix issue 7, File locking on windows/Disable Jetty's locking of
		// static files
		// http://code.google.com/p/run-jetty-run/issues/detail?id=7
		// by disabling the use of the file mapped buffers. The default Jetty
		// behavior is
		// intended to provide a performance boost, but run-jetty-run is focused
		// on
		// development (especially debugging) of web apps, not high-performance
		// production
		// serving of static content. Therefore, I'm not worried about the
		// performance
		// degradation of this change. My only concern is that there might be a
		// need to
		// test this feature that I'm disabling.
		web.setInitParams(Collections.singletonMap(
				"org.mortbay.jetty.servlet.Default.useFileMappedBuffer",
				"false"));

		if (webAppClassPath != null) {
			ProjectClassLoader loader = new ProjectClassLoader(web,
					webAppClassPath);
			web.setClassLoader(loader);
		}

		server.addHandler(web);

		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
		server.getContainer().addEventListener(mBeanContainer);
		mBeanContainer.start();

		// configureScanner
		if (enablescanner) {
			final ArrayList<File> scanList = new ArrayList<File>();
			if (webAppClassPath != null) {
				for (URL url : ((ProjectClassLoader) web.getClassLoader())
						.getURLs()) {
					File f = new File(url.getFile());
					if (f.isDirectory()) {
						scanList.add(f);
					}
				}
			}

			// startScanner
			Scanner scanner = new Scanner();
			scanner.setReportExistingFilesOnStartup(false);
			scanner.setScanInterval(scanIntervalSeconds);
			scanner.setScanDirs(scanList);
			scanner.addListener(new Scanner.BulkListener() {

				public void filesChanged(List changes) {
					try {
						// boolean reconfigure = changes.contains(getProject()
						// .getFile().getCanonicalPath());
						System.err.println("Stopping webapp ...");

						web.stop();

						if (webAppClassPath != null) {
							ProjectClassLoader loader = new ProjectClassLoader(
									web, webAppClassPath, false);
							web.setClassLoader(loader);
						}
						System.err.println("Restarting webapp ...");
						web.start();
						System.err.println("Restart completed.");
					} catch (Exception e) {
						System.err
								.println("Error reconfiguring/restarting webapp after change in watched files");
						e.printStackTrace();
					}
				}
			});
			System.err.println("Starting scanner at interval of "
					+ scanIntervalSeconds + " seconds.");
			scanner.start();
		}

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
		return;
	}

	private static Boolean getBoolean(String propertiesKey, boolean def) {
		String val = System.getProperty(propertiesKey);

		Boolean ret = def;
		if (val != null) {
			try {
				ret = Boolean.parseBoolean(val);
			} catch (Exception e) {

			}

		}
		return ret;

	}

	private static String resovleWebappClasspath() {
		String classpath = System.getProperty("rjrclasspath");

		if (classpath != null && classpath.startsWith("file://")) {
			try {
				String filePath = classpath.substring(7);

				BufferedReader br = new BufferedReader(new InputStreamReader(
						new FileInputStream(filePath), "UTF-8"));
				StringBuffer sb = new StringBuffer();
				String str = br.readLine();
				while (str != null) {
					sb.append(str);
					str = br.readLine();
				}
				return sb.toString();

			} catch (IOException e) {
				System.err.println("read classpath failed!");
				throw new RuntimeException(" read classpath failed ", e);
			}
		}

		return classpath;
	}

	private static boolean available(int port) {
		if (port <= 0) {
			throw new IllegalArgumentException("Invalid start port: " + port);
		}

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/* should not be thrown */
				}
			}
		}

		return false;
	}
}
