package runjettyrun.utils;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

import runjettyrun.Plugin;

public class RunJettyRunLaunchConfigurationUtil {

	public static ILaunchConfiguration findLaunchConfiguration(
			String projectName) {
		ILaunchManager lnmanger = DebugPlugin.getDefault().getLaunchManager();
		try {
			for (ILaunchConfiguration lc : lnmanger.getLaunchConfigurations()) {

				if (isSupported(lc, projectName))
					return lc;

			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static boolean isSupported(ILaunchConfiguration launch,
			String projectname) throws CoreException {
		String currentProjectName = launch.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		if (!currentProjectName.equals(projectname))
			return false;

		if ("".equals(launch.getAttribute(Plugin.ATTR_CONTEXT, "")))
			return false;

		if ("".equals(launch.getAttribute(Plugin.ATTR_WEBAPPDIR, "")))
			return false;

		return true;
	}

	public static boolean validation(ILaunchConfiguration config) {

		try {
			String projectName = config.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
			IProject project = null;
			if (projectName.length() > 0) {
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				IStatus status = workspace.validateName(projectName,
						IResource.PROJECT);
				if (status.isOK()) {
					project = ResourcesPlugin.getWorkspace().getRoot()
							.getProject(projectName);
					if (!project.exists()) {
						return false;
					}
					if (!project.isOpen()) {
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
			String directory = config.getAttribute(Plugin.ATTR_WEBAPPDIR, "");
			if (!"".equals(directory.trim())) {

				if("/".equals(directory)){ //root as webapp folder
					IFolder file = project.getFolder(new Path("/WEB-INF"));
					if (!file.exists()) {
						return false;
					}

				}else{
					IFolder folder = project.getFolder(directory);
					if (!folder.exists()) {
						return false;
					}
					IFolder file = project.getFolder(new Path(directory
							+ "/WEB-INF"));
					if (!file.exists()) {
						return false;
					}
				}
			} else {
				return false;
			}

			String port = config.getAttribute(Plugin.ATTR_PORT, "");
			String sslPort = config.getAttribute(Plugin.ATTR_SSL_PORT, "");
			if (port.length() == 0 && sslPort.length() == 0) {
				return false;
			}
			if (isInvalidPort(port))
				return false;
			if (isInvalidPort(sslPort))
				return false;

			if (config.getAttribute(Plugin.ATTR_ENABLE_SCANNER, false)) {
				String scan = config.getAttribute(
						Plugin.ATTR_SCANINTERVALSECONDS, "5");

				if (scan.length() == 0) {
					return false;
				}
				if (isInvalidScan(scan))
					return false;
			}

			if (config.getAttribute(Plugin.ATTR_ENABLE_SSL, false)) {
				// Validate that we have the necessary key store info.
				String keystore = config.getAttribute(Plugin.ATTR_KEYSTORE, "")
						.trim();
				String keyPwd = config.getAttribute(Plugin.ATTR_KEY_PWD, "")
						.trim();
				String password = config.getAttribute(Plugin.ATTR_PWD, "")
						.trim();
				if (keystore.length() == 0) {
					return false;
				} else if (!new File(keystore).isFile()) {
					return false;
				}
				if (keyPwd.length() == 0) {
					return false;
				}
				if (password.length() == 0) {
					return false;
				}
			}

			return true;
		} catch (CoreException ex) {
			return false;
		}
	}

	public static boolean isInvalidPort(String s) {
		if (s.length() == 0)
			return false;
		try {
			int p = Integer.parseInt(s);
			if (1 <= p && p <= 65535)
				return false;
		} catch (NumberFormatException e) {
		}
		return true;
	}

	public static boolean isInvalidScan(String s) {
		try {
			Integer.parseInt(s);
			return false;
		} catch (NumberFormatException e) {
		}
		return true;
	}

	public static IRuntimeClasspathEntry[] loadPackage(
			ILaunchConfiguration configuration, int type) {
		String ver = "";
		try {
			ver = configuration.getAttribute(
					Plugin.ATTR_SELECTED_JETTY_VERSION, "");
		} catch (CoreException e) {
			e.printStackTrace();
		}
		Plugin plg = Plugin.getDefault();
		if (plg.supportJetty(ver, type)) {
			return (plg.getPackages(ver, type));
		} else {
			return (plg.getDefaultPackages(type));
		}

	}
}
