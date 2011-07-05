package runjettyrun;
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

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
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
		System.err.println("Running Jetty 6.1.26");

		final Configs configs = new Configs();

		configs.validation();

		server = new Server();

		initConnnector(server, configs);

		initWebappContext(server,configs);

		// configureScanner
		if (configs.getEnablescanner())
			initScanner(web, configs.getWebAppClassPath(),
					configs.getScanIntervalSeconds());

		server.setStopAtShutdown(true);

		initEclipseListener(configs);

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
		return;
	}

	private static void initEclipseListener(final Configs configs){
		//init eclipse hook
		if(configs.getEclipseListenerPort() != -1 ){
			Thread eclipseListener = new Thread(){
				public void run() {
					try {
						while(true){
							Thread.sleep(10000L);
							Socket sock = new Socket("127.0.0.1", configs.getEclipseListenerPort());
							byte[] response = new byte[4];
							sock.getInputStream().read(response);

							//@see runjettyrun.Plugin#enableListenter
							if(response[0] ==1 && response[1] ==2){
								//it's ok!
							}else{
								//Eclipse crashs
								shutdownServer();
							}

						}

					} catch (UnknownHostException e) {
						System.err.println("lost connection with Eclipse , shutting down.");
						shutdownServer();
					} catch (IOException e) {
						System.err.println("lost connection with Eclipse , shutting down.");
						shutdownServer();
					} catch (InterruptedException e) {
						System.err.println("lost connection with Eclipse , shutting down.");
						shutdownServer();
					}
				};
			};
			eclipseListener.start();
		}

	}

	private static void shutdownServer(){
		try {
			server.stop();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	private static void initWebappContext(Server server,Configs configs)
		throws IOException, URISyntaxException {
		web = new WebAppContext();

		if (configs.getParentLoaderPriority()) {
			System.err.println("ParentLoaderPriority enabled");
			web.setParentLoaderPriority(true);
		}

		web.setContextPath(configs.getContext());
		System.err.println("Context path:"+configs.getContext());
		web.setWar(configs.getWebAppDir());

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

		//Don't set any host , or the port detection will failed. -_-#
		//connector.setHost("127.0.0.1");
		connector.setPort(configObj.getPort());

		if (configObj.getEnablessl() && configObj.getSslport() != null){
			if (!available(configObj.getSslport())) {
				throw new IllegalStateException("SSL port :" + configObj.getSslport()
						+ " already in use!");
			}
			connector.setConfidentialPort(configObj.getSslport());
		}

		server.addConnector(connector);

		if (configObj.getEnablessl() && configObj.getSslport() != null)
			initSSL(server, configObj.getSslport(), configObj.getKeystore(),
					configObj.getPassword(), configObj.getKeyPassword(),
					configObj.getNeedClientAuth());

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

	private static void initSSL(Server server, int sslport, String keystore,
			String password, String keyPassword, boolean needClientAuth) {

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
		scanner.setRecursive(true);
		scanner.addListener(new Scanner.BulkListener() {

			public void filesChanged(@SuppressWarnings("rawtypes") List changes) {
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


}
