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

public class RunJettyRunContainerResolver implements
		IRuntimeClasspathEntryResolver2 {

	public IVMInstall resolveVMInstall(IClasspathEntry entry)
			throws CoreException {
		return null;
	}

	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(
			IRuntimeClasspathEntry entry, IJavaProject project)
			throws CoreException {
		return resolvedEntry(entry);

	}

	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(
			IRuntimeClasspathEntry entry, ILaunchConfiguration configuration)
			throws CoreException {
		return resolvedEntry(entry);
	}

	public boolean isVMInstallReference(IClasspathEntry entry) {
		return false;
	}

	private  IRuntimeClasspathEntry[] resolvedEntry(IRuntimeClasspathEntry entry){
		if(Plugin.CONTAINER_RJR_BOOTSTRAP.equals(entry.getVariableName()))
			return getBootstrap();

		if(Plugin.CONTAINER_RJR_JETTY6.equals(entry.getVariableName()))
			return getJetty6();

		return new IRuntimeClasspathEntry[0];
	}

	private IRuntimeClasspathEntry[] getBootstrap() {

		List<IRuntimeClasspathEntry> entries = new ArrayList<IRuntimeClasspathEntry>();
		Bundle bundle = Plugin.getDefault().getBundle();
		URL installUrl = bundle.getEntry("/");
		addRelativeArchiveEntry(entries, installUrl, "run-jetty-run-bootstrap");
		return entries.toArray(new IRuntimeClasspathEntry[entries.size()]);
	}
	private IRuntimeClasspathEntry[] getJetty6() {

		List<IRuntimeClasspathEntry> entries = new ArrayList<IRuntimeClasspathEntry>();
		Bundle bundle = Plugin.getDefault().getBundle();
		URL installUrl = bundle.getEntry("/");

		addRelativeArchiveEntry(entries, installUrl, "jetty-"
				+ Plugin.JETTY_VERSION);
		addRelativeArchiveEntry(entries, installUrl, "jetty-util-"
				+ Plugin.JETTY_VERSION);
		addRelativeArchiveEntry(entries, installUrl, "jetty-management-"
				+ Plugin.JETTY_VERSION);
		addRelativeArchiveEntry(entries, installUrl, "servlet-api-2.5-20081211");
		addRelativeArchiveEntry(entries, installUrl, "jsp-api-2.1");
		addRelativeArchiveEntry(entries, installUrl, "jsp-2.1");
		addRelativeArchiveEntry(entries, installUrl, "core-3.1.1");

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
