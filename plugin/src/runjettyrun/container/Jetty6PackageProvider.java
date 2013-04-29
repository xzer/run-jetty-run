package runjettyrun.container;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.osgi.framework.Bundle;

import runjettyrun.Plugin;
import runjettyrun.extensions.IJettyPackageProvider;
import runjettyrun.utils.ProjectUtil;

public class Jetty6PackageProvider implements IJettyPackageProvider {

	public static final String VERSION = "Jetty 6.1.26";

	public IRuntimeClasspathEntry[] getPackage(String version, int type) {
		try {
			Bundle bundle = Plugin.getDefault().getBundle();
			if (type == TYPE_JETTY_BUNDLE) {

				return ProjectUtil.getLibs(bundle,
						ProjectUtil.getJarFilesIn(bundle, "lib"));

			} else if (type == TYPE_UTIL) {
				return ProjectUtil.getLibs(bundle,
						ProjectUtil.getJarFilesIn(bundle, "jndilib"));
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
