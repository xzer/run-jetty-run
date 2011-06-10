package runjettyrun.container;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver2;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.framework.Bundle;

import runjettyrun.Plugin;
import runjettyrun.extensions.IJettyPackageProvider;

public class RunJettyRunContainerResolver implements
		IRuntimeClasspathEntryResolver2 {

	public IVMInstall resolveVMInstall(IClasspathEntry entry)
			throws CoreException {
		return null;
	}

	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(
			IRuntimeClasspathEntry entry, IJavaProject project)
			throws CoreException {

		//Not a expected call , we need configuration to fingure out how user use it.
		throw new UnsupportedOperationException("...");

	}

	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(
			IRuntimeClasspathEntry entry, ILaunchConfiguration configuration)
			throws CoreException {
		return resolvedEntry(entry , configuration);
	}
	private  IRuntimeClasspathEntry[] resolvedEntry(IRuntimeClasspathEntry entry,
			ILaunchConfiguration configuration){

		String ver = "";
		try {
			ver = configuration.getAttribute(Plugin.ATTR_SELECTED_JETTY_VERSION, "");
		} catch (CoreException e) {
			e.printStackTrace();
		}
		Plugin plg = Plugin.getDefault();
		if(Plugin.CONTAINER_RJR_JETTY.equals(entry.getVariableName())){

			if(plg.supportJetty(ver, IJettyPackageProvider.TYPE_JETTY_BUNDLE)){
				return plg.getPackages(ver, IJettyPackageProvider.TYPE_JETTY_BUNDLE);
			}else{
				return plg.getDefaultPackages( IJettyPackageProvider.TYPE_JETTY_BUNDLE);
			}
		}

		if(Plugin.CONTAINER_RJR_JETTY_JNDI.equals(entry.getVariableName())){
			if(plg.supportJetty(ver, IJettyPackageProvider.TYPE_UTIL)){
				return plg.getPackages(ver, IJettyPackageProvider.TYPE_UTIL);
			}else{
				return plg.getDefaultPackages( IJettyPackageProvider.TYPE_UTIL);
			}
		}

		return new IRuntimeClasspathEntry[0];
	}

	public boolean isVMInstallReference(IClasspathEntry entry) {
		return false;
	}


	private IRuntimeClasspathEntry[] getBootstrap() {

		List<IRuntimeClasspathEntry> entries = new ArrayList<IRuntimeClasspathEntry>();
		Bundle bundle = Plugin.getDefault().getBundle();
		URL installUrl = bundle.getEntry("/");
		addRelativeArchiveEntry(entries, installUrl, "run-jetty-run-bootstrap");
		return entries.toArray(new IRuntimeClasspathEntry[entries.size()]);
	}


	private void addRelativeArchiveEntry(List<IRuntimeClasspathEntry> entries,
			URL installUrl, String libJarName) {

		try {
			String relativePath = "lib/" + libJarName + ".jar";
			URL bundleUrl = new URL(installUrl, relativePath);
			addArchiveEntry(entries, bundleUrl);
			return;
		} catch (MalformedURLException e) {
			Plugin.logError(e);
			return;
		}
	}

	private void addArchiveEntry(List<IRuntimeClasspathEntry> entries,
			URL bundleUrl) {

		try {
			URL fileUrl = FileLocator.toFileURL(bundleUrl);
			IRuntimeClasspathEntry rcpe = JavaRuntime
					.newArchiveRuntimeClasspathEntry(new Path(fileUrl.getFile()));
			entries.add(rcpe);
			return;
		} catch (IOException e) {
			Plugin.logError(e);
			return;
		}
	}

}
