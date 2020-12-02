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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
	private Boolean ignoreScanClassFile = false;

	private Boolean enablessl;
	private Boolean needClientAuth;
	private Boolean enableJNDI;

	private String configurationClasses;
	private String resourceMapping;

	private String scanlist;
	private String jettyXML;

	private String excludedclasspath;

	private int eclipseListener;

	private static boolean debug = false;
	public Configs() {
		debug = getBooleanProp("rjrdebug",false);

		context = getProp("rjrcontext");
		webAppDir = getProp("rjrwebapp");
		if(webAppDir != null){
			if(webAppDir.matches("^\".*?\"$")){
				webAppDir = webAppDir.substring(1,webAppDir.length()-1);
			}
		}

		jettyXML = getProp("rjrjettyXMLPath");

		eclipseListener = getIntProp("rjrEclipseListener", -1);
		port = getIntProp("rjrport");
		sslport = getIntProp("rjrsslport");
		keystore = getProp("rjrkeystore");
		password = getProp("rjrpassword");

		excludedclasspath = getProp("rjrexcludedclasspath");
		try{
			if(excludedclasspath != null){
				Pattern.compile(excludedclasspath);
			}
		}catch(PatternSyntaxException ex){
			System.err.println("Excluded classpath setting occur regex syntax error, skipped. \n(Error Message:" + ex.getMessage()+")");
			excludedclasspath = null;
		}

		String classpath = getProp("rjrclasspath");
		webAppClassPath = resovlePropConfigFile(classpath);

		scanlist = resovlePropConfigFile(getProp("rjrscanlist"));

		keyPassword = getProp("rjrkeypassword");
		scanIntervalSeconds = getIntProp("rjrscanintervalseconds");

		enablescanner = getBooleanProp("rjrenablescanner");

		parentLoaderPriority = getBooleanProp("rjrparentloaderpriority", true);

		enablessl = getBooleanProp("rjrenablessl");

		needClientAuth = getBooleanProp("rjrneedclientauth");

		enableJNDI = getBooleanProp("rjrenbaleJNDI");

		configurationClasses = getProp("rjrconfigurationclasses", "");

		resourceMapping = trimQuote(getProp("rjrResourceMapping", ""));

		ignoreScanClassFile = getBooleanProp("rjrignoreScanClassFile", false);

	}

	private static String getProp(String key){
		printSystemProperty(key);
		return System.getProperty(key);
	}

	private static String getProp(String key,String def){
		printSystemProperty(key);
		return System.getProperty(key,def);
	}

	private static Integer getIntProp(String key){
		printSystemProperty(key);
		return Integer.getInteger(key);
	}

	private static Integer getIntProp(String key,int def){
		printSystemProperty(key);
		return Integer.getInteger(key,def);
	}

	private static Boolean getBooleanProp(String key){
		printSystemProperty(key);
		return Boolean.getBoolean(key);
	}

	//debug tool
	public static void printSystemProperty(String key){
		if(!debug) return ;
		String result = System.getProperty(key);
		if(result!= null){
			System.out.println("-D"+key+"="+result+" ");
		}
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

		configuration.add("runjettyrun.webapp.RJRWebInfConfiguration");
		configuration.add("org.eclipse.jetty.webapp.WebXmlConfiguration");
		configuration.add("runjettyrun.webapp.RJRMetaInfoConfiguration");
		configuration.add("org.eclipse.jetty.webapp.FragmentConfiguration");

		if(!getBooleanProp("rjrDisableannotation")){
			configuration.add("runjettyrun.annotation.RJRAnnotationConfiguration");
		}

		//Configuration removed after 8.1.2 v20120308.
//		configuration.add("org.eclipse.jetty.annotations.ContainerInitializerConfiguration");
		configuration.add("org.eclipse.jetty.webapp.JettyWebXmlConfiguration");
		//remove after 9.2
		//configuration.add("org.eclipse.jetty.webapp.TagLibConfiguration");

		if (getEnableJNDI()) {

			//http://wiki.eclipse.org/Jetty/Feature/JNDI#Applying_JNDI_to_a_Single_Web_App
			/* A way to verify the class correct or not
			org.eclipse.jetty.webapp.WebInfConfiguration d;
			org.eclipse.jetty.plus.webapp.EnvConfiguration d2;
			org.eclipse.jetty.plus.webapp.PlusConfiguration d3;
			org.eclipse.jetty.webapp.JettyWebXmlConfiguration d4;
			org.eclipse.jetty.webapp.TagLibConfiguration d5;
			*/

			configuration.add("org.eclipse.jetty.plus.webapp.EnvConfiguration");
			configuration.add("org.eclipse.jetty.plus.webapp.PlusConfiguration");

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

	public static String trimQuote(String str){
		if(str!= null && str.startsWith("\"") && str.endsWith("\"")){
			return str.substring(1,str.length()-1);
		}
		return str;
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
	private static String resovlePropConfigFile(String prop) {

		if (prop != null && prop.startsWith("file://")) {
			try {
				String filePath = prop.substring(7);

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

		return prop;
	}


	private static Boolean getBooleanProp(String propertiesKey, boolean def) {
		printSystemProperty(propertiesKey);
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

	public int getEclipseListenerPort() {
		return eclipseListener;
	}

	public String getExcludedclasspath() {
		return excludedclasspath;
	}

	public String getJettyXML() {
		return jettyXML;
	}

	public void setJettyXML(String jettyXML) {
		this.jettyXML = jettyXML;
	}

	public String getScanlist() {
		return scanlist;
	}

	public Boolean getIgnoreScanClassFile() {
		return ignoreScanClassFile;
	}

}