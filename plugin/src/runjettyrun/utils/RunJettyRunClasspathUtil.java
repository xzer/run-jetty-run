package runjettyrun.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

import runjettyrun.Plugin;

public class RunJettyRunClasspathUtil {

	public static List<IRuntimeClasspathEntry> getProjectClasspathsForUserlibs(IJavaProject project,boolean isMaven) throws CoreException{

		if(!isMaven){
			IClasspathEntry[] entries =	project.getRawClasspath();
			List<IRuntimeClasspathEntry> classpathEntries = new ArrayList<IRuntimeClasspathEntry>();
			Set<IPath> sourceTargets = new HashSet<IPath>();
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry entry = entries[i];
				switch (entry.getEntryKind()) {
					case IClasspathEntry.CPE_CONTAINER:
						IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
						if (container != null) {
							switch (container.getKind()) {
								case IClasspathContainer.K_DEFAULT_SYSTEM:
//									classpathEntries.add(JavaRuntime.newRuntimeContainerClasspathEntry(container.getPath(), IRuntimeClasspathEntry.STANDARD_CLASSES, project));
									break;
								default:
									classpathEntries.add(JavaRuntime.newRuntimeContainerClasspathEntry(container.getPath(), IRuntimeClasspathEntry.USER_CLASSES, project));
									break;
							}
						}
						break;
					case IClasspathEntry.CPE_LIBRARY:
						classpathEntries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(entry.getPath()));
						break;
					case IClasspathEntry.CPE_SOURCE:
						IPath path = entry.getOutputLocation();
						if (path != null) {
							sourceTargets.add(path);
							classpathEntries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
						}
					default:
						break;
				}
			}

			if(!sourceTargets.contains(project.getOutputLocation())){ //add project default output
				classpathEntries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(project.getOutputLocation()));
			}

			// Resolve the entries to actual file/folder locations.
			return classpathEntries;
		}else{

			IRuntimeClasspathEntry[] entries = resolveOutputLocations(project);
			List<IRuntimeClasspathEntry> entryList = new ArrayList<IRuntimeClasspathEntry>(entries.length);
			for (int j = 0; j < entries.length; j++) {
				IRuntimeClasspathEntry e =  entries[j];

				if (!(entryList.contains(e))){
					entryList.add(e);
				}

			}

			return entryList;
		}
	}

	public static IRuntimeClasspathEntry[] getDefaultWebAppClasspaths(
			ILaunchConfiguration configuration) throws CoreException {
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

		// Resolve the entries to actual file/folder locations.

		entries = entryList.toArray(new IRuntimeClasspathEntry[0]);
		return entries;
	}

	public static List<IRuntimeClasspathEntry>  getWebInfLibRuntimeClasspathEntrys(ILaunchConfiguration configuration){
		List<IRuntimeClasspathEntry> result = new ArrayList<IRuntimeClasspathEntry>();

		IFolder lib = getWebInfLib(configuration);

		if (lib == null || !lib.exists()) {
			return result;
		}
		try{
			IResource[] resources = lib.members();

			for(IResource res:resources){
				if("jar".equalsIgnoreCase(res.getFileExtension())){
					result.add(JavaRuntime.newArchiveRuntimeClasspathEntry(res));
				}
			}

		}catch(CoreException e){
			e.printStackTrace();
		}
		return result;
	}

	public static HashSet<String> getWebInfLibLocations(ILaunchConfiguration configuration){
		HashSet<String> result = new HashSet<String>();

		IFolder lib = getWebInfLib(configuration);

		if (lib == null || !lib.exists()) {
			return result;
		}
		try{
			IResource[] resources = lib.members();

			for(IResource res:resources){
				if("jar".equalsIgnoreCase(res.getFileExtension())){
					result.add(res.getLocation().toOSString());
				}
			}

		}catch(CoreException e){
			e.printStackTrace();
		}
		return result;
	}

	public static IFolder getWebInfLib(ILaunchConfiguration configuration){
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
			return null;
		}

		IJavaProject project = javaModel.getJavaProject(projectName);
		if (project == null) {
			return null;
		}

		// this should be fine since the plugin checks whether WEB-INF exists
		IFolder webInfDir = ProjectUtil.getWebappFolder(project.getProject(), webAppDirName).getFolder(
				new Path("WEB-INF"));
		if (webInfDir == null || !webInfDir.exists()) {
			return null;
		}
		IFolder lib = webInfDir.getFolder("lib");
		if (lib == null || !lib.exists()) {
			return null;
		}

		return lib;
	}


	public static IRuntimeClasspathEntry[] filterWebInfLibs(
			IRuntimeClasspathEntry[] defaults,
			ILaunchConfiguration configuration) {

		IFolder lib = getWebInfLib(configuration);
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

	private static IRuntimeClasspathEntry[] resolveOutputLocations(IJavaProject project) throws CoreException {

		if(project == null){
			throw new IllegalArgumentException("project shouldn't be null");
		}

		List<IPath> nonDefault = new ArrayList<IPath>();
		if (project.exists() && project.getProject().isOpen()) {
			IClasspathEntry entries[] = project.getRawClasspath();
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry classpathEntry = entries[i];
				if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					IPath path = classpathEntry.getOutputLocation();
					if (path != null) {
						nonDefault.add(path);
					}
				}
			}
		}
		// add the default location if not already included
		IPath def = project.getOutputLocation();
		if (!nonDefault.contains(def)) {
			nonDefault.add(def);
		}
		IRuntimeClasspathEntry[] locations = new IRuntimeClasspathEntry[nonDefault.size()];
		for (int i = 0; i < locations.length; i++) {
			locations[i] = JavaRuntime.newArchiveRuntimeClasspathEntry((IPath)nonDefault.get(i));
		}
		return locations;
	}

}
