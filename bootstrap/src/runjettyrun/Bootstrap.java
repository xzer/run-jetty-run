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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.management.MBeanContainer;
import org.mortbay.resource.FileResource;
import org.mortbay.resource.Resource;
import org.mortbay.resource.ResourceCollection;
import org.mortbay.util.Scanner;

/**
 * Started up by the plugin's runner. Starts Jetty.
 *
 * @author hillenius, jsynge, jumperchen
 */
public class Bootstrap {

	private static Server server;

	private static WebAppContext web;

	/**
	 * Main function, starts the jetty server.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {

		logArgus(false);
		Bootstrap.Configs configs = new Bootstrap.Configs();


		configs.validation();

		server = new Server();

		initConnnector(server, configs);

		initWebappContext(server,configs);

		initMBeanServer(server);

		// configureScanner
		if (configs.getEnablescanner())
			initScanner(web, configs.getWebAppClassPath(),
					configs.getScanIntervalSeconds());

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
		return;
	}

	private static void initWebappContext(Server server,Configs configs)
		throws IOException, URISyntaxException {
		web = new WebAppContext();

		if (configs.getParentLoaderPriority()) {
			System.err.println("ParentLoaderPriority enabled");
			web.setParentLoaderPriority(true);
		}

		web.setContextPath(configs.getContext());
		web.setWar(configs.getWebAppDir());


		//TODO add multiple root selection support
		//		 ResourceCollection webAppDirResources = new ResourceCollection();
		//
		//	     webAppDirResources.setResourcesAsCSV(webAppDir);
		//	     web.setBaseResource(webAppDirResources);

		/**
		 * open a way to set the configuration classes
		 */
		List<String> configurationClasses = configs.getConfigurationClassesList();
		if (configurationClasses.size() != 0) {
			web.setConfigurationClasses(configurationClasses.toArray(new String[0]));

			for (String conf : configurationClasses)
				System.err.println("Enable config class:" + conf);

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

		if (configs.getWebAppClassPath() != null) {
			ProjectClassLoader loader = new ProjectClassLoader(web,
					configs.getWebAppClassPath());
			web.setClassLoader(loader);
		}


		List<Resource> resources = new ArrayList<Resource>();

		URL urlWebapp = new File(configs.getWebAppDir()).toURI().toURL();
		Resource webapp = new FileResource(urlWebapp);
		resources.add(webapp);

		Map<String,String> map = configs.getResourceMap();
		for(String key : map.keySet()){
/*
 * 			URL url = new File(map.get(key)).toURI().toURL();
			Resource resource;
			try {
				resource = new FileResource(url);
				final ResourceHandler handler = new ResourceHandler();
				handler.setBaseResource(resource);
				handler.setServer(server);
				handler.setContextPath(key);
				web.addHandler(handler);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

 */
			resources.add(new VirtualResource(webapp,"/"+key,map.get(key)));
//			final WebAppContext js = new WebAppContext();
//			js.setContextPath(key);
//			js.setResourceBase(map.get(key)); // or whatever the correct path is in your case
//			js.setParentLoaderPriority(true);
//			server.addHandler(js);
		}

		ResourceCollection webAppDirResources = new ResourceCollection();
		webAppDirResources.setResources(resources.toArray(new Resource[0]));
        web.setBaseResource(webAppDirResources);

		server.addHandler(web);
	}

	private static void initConnnector(Server server, Configs configObj) {
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(configObj.getPort());

		if (configObj.getEnablessl() && configObj.getSslport() != null)
			connector.setConfidentialPort(configObj.getSslport());

		server.addConnector(connector);

		if (configObj.getEnablessl() && configObj.getSslport() != null)
			initSSL(server, configObj.getSslport(), configObj.getKeystore(),
					configObj.getPassword(), configObj.getKeyPassword(),
					configObj.getNeedClientAuth());

	}

	private static void logArgus(boolean loggerparam) {

		if (loggerparam) {
			String[] propkeys = new String[] { "rjrcontext", "rjrwebapp",
					"rjrport", "rjrsslport", "rjrkeystore", "rjrpassword",
					"rjrclasspath", "rjrkeypassword", "rjrscanintervalseconds",
					"rjrenablescanner", "rjrenablessl", "rjrenbaleJNDI" };
			for (String key : propkeys) {
				System.err.println("-D" + key + "=" + System.getProperty(key));
			}
		}
	}


