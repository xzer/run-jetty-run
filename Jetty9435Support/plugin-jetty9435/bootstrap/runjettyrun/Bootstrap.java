package runjettyrun;



import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.Scanner;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;

import runjettyrun.scanner.RJRFileChangeListener;

/**
 * Started up by the plugin's runner. Starts Jetty.
 *
 * @author hillenius, jsynge, jumperchen , TonyQ
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
		System.err.println("Running Jetty 9.4.35.v20201120");

		Configs configs = new Configs();


		configs.validation();

		server = new Server();

		// Setup JMX
		MBeanContainer mbContainer=new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
		server.addEventListener(mbContainer);
		server.addBean(mbContainer);

		System.err.println("added mbean");
		
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

	private static void initWebappContext(Server server,Configs configs)
		throws IOException, URISyntaxException {
		web = new WebAppContext();

		if (configs.getParentLoaderPriority()) {
			System.err.println("ParentLoaderPriority enabled");
			web.setParentLoaderPriority(true);
		}

		web.setContextPath(configs.getContext());

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
		web.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer",
				"false");

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
			resources.add(new VirtualResource(webapp,"/"+key,map.get(key)));
		}

		ResourceCollection webAppDirResources =
				new ResourceCollection(resources.toArray(new Resource[0]));
        web.setBaseResource(webAppDirResources);

        server.setHandler(web);
	}

	private static void initConnnector(Server server, Configs configObj) {
		ServerConnector connector = new ServerConnector(server);

		//Don't set any host , or the port detection will failed. -_-#
		//connector.setHost("127.0.0.1");
		connector.setPort(configObj.getPort());

		server.addConnector(connector);

		if (configObj.getEnablessl() && configObj.getSslport() != null){
			initSSL(server, configObj.getSslport(), configObj.getKeystore(),
					configObj.getPassword(), configObj.getKeyPassword(),
					configObj.getNeedClientAuth());
		}

	}

	private static void initEclipseListener(final Configs configs){
		//init eclipse hook
		if(configs.getEclipseListenerPort() != -1 ){
			Thread eclipseListener = new Thread(){
				public void run() {
					try {
						while(true){
							Thread.sleep(5000L);
							Socket sock = null;
							try{
								sock = new Socket("127.0.0.1", configs.getEclipseListenerPort());
								byte[] response = new byte[4];
								sock.getInputStream().read(response);
	
								//@see runjettyrun.Plugin#enableListenter
								if(response[0] ==1 && response[1] ==2){
									//it's ok!
								}else{
									//Eclipse crashs
									shutdownServer();
								}
							}finally{
								if(sock != null){
									try{
										sock.close();
									}catch(Exception ex){
										ex.printStackTrace(System.err);
									}
								}
							}
						}//end while
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


		SslContextFactory sslcontextfactory = new SslContextFactory();
		sslcontextfactory.setKeyStorePath(keystore);
		sslcontextfactory.setKeyStorePassword(password);

		sslcontextfactory.setKeyManagerPassword(keyPassword);

		if (needClientAuth) {
			System.err.println("Enable NeedClientAuth.");
			sslcontextfactory.setNeedClientAuth(needClientAuth);
		}

		
		ServerConnector sslConnector = new ServerConnector(server,sslcontextfactory);
        sslConnector.setPort(sslport);
        server.addConnector(sslConnector);
        try{
        	sslConnector.open();
        }catch(Exception ex){
        	ex.printStackTrace();
        }
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

		// startScanner
		Scanner scanner = new Scanner();
		scanner.setScanInterval(scanIntervalSeconds);
		scanner.setScanDirs(scanList);
		scanner.setRecursive(true);
		scanner.setReportExistingFilesOnStartup(false);
		scanner.addListener(new RJRFileChangeListener(web,config));
		System.err.println("Starting scanner at interval of "
				+ scanIntervalSeconds + " seconds.");
		try {
			scanner.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
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
												configs.getWebAppClassPath(),
												configs.getExcludedclasspath(), false);
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

}
