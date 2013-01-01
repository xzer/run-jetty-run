package run_jetty_run_jetty9;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

import runjettyrun.extensions.IJettyPackageProvider;
import runjettyrun.utils.ProjectUtil;

public class Jetty9PackageProvider implements IJettyPackageProvider {

	public static final String VERSION = "Jetty 9.0.0.M3";

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
					"lib/jetty-annotations-9.0.0.M3.jar",
					"lib/jetty-client-9.0.0.M3.jar",
					"lib/jetty-continuation-9.0.0.M3.jar",
					"lib/jetty-deploy-9.0.0.M3.jar",
					"lib/jetty-http-9.0.0.M3.jar",
					"lib/jetty-io-9.0.0.M3.jar",
					"lib/jetty-jaas-9.0.0.M3.jar",
					
					"lib/jetty-jmx-9.0.0.M3.jar",
					"lib/jetty-jndi-9.0.0.M3.jar",
					"lib/jetty-plus-9.0.0.M3.jar",
					"lib/jetty-rewrite-9.0.0.M3.jar",
					"lib/jetty-security-9.0.0.M3.jar",
					"lib/jetty-server-9.0.0.M3.jar",
					"lib/jetty-servlet-9.0.0.M3.jar",
					"lib/jetty-servlets-9.0.0.M3.jar",
					"lib/jetty-util-9.0.0.M3.jar",
					"lib/jetty-webapp-9.0.0.M3.jar",
					"lib/jetty-xml-9.0.0.M3.jar",
					"lib/javax.websocket-api-0.0.006.draft.jar",
					"lib/websocket-api-9.0.0.M3.jar",
					"lib/websocket-client-9.0.0.M3.jar",
					"lib/websocket-common-9.0.0.M3.jar",
					"lib/websocket-server-9.0.0.M3.jar",
					"lib/websocket-servlet-9.0.0.M3.jar",
					"lib/org.apache.jasper.glassfish-2.2.2.v201112011158.jar",
					"lib/org.apache.taglibs.standard.glassfish-1.2.0.v201112081803.jar",
					"lib/org.objectweb.asm_3.3.1.v201101071600.jar",
					"lib/org.eclipse.jdt.core-3.7.1.jar",
					"lib/run-jetty-run-bootstrap-jetty9.jar",
					"lib/servlet-api-3.0.jar",
					"lib/spdy-client-9.0.0.M3.jar",
					"lib/spdy-core-9.0.0.M3.jar",
					"lib/spdy-http-server-9.0.0.M3.jar",
					"lib/spdy-server-9.0.0.M3.jar",
						
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
