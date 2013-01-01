package run_jetty_run_jetty8;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

import runjettyrun.extensions.IJettyPackageProvider;
import runjettyrun.utils.ProjectUtil;

public class Jetty8PackageProvider implements IJettyPackageProvider {

	public static final String VERSION = "Jetty 8.1.8.v20121106";

	public IRuntimeClasspathEntry[] getPackage(String version, int type) {
		try {
			if (type == TYPE_JETTY_BUNDLE) {

				return ProjectUtil.getLibs(Activator.getDefault().getBundle(),
						new String[]{
					"lib/com.sun.el-2.2.0.v201108011116.jar",
					"lib/javax.annotation_1.1.0.v201105051105.jar",
					"lib/javax.el-2.2.0.v201108011116.jar",
					"lib/javax.servlet.jsp-2.2.0.v201112011158.jar",
					"lib/javax.servlet.jsp.jstl-1.2.0.v201105211821.jar",
					"lib/jetty-ajp-8.1.8.v20121106.jar",
					"lib/jetty-annotations-8.1.8.v20121106.jar",
					"lib/jetty-client-8.1.8.v20121106.jar",
					"lib/jetty-continuation-8.1.8.v20121106.jar",
					"lib/jetty-deploy-8.1.8.v20121106.jar",
					"lib/jetty-http-8.1.8.v20121106.jar",
					"lib/jetty-io-8.1.8.v20121106.jar",
					"lib/jetty-jmx-8.1.8.v20121106.jar",
					"lib/jetty-jndi-8.1.8.v20121106.jar",
					"lib/jetty-overlay-deployer-8.1.8.v20121106.jar",
					"lib/jetty-plus-8.1.8.v20121106.jar",
					"lib/jetty-policy-8.1.8.v20121106.jar",
					"lib/jetty-rewrite-8.1.8.v20121106.jar",
					"lib/jetty-security-8.1.8.v20121106.jar",
					"lib/jetty-server-8.1.8.v20121106.jar",
					"lib/jetty-servlet-8.1.8.v20121106.jar",
					"lib/jetty-servlets-8.1.8.v20121106.jar",
					"lib/jetty-util-8.1.8.v20121106.jar",
					"lib/jetty-webapp-8.1.8.v20121106.jar",
					"lib/jetty-websocket-8.1.8.v20121106.jar",
					"lib/jetty-xml-8.1.8.v20121106.jar",
					"lib/jsp-impl-2.2.2.b05.0.jar",
					"lib/org.apache.jasper.glassfish-2.2.2.v201112011158.jar",
					"lib/org.apache.taglibs.standard.glassfish-1.2.0.v201112081803.jar",
					"lib/org.objectweb.asm_3.3.1.v201101071600.jar",
					"lib/run-jetty-run-bootstrap-jetty8.jar",
					"lib/servlet-api-3.0.jar",
					"lib/spdy-core-8.1.8.v20121106.jar",
					"lib/spdy-jetty-8.1.8.v20121106.jar",
					"lib/spdy-jetty-http-8.1.8.v20121106.jar"
				});

			} else if (type == TYPE_UTIL) {
				return ProjectUtil.getLibs(Activator.getDefault().getBundle(),
				 new String[]{
					"jndilib/javax.activation_1.1.0.v201105071233.jar",
					"jndilib/javax.mail.glassfish_1.4.1.v201005082020.jar",
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

	public static void main(String[] args) {
		//We have to update the list manually since I still didn't know how to fetch all the files in a folder in OSGi.

		for(File f :new File("lib").listFiles()){
			System.out.println("\"lib/"+f.getName()+"\",");
		}

		for(File f :new File("jndilib").listFiles()){
			System.out.println("\"jndilib/"+f.getName()+"\",");
		}
	}
}
