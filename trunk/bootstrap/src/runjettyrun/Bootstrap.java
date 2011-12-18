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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
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
import org.mortbay.xml.XmlConfiguration;

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


		if(configs.getJettyXML() != null && !"".equals(configs.getJettyXML().trim())){
			System.err.println("Loading Jetty.xml:"+configs.getJettyXML());
			try{
				XmlConfiguration configuration = new XmlConfiguration(
						new File(configs.getJettyXML()).toURI().toURL());
				configuration.configure(server);
			}catch(Exception ex){
				System.err.println("Exception happened when loading Jetty.xml:");
				ex.printStackTrace();
			}
		}

		// configureScanner
		if (configs.getEnablescanner())
			initScanner(web, configs);

		initEclipseListener(configs);
		initCommandListener(configs);

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
		return;
	}

	/*
	 * It's too hard to build a built-in supprot with the red button,
	 * actually we get very close with this feature,
	 * I could event handle this well when user is not in debug mode.
	 *
	 * But when we are running under debug mode ,
	 * the DebugTarget will terminate JVM directly even before it terminate process.
	 *
	 *  I got no chance to prevent the default behavior ,
	 *  (Maybe it did , but I am too tired to look into the details right now ,
	 *   it's not fun to work with JDT core. lol)
	 *
	 *  So I think the simply workaround is ,
	 *  when user want to test with graceful shutdown ,
	 *  please type the shutdown command in console by himself .
	 *
	 *  Isn't it really a simple solution ? ;)
	 *
	 *  Also we create a way to let user restart the server by command ,
	 *  so we are going to supprot following command after 1.3
	 *
	 *  "restart" or "r"  : restart the server like what scanner do , it's case-insensitive.
	 *  "quit" or "q" or "exit" : shutdown the server.
	 *
	 *  Actually I don't think this is a common issue , it should be some "debugging" case,
	 *  and will not be a common case, if you have good use case on this , we could check this later.
	 *
	 *  If you think it's not good enough ,we are also looking for
	 *  good suggestion to implement this as possible as we could.
	 *
	 *  Maybe we will provide a Jetty view to make it simpler in future,
	 *  that depends how many user need this feature.
	 *
	 */
	private static void initCommandListener(final Configs configs){
		//init eclipse hook
		Thread commandListener = new Thread(){
			public void run() {
				try {
					BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
					while(true){
						String inputStr = input.readLine();
						if(inputStr != null){
							inputStr = inputStr.trim();

							if("exit".equalsIgnoreCase(inputStr) ||
									"quit".equalsIgnoreCase(inputStr) ||
									"q".equalsIgnoreCase(inputStr)
							){
								System.err.println("shutting down");
								shutdownServer();
							}else if("r".equalsIgnoreCase(inputStr)
									|| "restart".equalsIgnoreCase(inputStr)
							){
								try{
									System.err.println("Stopping webapp ...");
									web.stop();

									if (configs.getWebAppClassPath() != null) {
										ProjectClassLoader loader = new ProjectClassLoader(web,
												configs.getWebAppClassPath(),configs.getExcludedclasspath(), false);
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

						}
					}

				} catch (UnknownHostException e) {
					System.err.println("lost connection with Eclipse , shutting down.");
					shutdownServer();
				} catch (IOException e) {
					System.err.println("lost connection with Eclipse , shutting down.");
					shutdownServer();
				}
			};
		};
		commandListener.start();

	}
	private static void initEclipseListener(final Configs configs){
		//init eclipse hook
		if(configs.getEclipseListenerPort() != -1 ){
			Thread eclipseListener = new Thread(){
				public void run() {
					try {
						while(true){
							Thread.sleep(5000L);
							Socket sock = new Socket("127.0.0.1", configs.getEclipseListenerPort());
							byte[] response = new byte[4];
							sock.getInputStream().read(response);

							//@see runjettyrun.Plugin#enableListenter
							//TODO applied on Jetty7 and Jetty8
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
			System.exit(-1);
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
					configs.getWebAppClassPath(),configs.getExcludedclasspath());
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
			final Configs config ) {

		int scanIntervalSeconds = config.getScanIntervalSeconds();

		final ArrayList<File> scanList = new ArrayList<File>();

		System.err.println("init scanning folders...");
		if (config.getScanlist() != null) {
			String[] items = config.getScanlist().split(File.pathSeparator);
			for (String item:items) {
				File f = new File(item);
				scanList.add(f);
				System.err.println("add to scan list:"+item);
			}
		}

		if(config.getScanWEBINF()){
			Resource r;
			try {
				r = web.getResource("/WEB-INF");
				if(r.exists()){
					if(r.getFile().isDirectory()){
						scanList.add(r.getFile());
						System.err.println("add to scan list :" + r.getFile().getAbsolutePath());
					}
				}
			} catch (MalformedURLException e) {
			} catch (IOException e) {
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
					if(changes.size() > 0 ){
						System.err.println("Stopping webapp ...(File changed:"+changes.get(0)+")");
					}else{
						System.err.println("Stopping webapp ...");
					}
					web.stop();

					if (config.getWebAppClassPath() != null) {
						ProjectClassLoader loader = new ProjectClassLoader(web,
								config.getWebAppClassPath(), config.getExcludedclasspath(), false);
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