	private static void initSSL(Server server, int sslport, String keystore,
			String password, String keyPassword, boolean needClientAuth) {
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

	private static void initMBeanServer(Server server) {
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
		server.getContainer().addEventListener(mBeanContainer);
		mBeanContainer.start();
	}

	/**
	 * add source scanner to restart server when source change
	 * @param web
	 * @param webAppClassPath
	 * @param scanIntervalSeconds
	 */
	private static void initScanner(final WebAppContext web,
			final String webAppClassPath, int scanIntervalSeconds) {

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
						ProjectClassLoader loader = new ProjectClassLoader(web,
								webAppClassPath, false);
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

	/**
	 * To get the full list for classpath list,
	 * Note:sometimes the classpath will be very longer,
	 * 		especially when you are working on a Maven project.
	 *
	 * 		Since if classpath too long will cause command line too long issue,
	 * 		we use file to handle it in this case.
	 * @return
	 */
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

	/**
	 * A configuration object to handle the complicated parse job ,
	 * and make thing easier.
	 */
	public static class Configs {
		private String context;
		private String webAppDir;
		private Integer port;
		private Integer sslport;
		private String keystore;
		private String password;
		private String webAppClassPath;
		private String keyPassword;
		private Integer scanIntervalSeconds;
		private Boolean enablescanner;
		private Boolean parentLoaderPriority;

		private Boolean enablessl;
		private Boolean needClientAuth;
		private Boolean enableJNDI;
		private String configurationClasses;
		private String resourceMapping;

		public Configs() {
			context = System.getProperty("rjrcontext");
			webAppDir = System.getProperty("rjrwebapp");
			port = Integer.getInteger("rjrport");
			sslport = Integer.getInteger("rjrsslport");
			keystore = System.getProperty("rjrkeystore");
			password = System.getProperty("rjrpassword");
			webAppClassPath = resovleWebappClasspath();
			keyPassword = System.getProperty("rjrkeypassword");
			scanIntervalSeconds = Integer.getInteger("rjrscanintervalseconds");
			enablescanner = Boolean.getBoolean("rjrenablescanner");
			parentLoaderPriority = getBoolean("rjrparentloaderpriority", true);

			enablessl = Boolean.getBoolean("rjrenablessl");
			needClientAuth = Boolean.getBoolean("rjrneedclientauth");
			enableJNDI = Boolean.getBoolean("rjrenbaleJNDI");
			configurationClasses = System
					.getProperty("rjrconfigurationclasses", "");

			resourceMapping = System
			.getProperty("rjrResourceMapping", "");
		}


		public String getContext() {
			return context;
		}

		public String getWebAppDir() {
			return webAppDir;
		}

		public Integer getPort() {
			return port;
		}

		public Integer getSslport() {
			return sslport;
		}

		public String getKeystore() {
			return keystore;
		}

		public String getPassword() {
			return password;
		}

		public String getWebAppClassPath() {
			return webAppClassPath;
		}

		public String getKeyPassword() {
			return keyPassword;
		}

		public Integer getScanIntervalSeconds() {
			return scanIntervalSeconds;
		}

		public Boolean getEnablescanner() {
			return enablescanner;
		}

		public Boolean getParentLoaderPriority() {
			return parentLoaderPriority;
		}

		public Boolean getEnablessl() {
			return enablessl;
		}

		public Boolean getNeedClientAuth() {
			return needClientAuth;
		}

		public Boolean getEnableJNDI() {
			return enableJNDI;
		}

		public String getConfigurationClasses() {
			return configurationClasses;
		}

		public List<String> getConfigurationClassesList(){
			ArrayList<String> configuration = new ArrayList<String>();
			if (getEnableJNDI()) {
				configuration.add("org.mortbay.jetty.webapp.WebInfConfiguration");
				configuration.add("org.mortbay.jetty.plus.webapp.EnvConfiguration");
				configuration.add("org.mortbay.jetty.plus.webapp.Configuration");
				configuration.add("org.mortbay.jetty.webapp.JettyWebXmlConfiguration");
				configuration.add("org.mortbay.jetty.webapp.TagLibConfiguration");
			}
			if (!"".equals(getConfigurationClasses())) {
				String[] configs = getConfigurationClasses().split(";");
				for (String conf : configs) {
					configuration.add(conf);
				}
			}
			return configuration;
		}

		public void validation() {
			if (getContext() == null) {
				throw new IllegalStateException(
						"you need to provide argument -Drjrcontext");
			}
			if (getWebAppDir() == null) {
				throw new IllegalStateException(
						"you need to provide argument -Drjrwebapp");
			}
			if (getPort() == null && getSslport() == null) {
				throw new IllegalStateException(
						"you need to provide argument -Drjrport and/or -Drjrsslport");
			}
			if (!available(port)) {
				throw new IllegalStateException("port :" + port
						+ " already in use!");
			}
		}

		public String getResourceMapping() {
			return resourceMapping;
		}

		public Map<String,String> getResourceMap(){

			String[] resources = resourceMapping.split(";");

			HashMap<String,String> map = new HashMap<String,String>();

			for(String resource:resources){
				if( resource == null || "".equals(resource.trim())) continue;
				String[] tokens = resource.split("=");
				map.put(tokens[0],tokens[1]);
			}
			return map;
		}
	}
}
