package runjettyrun;

import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Launch tab for the RunJettyRun plugin.
 * 
 * @author hillenius
 */
public class RunJettyRunTab extends JavaLaunchTab {

	private static abstract class ButtonListener implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	private Text fProjText;

	private Text fContextText;

	private Text fWebAppDirText;

	private Text fPortText;

	private Button fProjButton;

	private Button fWebappDirButton;

	/**
	 * Construct.
	 */
	public RunJettyRunTab() {
	}

	public void createControl(Composite parent) {

		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		comp.setFont(parent.getFont());
		GridData gd = new GridData(1);
		gd.horizontalSpan = GridData.FILL_BOTH;
		comp.setLayoutData(gd);
		((GridLayout) comp.getLayout()).verticalSpacing = 0;
		createProjectEditor(comp);
		createVerticalSpacer(comp, 1);
		createJettyOptionsEditor(comp);
		createVerticalSpacer(comp, 1);
		setControl(comp);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
		// IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);
	}

	/**
	 * Creates the widgets for specifying a main type.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private void createProjectEditor(Composite parent) {
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		group.setText("Project");
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);
		fProjText = new Text(group, SWT.SINGLE | SWT.BORDER);
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		fProjText.setLayoutData(gd);
		fProjText.setFont(font);
		fProjText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		fProjButton = createPushButton(group, "&Browse...", null);
		fProjButton.addSelectionListener(new ButtonListener() {

			public void widgetSelected(SelectionEvent e) {
				handleProjectButtonSelected();
			}
		});
	}

	/**
	 * Creates the widgets for specifying the directory, context and port for
	 * the web application.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private void createJettyOptionsEditor(Composite parent) {
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		group.setText("Web Application/ Jetty");
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		group.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		group.setLayout(layout);
		group.setFont(font);

		new Label(group, SWT.LEFT).setText("Context");
		fContextText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fContextText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		fContextText.setLayoutData(gd);
		fContextText.setFont(font);

		new Label(group, SWT.LEFT).setText("Port");
		fPortText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fPortText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		gd = new GridData();
		fPortText.setLayoutData(gd);
		fPortText.setFont(font);

		new Label(group, SWT.LEFT).setText("WebApp dir");
		fWebAppDirText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fWebAppDirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalSpan = 2;
		fWebAppDirText.setLayoutData(gd);
		fWebAppDirText.setFont(font);
		fWebappDirButton = createPushButton(group, "&Browse...", null);
		fWebappDirButton.addSelectionListener(new ButtonListener() {

			public void widgetSelected(SelectionEvent e) {
				chooseWebappDir();
			}
		});
		fWebappDirButton.setEnabled(false);
	}

	@Override
	public Image getImage() {
		return Plugin.getJettyIcon();
	}

	@Override
	public String getMessage() {
		return "Create a configuration to launch a web application with Jetty.";
	}

	public String getName() {
		return "Jetty";
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			fProjText
					.setText(configuration.getAttribute(ATTR_PROJECT_NAME, ""));
			fContextText.setText(configuration.getAttribute(
					Plugin.ATTR_CONTEXT, ""));
			fWebAppDirText.setText(configuration.getAttribute(
					Plugin.ATTR_WEBAPPDIR, ""));
			fPortText.setText(configuration.getAttribute(Plugin.ATTR_PORT, ""));
		} catch (CoreException e) {
			Plugin.logError(e);
		}
	}

	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);

		String projectName = fProjText.getText().trim();
		IProject project = null;
		if (projectName.length() > 0) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IStatus status = workspace.validateName(projectName,
					IResource.PROJECT);
			if (status.isOK()) {
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(
						projectName);
				if (!project.exists()) {
					setErrorMessage(MessageFormat.format(
							"Project {0} does not exist", projectName));
					fWebappDirButton.setEnabled(false);
					return false;
				}
				if (!project.isOpen()) {
					setErrorMessage(MessageFormat.format(
							"Project {0} is closed", projectName));
					fWebappDirButton.setEnabled(false);
					return false;
				}
			} else {
				setErrorMessage(MessageFormat.format(
						"Illegal project name: {0}", status.getMessage()));
				fWebappDirButton.setEnabled(false);
				return false;
			}
			fWebappDirButton.setEnabled(true);
		} else {
			setErrorMessage("No project selected");
			return false;
		}
		String directory = fWebAppDirText.getText().trim();
		if (!"".equals(directory.trim())) {
			IFolder folder = project.getFolder(directory);
			if (!folder.exists()) {
				setErrorMessage(MessageFormat.format(
						"Folder {0} does not exist in project {1}", directory,
						project.getName()));
				return false;
			}
			IFile file = project.getFile(new Path(directory
					+ "/WEB-INF/web.xml"));
			if (!file.exists()) {
				setErrorMessage(MessageFormat
						.format(
								"Directoy {0} does not contain WEB-INF/web.xml; it is not a valid web application directory",
								directory));
				return false;
			}
		} else {
			setErrorMessage("Web application directory is not set");
			return false;
		}

		return true;
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_PROJECT_NAME, fProjText.getText());
		configuration.setAttribute(Plugin.ATTR_CONTEXT, fContextText.getText());
		configuration.setAttribute(Plugin.ATTR_WEBAPPDIR, fWebAppDirText
				.getText());
		configuration.setAttribute(Plugin.ATTR_PORT, fPortText.getText());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

		IJavaElement javaElement = getContext();
		if (javaElement != null) {
			initializeJavaProject(javaElement, configuration);
		} else {
			configuration.setAttribute(ATTR_PROJECT_NAME, "");
		}

		configuration.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME,
				Plugin.BOOTSTRAP_CLASS_NAME);

		// set the class path provider so that Jetty and the bootstrap jar are
		// added to the run time class path. Value has to be the same as the one
		// defined for the extension point
		configuration.setAttribute(
				IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER,
				"RunJettyRunWebAppClassPathProvider");

		// get the name for this launch configuration
		String launchConfigName = "";
		try {
			// try to base the launch config name on the current project
			launchConfigName = configuration.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
		} catch (CoreException e) {
			// ignore
		}
		if (launchConfigName == null || launchConfigName.length() == 0) {
			// if no project name was found, base on a default name
			launchConfigName = "Jetty Webapp";
		}
		// generate an unique name (e.g. myproject(2))
		launchConfigName = getLaunchConfigurationDialog().generateName(
				launchConfigName);
		configuration.rename(launchConfigName); // and rename the config

		configuration.setAttribute(Plugin.ATTR_CONTEXT, "/");
		configuration.setAttribute(Plugin.ATTR_WEBAPPDIR, "src/main/webapp");
		configuration.setAttribute(Plugin.ATTR_PORT, "8080");
	}

	private IJavaProject chooseJavaProject() {
		ILabelProvider labelProvider = new JavaElementLabelProvider(
				JavaElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), labelProvider);
		dialog.setTitle("Project Selection");
		dialog.setMessage("Select a project to constrain your search.");
		try {
			dialog
					.setElements(JavaCore.create(
							ResourcesPlugin.getWorkspace().getRoot())
							.getJavaProjects());
		} catch (JavaModelException jme) {
			Plugin.logError(jme);
		}

		IJavaProject javaProject = null;
		String projectName = fProjText.getText().trim();
		if (projectName.length() > 0) {
			javaProject = JavaCore.create(getWorkspaceRoot()).getJavaProject(
					projectName);
		}
		if (javaProject != null) {
			dialog.setInitialSelections(new Object[] { javaProject });
		}
		if (dialog.open() == Window.OK) {
			return (IJavaProject) dialog.getFirstResult();
		}
		return null;
	}

	private void chooseWebappDir() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				fProjText.getText());
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), project, false, "Select Web Application Directory");
		dialog.setTitle("Folder Selection");
		if (project != null) {
			IPath path = project.getFullPath();
			dialog.setInitialSelections(new Object[] { path });
		}
		dialog.showClosedProjects(false);
		dialog.open();
		Object[] results = dialog.getResult();
		if ((results != null) && (results.length > 0)
				&& (results[0] instanceof IPath)) {
			IPath path = (IPath) results[0];
			path = path.removeFirstSegments(1);
			String containerName = path.makeRelative().toString();
			fWebAppDirText.setText(containerName);
		}
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	private void handleProjectButtonSelected() {
		IJavaProject project = chooseJavaProject();
		if (project == null) {
			return;
		}
		String projectName = project.getElementName();
		fProjText.setText(projectName);
	}
}
