package runjettyrun;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;
import org.osgi.framework.Bundle;

public class JettyLaunchConfigurationClassPathProvider extends
		StandardClasspathProvider {

	public JettyLaunchConfigurationClassPathProvider() {
	}

	@Override
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(
			ILaunchConfiguration configuration) throws CoreException {
		IRuntimeClasspathEntry[] classpath = super
				.computeUnresolvedClasspath(configuration);
		boolean useDefault = configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
		if (useDefault) {
			classpath = filterWebInfLibs(classpath, configuration);
			classpath = addJettyAndBootstrap(classpath, configuration);

		} else {
			// recover persisted classpath
			return recoverRuntimePath(configuration,
					IJavaLaunchConfigurationConstants.ATTR_CLASSPATH);
		}
		return classpath;
	}

	private IRuntimeClasspathEntry[] addJettyAndBootstrap(
			IRuntimeClasspathEntry[] existing, ILaunchConfiguration config) {

		List<IRuntimeClasspathEntry> entries = new ArrayList<IRuntimeClasspathEntry>();
		entries.addAll(Arrays.asList(existing));
		Bundle bundle = Plugin.getDefault().getBundle();
		URL installUrl = bundle.getEntry("/");
		try {
			URL bootstrapJarUrl = FileLocator.toFileURL(new URL(installUrl,
					"lib/run-jetty-run-bootstrap.jar"));
			entries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(
					bootstrapJarUrl.getFile())));
			URL jettyJarUrl = FileLocator.toFileURL(new URL(installUrl,
					"lib/jetty-" + Plugin.JETTY_VERSION + ".jar"));
			entries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(
					jettyJarUrl.getFile())));
			URL jettyUtilJarUrl = FileLocator.toFileURL(new URL(installUrl,
					"lib/jetty-util-" + Plugin.JETTY_VERSION + ".jar"));
			entries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(
					jettyUtilJarUrl.getFile())));
			URL jettyMngmtJarUrl = FileLocator.toFileURL(new URL(installUrl,
					"lib/jetty-management-" + Plugin.JETTY_VERSION + ".jar"));
			entries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(
					jettyMngmtJarUrl.getFile())));
			URL jspApiJarUrl = FileLocator.toFileURL(new URL(installUrl,
					"lib/jsp-api-2.1.jar"));
			entries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(
					jspApiJarUrl.getFile())));
			URL jspJarUrl = FileLocator.toFileURL(new URL(installUrl,
					"lib/jsp-2.1.jar"));
			entries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(
					jspJarUrl.getFile())));
			URL eclipseCompilerJarUrl = FileLocator.toFileURL(new URL(
					installUrl, "lib/core-3.1.1.jar"));
			entries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(
					eclipseCompilerJarUrl.getFile())));
		} catch (IOException e) {
			Plugin.logError(e);
		}
		return entries.toArray(new IRuntimeClasspathEntry[entries.size()]);
	}

	private IRuntimeClasspathEntry[] filterWebInfLibs(
			IRuntimeClasspathEntry[] defaults,
			ILaunchConfiguration configuration) {

		IJavaModel javaModel = JavaCore.create(ResourcesPlugin.getWorkspace()
				.getRoot());
		String projectName = null;
		String webAppDirName = null;
		try {
			projectName = configuration.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			webAppDirName = configuration.getAttribute(Plugin.ATTR_WEBAPPDIR,
					"");
		} catch (CoreException e) {
			Plugin.logError(e);
		}

		if (projectName == null || projectName.trim().equals("")
				|| webAppDirName == null || webAppDirName.trim().equals("")) {
			return defaults;
		}

		IJavaProject project = javaModel.getJavaProject(projectName);
		if (project == null) {
			return defaults;
		}

		// this should be fine since the plugin checks whether WEB-INF exists
		IFolder webInfDir = project.getProject().getFolder(
				new Path(webAppDirName)).getFolder("WEB-INF");
		if (webInfDir == null || !webInfDir.exists()) {
			return defaults;
		}
		IFolder lib = webInfDir.getFolder("lib");
		if (lib == null || !lib.exists()) {
			return defaults;
		}

		// ok, so we have a WEB-INF/lib dir, which means that we should filter
		// out the entries in there since if the user wants those entries, they
		// should be part of the project definition already
		List<IRuntimeClasspathEntry> keep = new ArrayList<IRuntimeClasspathEntry>();
		for (int i = 0; i < defaults.length; i++) {
			if (defaults[i].getType() != IRuntimeClasspathEntry.ARCHIVE) {
				keep.add(defaults[i]);
				continue;
			}
			IResource resource = defaults[i].getResource();
			if (resource != null && !resource.getParent().equals(lib)) {
				keep.add(defaults[i]);
				continue;
			}
		}

		return keep.toArray(new IRuntimeClasspathEntry[keep.size()]);
	}
}
