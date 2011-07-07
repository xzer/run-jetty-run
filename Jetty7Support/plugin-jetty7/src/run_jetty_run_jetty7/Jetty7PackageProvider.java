package run_jetty_run_jetty7;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

import runjettyrun.extensions.IJettyPackageProvider;
import runjettyrun.utils.ProjectUtil;

public class Jetty7PackageProvider implements IJettyPackageProvider {

	public static final String VERSION = "Jetty 7.4.2.v20110526";
	public IRuntimeClasspathEntry[] getPackage(String version, int type) {
		try {
			if (type == TYPE_JETTY_BUNDLE) {

				return ProjectUtil.getLibs(Activator.getDefault().getBundle(),
						new String[]{
					"lib/com.sun.el_1.0.0.v201004190952.jar",
					"lib/ecj-3.6.jar",
					"lib/javax.el_2.1.0.v201004190952.jar",
					"lib/javax.servlet.jsp.jstl_1.2.0.v201004190952.jar",
					"lib/javax.servlet.jsp_2.1.0.v201004190952.jar",
					"lib/jetty-ajp-7.4.2.v20110526.jar",
					"lib/jetty-annotations-7.4.2.v20110526.jar",
					"lib/jetty-client-7.4.2.v20110526.jar",
					"lib/jetty-continuation-7.4.2.v20110526.jar",
					"lib/jetty-deploy-7.4.2.v20110526.jar",
					"lib/jetty-http-7.4.2.v20110526.jar",
					"lib/jetty-io-7.4.2.v20110526.jar",
					"lib/jetty-jmx-7.4.2.v20110526.jar",
					"lib/jetty-jndi-7.4.2.v20110526.jar",
					"lib/jetty-jsp-2.1-7.4.2.v20110526.jar",
					"lib/jetty-overlay-deployer-7.4.2.v20110526.jar",
					"lib/jetty-plus-7.4.2.v20110526.jar",
					"lib/jetty-policy-7.4.2.v20110526.jar",
					"lib/jetty-rewrite-7.4.2.v20110526.jar",
					"lib/jetty-security-7.4.2.v20110526.jar",
					"lib/jetty-server-7.4.2.v20110526.jar",
					"lib/jetty-servlet-7.4.2.v20110526.jar",
					"lib/jetty-servlets-7.4.2.v20110526.jar",
					"lib/jetty-util-7.4.2.v20110526.jar",
					"lib/jetty-webapp-7.4.2.v20110526.jar",
					"lib/jetty-websocket-7.4.2.v20110526.jar",
					"lib/jetty-xml-7.4.2.v20110526.jar",
					"lib/org.apache.jasper.glassfish_2.1.0.v201007080150.jar",
					"lib/org.apache.taglibs.standard.glassfish_1.2.0.v201004190952.jar",
					"lib/run-jetty-run-bootstrap-jetty7.jar",
					"lib/servlet-api-2.5.jar"
				});

			} else if (type == TYPE_UTIL) {
				return ProjectUtil.getLibs(Activator.getDefault().getBundle(),
				 new String[]{
					"jndilib/javax.activation_1.1.0.v201005080500.jar",
					"jndilib/javax.mail.glassfish_1.4.1.v201005082020.jar"
				});
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}


	public boolean acceptType(int type) {
		return ( type == TYPE_JETTY_BUNDLE) || (type == TYPE_UTIL) ;
	}

	public String getJettyVersion() {
		return VERSION;
	}

	public boolean accpet(String ver) {
		return VERSION.equals(ver);
	}

	public String getName() {
		return VERSION;
	}

	public String getText() {
		return VERSION;
	}

}
