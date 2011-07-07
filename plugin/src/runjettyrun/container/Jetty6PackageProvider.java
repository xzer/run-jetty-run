package runjettyrun.container;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

import runjettyrun.Plugin;
import runjettyrun.extensions.IJettyPackageProvider;
import runjettyrun.utils.ProjectUtil;

public class Jetty6PackageProvider implements IJettyPackageProvider {

	public static final String VERSION = "Jetty 6.1.26";

	public IRuntimeClasspathEntry[] getPackage(String version, int type) {
		try {
			if (type == TYPE_JETTY_BUNDLE) {

				return ProjectUtil.getLibs(Plugin.getDefault().getBundle(),
						new String[]{
							"lib/core-3.1.1.jar",
							"lib/jetty-6.1.26.jar",
							"lib/jetty-management-6.1.26.jar",
							"lib/jetty-util-6.1.26.jar",
							"lib/jsp-2.1.jar",
							"lib/jsp-api-2.1.jar",
							"lib/run-jetty-run-bootstrap.jar",
							"lib/servlet-api-2.5-20081211.jar"
						}
					);

			} else if (type == TYPE_UTIL) {
				return ProjectUtil.getLibs(Plugin.getDefault().getBundle(),
						new String[]{
						"jndilib/activation-1.1.1.jar",
						"jndilib/jetty-naming-6.1.26.jar",
						"jndilib/jetty-plus-6.1.26.jar",
						"jndilib/mail-1.4.jar"
					}
				);
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
