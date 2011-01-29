package runjettyrun.handlers;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;

import runjettyrun.utils.ProjectUtil;
import runjettyrun.utils.RunJettyRunLaunchConfigurationUtil;

public class RunJettyRunLaunch {

	public static boolean launch(IWorkbenchWindow window) {
		return launch(window, null);
	}

	public static boolean launch(IWorkbenchWindow window,
			IProgressMonitor monitor) {

		// if you open a java file without any project,
		// it will be FileStoreEditorInput and should not be supported now.
		IProject project = ProjectUtil.getProject(window);
		if (project != null) {

			ILaunchConfiguration launch = RunJettyRunLaunchConfigurationUtil
					.findLaunchConfiguration(project.getName());
			if (launch == null) {
				DebugUITools.openLaunchConfigurationDialogOnGroup(
						window.getShell(), new StructuredSelection(project),
						IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
				return false;
			} else {

				if (RunJettyRunLaunchConfigurationUtil.validation(launch)) {
					if (monitor != null)
						try {
							launch.launch(ILaunchManager.DEBUG_MODE, monitor);
						} catch (CoreException e) {
							return false;
						}
					else
						DebugUITools.launch(launch, ILaunchManager.DEBUG_MODE);
					return true;
				} else {
					DebugUITools.openLaunchConfigurationDialogOnGroup(
							window.getShell(), new StructuredSelection(launch),
							IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
					return false;
				}

			}

		} else {
			MessageDialog.openError(window.getShell(), "Run Jetty Run Plug-in",
					" you can't run RJR without any project. ");
			return false;
		}

	}
}
