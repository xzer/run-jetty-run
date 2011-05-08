package runjettyrun.launchshortcut;

import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.launcher.LauncherMessages;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;

import runjettyrun.Plugin;
import runjettyrun.RunJettyRunTab;
import runjettyrun.utils.ProjectUtil;

public class RunJettyRunLaunchShortcut implements ILaunchShortcut2 {

	public void launch(ISelection selection, String mode) {
		IResource ir = getLaunchableResource(selection);
		launch(ir,mode);
	}

	public void launch(IEditorPart editor, String mode) {
		launch(getLaunchableResource(editor),mode);
	}
	protected ILaunchConfiguration findLaunchConfiguration(IResource type) {
		if(type == null ) return null;
		IProject proj =  type.getProject();
		if(proj == null) return null;

		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().
				getLaunchManager().getLaunchConfigurations();
			String typeProjectName = proj.getName();
			for (int i = 0; i < configs.length; i++) {
				ILaunchConfiguration config = configs[i];
				if(config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "")
						.equals(typeProjectName))
						return config;
			}
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
		return null;
	}

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	public ILaunchConfigurationType getConfigurationType(){
		//TODO change the magic string to variable
		return getLaunchManager().getLaunchConfigurationType("RunJettyRunWebApp");
	}
	public ILaunchConfiguration createConfiguration(IResource type){
		if( type == null ) return null;

		ILaunchConfiguration config = null;
		ILaunchConfigurationWorkingCopy wc = null;
		try {
			ILaunchConfigurationType configType = getConfigurationType();

			if(RunJettyRunTab.isWebappProject(type.getProject())){
				String launchConfigName = getLaunchManager().
					generateLaunchConfigurationName(type.getProject().getName());

				wc = configType.newInstance(null, launchConfigName);
				RunJettyRunTab.initDefaultConfiguration(wc, type.getProject(), launchConfigName);
				//set mapped resource , let next time we could execute this directly from menuitem.
				wc.setMappedResources(new IResource[] {type});
				config = wc.doSave();
			}else{
				showError("Project is not a regular webapp project (missing WEB-INF\\web.xml");
			}
		} catch (CoreException exception) {
			showError( exception.getStatus().getMessage());
		}
		return config;
	}

	private void showError(String message){
		MessageDialog.openError(getActiveShell(),"Error when startup Jetty Applciation",
				message);
	}
	private Shell getActiveShell(){
		IWorkbenchWindow win = Plugin.getDefault().getWorkbench().getActiveWorkbenchWindow();

		if(win ==null) return null;

		return win.getShell();
	}

	public void launch(IResource ir,String mode){
		ILaunchConfiguration config = findLaunchConfiguration(ir);
		if (config == null) {
			config = createConfiguration(ir);
		}
		if (config != null) {
			DebugUITools.launch(config,mode);
		}
	}

	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		ILaunchConfiguration launchconf = findLaunchConfiguration(getLaunchableResource(selection));
		if(launchconf == null) return null;
		return new ILaunchConfiguration[]{launchconf};
	}

	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		ILaunchConfiguration launchconf = findLaunchConfiguration(getLaunchableResource(editorpart));
		if(launchconf == null) return null;
		return new ILaunchConfiguration[]{launchconf};
	}

	public IResource getLaunchableResource(ISelection selection) {
		return getLaunchableResource(ProjectUtil.getSelectedResource(selection));
	}

	public IResource getLaunchableResource(IEditorPart editorpart) {
		return getLaunchableResource(ProjectUtil.getFile(editorpart
				.getEditorInput()));
	}

	private IResource getLaunchableResource(IResource ir) {
		if (ir == null )
			return null;

		return ir;
	}

}
