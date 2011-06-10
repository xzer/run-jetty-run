package runjettyrun;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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

		logArgus(false);
		Configs configs = new Configs();


		configs.validation();

		server = new Server();

		initConnnector(server, configs);

		initWebappContext(server,configs);

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
		connector.setHost("127.0.0.1");
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
