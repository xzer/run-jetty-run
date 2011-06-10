package runjettyrun;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.util.Scanner;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Started up by the plugin's runner. Starts Jetty.
 *
 * @author hillenius, jsynge, jumperchen, TonyQ
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
		System.err.println("Running Jetty 7.4.2.v20110526");
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
		System.err.println("Context path:"+configs.getContext());

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
					configs.getWebAppClassPath());
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
		sslcontextfactory.setKeyStore(keystore);
		sslcontextfactory.setKeyStorePassword(password);

		sslcontextfactory.setKeyManagerPassword(keyPassword);

		if (needClientAuth) {
			System.err.println("Enable NeedClientAuth.");
			sslcontextfactory.setNeedClientAuth(needClientAuth);
		}

		SslSelectChannelConnector sslConnector = new SslSelectChannelConnector(sslcontextfactory);
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
		try {
			scanner.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
