package runjettyrun;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A configuration object to handle the complicated parse job ,
 * and make thing easier.
 */

public class Configs {
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
		if(webAppDir != null){
			if(webAppDir.matches("^\".*?\"$")){
				webAppDir = webAppDir.substring(1,webAppDir.length()-1);
			}
		}
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

		if (getEnablessl() && getSslport() != null){
			if (!available(sslport)) {
				throw new IllegalStateException("SSL port :" + sslport
						+ " already in use!");
			}
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
}