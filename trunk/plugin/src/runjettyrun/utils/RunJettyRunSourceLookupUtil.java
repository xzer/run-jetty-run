package runjettyrun.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.launching.JavaRuntime;

import runjettyrun.Plugin;

public class RunJettyRunSourceLookupUtil {

	public static List<IProject> findMavenRelatedProjects(ILaunchConfiguration configuration)
			throws CoreException {
		IJavaProject proj = JavaRuntime.getJavaProject(configuration);
		if (proj == null) {
			Plugin.logError("No project!");
			throw new IllegalArgumentException("project is null");
		}
		IRuntimeClasspathEntry[] entries = RunJettyRunClasspathUtil
				.filterWebInfLibs(
						JavaRuntime.computeUnresolvedRuntimeClasspath(proj),
						configuration);

		// Remove JRE entry/entries.

		IRuntimeClasspathEntry stdJreEntry = JavaRuntime
				.computeJREEntry(configuration);
		IRuntimeClasspathEntry projJreEntry = JavaRuntime.computeJREEntry(proj);
		List<IRuntimeClasspathEntry> entryList = new ArrayList<IRuntimeClasspathEntry>(
				entries.length);

		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry entry = entries[i];
			if (entry.equals(stdJreEntry))
				continue;
			if (entry.equals(projJreEntry))
				continue;
			entryList.add(entry);
		}


		boolean isMaven = ProjectUtil.isMavenProject(proj.getProject());
		List<IProject> result = new ArrayList<IProject>();
		for(IRuntimeClasspathEntry entry :entryList){
			result.addAll(searchProjectInRuntimeEntry(entry, configuration, isMaven));
		}

		return result;
	}

	/**
	 * @see JavaRuntime#resolveRuntimeClasspathEntry
	 * @param entries
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private static  List<IProject> searchProjectInRuntimeEntry(
			IRuntimeClasspathEntry entry, ILaunchConfiguration configuration,
			boolean isMaven) throws CoreException {

		if (RunJettyRunClasspathUtil.isDefaultProjectClasspathEntry(entry)) {
			IRuntimeClasspathEntry2 entry2 = (IRuntimeClasspathEntry2) entry;
			IRuntimeClasspathEntry[] entries = entry2
					.getRuntimeClasspathEntries(configuration);
			List<IProject> resolved = new ArrayList<IProject>();
			for (int i = 0; i < entries.length; i++) {
				IRuntimeClasspathEntry entryCur = entries[i];

				if (isMaven
						&& (RunJettyRunClasspathResolver
								.isM2EMavenContainer(entryCur) || RunJettyRunClasspathResolver
								.isWebAppContainer(entryCur))) {
					resolved.addAll( getIProjectInMavenContainerEntries(entryCur,
									configuration));
				}
			}

			return resolved;
		} else {
			return new ArrayList<IProject>();

		}
	}

	/**
	 * Performs default resolution for a container entry. Delegates to the Java
	 * model.
	 */
	public static List<IProject> getIProjectInMavenContainerEntries(
			IRuntimeClasspathEntry entry, ILaunchConfiguration config)
			throws CoreException {
		IJavaProject project = entry.getJavaProject();
		if (project == null) {
			project = JavaRuntime.getJavaProject(config);
		}
		if (project == null || entry == null) {
			// cannot resolve without entry or project context
			return new ArrayList<IProject>();
		}
		IClasspathContainer container = JavaCore.getClasspathContainer(
				entry.getPath(), project);
		if (container == null) {
			return new ArrayList<IProject>();
		}
		IClasspathEntry[] cpes = container.getClasspathEntries();

		List<IProject> result = new ArrayList<IProject>();

		for (int i = 0; i < cpes.length; i++) {
			IClasspathEntry cpe = cpes[i];
			if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT) {

				IResource ir = getResource(cpe.getPath());
				if(ir instanceof IProject){
					result.add((IProject)ir);
				}
			}
		}

		return result;
	}
	/**
	 * Returns the resource in the workspace assciated with the given
	 * absolute path, or <code>null</code> if none. The path may have
	 * a device.
	 *
	 * @param path absolute path, or <code>null</code>
	 * @return resource or <code>null</code>
	 */
	private static IResource getResource(IPath path) {
		if (path != null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			// look for files or folders with the given path
			IFile[] files = root.findFilesForLocation(path);
			if (files.length > 0) {
				return files[0];
			}
			IContainer[] containers = root.findContainersForLocation(path);
			if (containers.length > 0) {
				return containers[0];
			}
			if (path.getDevice() == null) {
				// search relative to the workspace if no device present
				return root.findMember(path);
			}
		}
		return null;
	}
}
