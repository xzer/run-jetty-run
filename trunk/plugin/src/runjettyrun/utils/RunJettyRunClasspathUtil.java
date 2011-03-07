package runjettyrun.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

import runjettyrun.Plugin;

public class RunJettyRunClasspathUtil {

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
		IFolder webInfDir = project.getProject()
				.getFolder(new Path(webAppDirName)).getFolder("WEB-INF");
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
}
