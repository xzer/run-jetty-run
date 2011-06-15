/*
 * $Id$
 * $HeadURL$
 *
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package runjettyrun;

import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IContainer;
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
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.part.FileEditorInput;

import runjettyrun.extensions.IJettyPackageProvider;
import runjettyrun.utils.ProjectUtil;
import runjettyrun.utils.RunJettyRunLaunchConfigurationUtil;
import runjettyrun.utils.UIUtil;

/**
 * Launch tab for the RunJettyRun plugin.
 *
 * @author hillenius, James Synge
 */
@SuppressWarnings("restriction")
public class RunJettyRunTab extends JavaLaunchTab {

	private UpdateModfiyListener _updatedListener = new UpdateModfiyListener();

	private Text fProjText;

	private Text fPortText;

	private Text fSSLPortText;

	private Text fKeystoreText;

	private Text fKeyPasswordText;

	private Text fPasswordText;

	private Text fContextText;

	private Text fScanText;

	private Text fWebAppDirText;

	private Button fProjButton;

	private Button fEnableSSLbox;

	private Button fEnableNeedClientAuth;

	private Button fKeystoreButton;

	private Button fWebappDirButton;

	private Button fWebappScanButton;

	private Button fEnablebox;

	private Button fEnableMavenDisableTestClassesBox;

	private Combo fJettyVersion;

	private Label jettyVersionNote;

	private Button fEnableParentLoadPriorityBox;

	private Group mavenGroup = null;

	private boolean isMavenProject = false;

	private Button fEnableJNDI;

	/**
	 * Construct.
	 */
	public RunJettyRunTab() {
	}

	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setFont(parent.getFont());

		GridData gd = new GridData(1);
		gd.horizontalSpan = GridData.FILL_BOTH;
		comp.setLayoutData(gd);

		GridLayout layout = new GridLayout(1, false);
		layout.verticalSpacing = 0;
		comp.setLayout(layout);

		createJettyVersionSelector(comp);
		createVerticalSpacer(comp, 1);
		createProjectEditor(comp);
		createVerticalSpacer(comp, 1);
		createPortEditor(comp);
		createVerticalSpacer(comp, 1);
		createJettyOptionsEditor(comp);
		createVerticalSpacer(comp, 1);
		createMavenEditor(comp);
		createVerticalSpacer(comp, 1);
		createVerticalSpacer(comp, 1);
		setControl(comp);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
		// IJavaDebugHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_MAIN_TAB);

		return;
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
		group.setLayoutData(createHFillGridData());

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		group.setLayout(layout);
		group.setFont(font);
		fProjText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fProjText.setLayoutData(createHFillGridData());
		fProjText.setFont(font);
		fProjText.addModifyListener(_updatedListener);
		fProjButton = createPushButton(group, "&Browse...", null);
		fProjButton.addSelectionListener(new ButtonListener() {

			public void widgetSelected(SelectionEvent e) {
				handleProjectButtonSelected();
			}
		});
	}

	/**
	 *
	 * @param parent
	 */
	private void createJettyVersionSelector(Composite parent) {

		IJettyPackageProvider[] providers = Plugin.getDefault().getProviders();

		if (providers.length == 1) {
			return;
		}

		Font font = parent.getFont();

		/*
		 * ---------------------------------------------------------------------
		 */

		Group jettyGroup = new Group(parent, SWT.NONE);
		jettyGroup.setText("RunJettyRun Version Support");
		jettyGroup.setLayoutData(createHFillGridData());
		{
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			jettyGroup.setLayout(layout);
		}
		jettyGroup.setFont(font);

		/*
		 * ---------------------------------------------------------------------
		 */

		new Label(jettyGroup, SWT.LEFT).setText("Select a Jetty Version:");

		fJettyVersion = new Combo(jettyGroup, SWT.DROP_DOWN | SWT.READ_ONLY| SWT.BORDER);
		{
			GridData gd = new GridData();
			gd.horizontalAlignment = SWT.LEFT;
			fJettyVersion.setLayoutData(gd);
		}

		for (IJettyPackageProvider provider : providers) {
			fJettyVersion.add(provider.getJettyVersion());
		}

		jettyVersionNote = new Label(jettyGroup, SWT.LEFT);
		jettyVersionNote.setText("Note: If you are running jsp page with Jetty8 Bundle , "+
				" your project classpath's JRE need to be configurated as a JDK .");
		{
			GridData gd = new GridData();
			gd.horizontalAlignment = SWT.LEFT;
			gd.horizontalSpan = 2;
			jettyVersionNote.setLayoutData(gd);
		}
		jettyVersionNote.setVisible(false);

		fJettyVersion.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
				if(fJettyVersion.getText().indexOf("8.0.0")!=-1){
					jettyVersionNote.setVisible(true);
				}else{
					jettyVersionNote.setVisible(false);
				}

			}

			public void widgetDefaultSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
				if(fJettyVersion.getText().indexOf("8.0.0")!=-1){
					jettyVersionNote.setVisible(true);
				}else{
					jettyVersionNote.setVisible(false);
				}
			}
		});

	}

	/**
	 * create a group for M2E config.
	 *
	 * @param parent
	 *            the parent composite
	 */
	private void createMavenEditor(Composite parent) {
		Font font = parent.getFont();

		/*
		 * ---------------------------------------------------------------------
		 */

		mavenGroup = new Group(parent, SWT.NONE);
		mavenGroup.setVisible(isMavenProject);
		mavenGroup.setText("RunJettyRun Support for M2Eclipse");
		mavenGroup.setLayoutData(createHFillGridData());
		{
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			mavenGroup.setLayout(layout);
		}
		mavenGroup.setFont(font);

		/*
		 * ---------------------------------------------------------------------
		 */

		fEnableMavenDisableTestClassesBox = createCheckButton(mavenGroup,
				"Exclude test-classes for maven");
		{
			GridData gd = new GridData();
			gd.horizontalAlignment = SWT.LEFT;
			fEnableMavenDisableTestClassesBox.setLayoutData(gd);
		}
		// update configuration directly when user select it.
		fEnableMavenDisableTestClassesBox
				.addSelectionListener(new ButtonListener() {
					public void widgetSelected(SelectionEvent e) {
						updateLaunchConfigurationDialog();
					}
				});

	}

	private GridData createHFillGridData() {
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		return gd;
	}

	private GridData createHFillGridData(int span, int position) {
		// gd.horizontalAlignment = SWT.FILL
		GridData gd = createHFillGridData();
		if (position != -1)
			gd.horizontalAlignment = position;
		if (span != -1)
			gd.horizontalSpan = span;

		return gd;
	}

	/**
	 * Creates the widgets for specifying the ports:
	 *
	 * HTTP Port: Text....... HTTPS Port: Text....... Keystore:
	 * Text.................. Browse Button Store Password: Text.. Key Password:
	 * Text.....
	 *
	 * @param parent
	 *            the parent composite
	 */
	private void createPortEditor(Composite parent) {
		// Create group, container for widgets
		Font font = parent.getFont();
		Group group = new Group(parent, SWT.NONE);
		group.setText("Ports");
		group.setLayoutData(createHFillGridData());
		{
			GridLayout layout = new GridLayout();
			layout.numColumns = 5;
			group.setLayout(layout);
		}
		group.setFont(font);

		// HTTP and HTTPS ports

		/*
		 * ---------------------------------------------------------------------
		 */

		new Label(group, SWT.LEFT).setText("HTTP");

		/*
		 * ---------------------------------------------------------------------
		 */

		fPortText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fPortText.addModifyListener(_updatedListener);
		fPortText.setLayoutData(createHFillGridData());
		fPortText.setFont(font);
		fPortText.setTextLimit(5);
		setWidthForSampleText(fPortText, " 65535 ");

		/*
		 * ---------------------------------------------------------------------
		 */

		fEnableSSLbox = createCheckButton(group, "HTTPS");
		fEnableSSLbox.addSelectionListener(new ButtonListener() {

			public void widgetSelected(SelectionEvent e) {
				setSSLSettingEnabled(fEnableSSLbox.getSelection());
				updateLaunchConfigurationDialog();
			}
		});

		{
			GridData gd = new GridData();
			gd.horizontalAlignment = SWT.RIGHT;
			fEnableSSLbox.setLayoutData(gd);
		}

		/*
		 * ---------------------------------------------------------------------
		 */

		fSSLPortText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fSSLPortText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				boolean isNotEmpty = fSSLPortText.getText().trim().length() != 0;
				setSSLSettingEnabled(isNotEmpty);
				updateLaunchConfigurationDialog();
			}
		});
		fSSLPortText.setLayoutData(createHFillGridData());
		fSSLPortText.setFont(font);

		/*
		 * SslSocketConnector
		 */
		/*
		 * ---------------------------------------------------------------------
		 */

		fEnableNeedClientAuth = createCheckButton(group, "NeedClientAuth");
		fEnableNeedClientAuth.addSelectionListener(new ButtonListener() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		/*
		 * ---------------------------------------------------------------------
		 */
		// keystore
		new Label(group, SWT.LEFT).setText("Keystore");
		/*
		 * ---------------------------------------------------------------------
		 */

		fKeystoreText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fKeystoreText.addModifyListener(_updatedListener);
		fKeystoreText.setLayoutData(createHFillGridData(3, -1));
		fKeystoreText.setFont(font);
		fKeystoreText.setEnabled(false);

		/*
		 * ---------------------------------------------------------------------
		 */

		fKeystoreButton = createPushButton(group, "&Browse...", null);
		fKeystoreButton.addSelectionListener(new ButtonListener() {

			public void widgetSelected(SelectionEvent e) {
				handleBrowseFileSystem();
			}
		});
		fKeystoreButton.setEnabled(false);
		fKeystoreButton.setLayoutData(new GridData());
		/*
		 * ---------------------------------------------------------------------
		 */

		// Password and Key Password (not sure exactly how used by keystore)

		new Label(group, SWT.LEFT).setText("Password");
		/*
		 * ---------------------------------------------------------------------
		 */

		fPasswordText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fPasswordText.addModifyListener(_updatedListener);
		fPasswordText.setLayoutData(createHFillGridData());
		fPasswordText.setFont(font);
		fPasswordText.setEnabled(false);

		/*
		 * ---------------------------------------------------------------------
		 */
		new Label(group, SWT.LEFT).setText("Key Password");

		/*
		 * ---------------------------------------------------------------------
		 */
		fKeyPasswordText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fKeyPasswordText.addModifyListener(_updatedListener);
		fKeyPasswordText.setLayoutData(createHFillGridData());
		fKeyPasswordText.setFont(font);
		fKeyPasswordText.setEnabled(false);
		/*
		 * ---------------------------------------------------------------------
		 */
		return;
	}

	private void setWidthForSampleText(Text control, String sampleText) {
		GC gc = new GC(control);
		try {
			Point sampleSize = gc.textExtent(sampleText);
			Point currentSize = control.getSize();
			sampleSize.y = currentSize.y;
			control.setSize(sampleSize);
			return;
		} finally {
			gc.dispose();
		}
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
		group.setText("Web Application");
		group.setLayoutData(createHFillGridData());
		{
			GridLayout layout = new GridLayout();
			layout.numColumns = 6;
			group.setLayout(layout);
		}
		group.setFont(font);


		/*
		 * ---------------------------------------------------------------------
		 */

		// Row 1: "Context", Text field (2 columns)
		new Label(group, SWT.LEFT).setText("Context");

		fContextText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fContextText.addModifyListener(_updatedListener);
		fContextText.setLayoutData(createHFillGridData(5, -1));
		fContextText.setFont(font);

		/*
		 * ---------------------------------------------------------------------
		 */

		// Row 2: "WebApp dir", Text field, "Browse..." Button
		new Label(group, SWT.LEFT).setText("WebApp dir");

		/*
		 * ---------------------------------------------------------------------
		 */

		fWebAppDirText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fWebAppDirText.addModifyListener(_updatedListener);
		fWebAppDirText.setLayoutData(createHFillGridData(3, -1));
		fWebAppDirText.setFont(font);

		/*
		 * ---------------------------------------------------------------------
		 */

		fWebappDirButton = createPushButton(group, "&Browse...", null);
		fWebappDirButton.addSelectionListener(new ButtonListener() {

			public void widgetSelected(SelectionEvent e) {
				chooseWebappDir();
			}
		});
		fWebappDirButton.setEnabled(false);
		fWebappDirButton.setLayoutData(new GridData());

		/*
		 * ---------------------------------------------------------------------
		 */
		fWebappScanButton = createPushButton(group, "&Scan...", null);
		fWebappScanButton.addSelectionListener(new ButtonListener() {

			public void widgetSelected(SelectionEvent e) {
				fWebAppDirText.setText(scanWebAppDir(fProjText.getText()));
			}
		});
		fWebappScanButton.setEnabled(false);
		fWebappScanButton.setLayoutData(new GridData());

		/*
		 * ---------------------------------------------------------------------
		 */
		// Row 3: Scan interval seconds
		new Label(group, SWT.LEFT).setText("Scan Interval Seconds");

		/*
		 * ---------------------------------------------------------------------
		 */
		fScanText = new Text(group, SWT.SINGLE | SWT.BORDER);
		fScanText.addModifyListener(_updatedListener);

		fScanText.setLayoutData(createHFillGridData(3, -1));
		fScanText.setFont(font);
		fScanText.setTextLimit(5);

		/*
		 * ---------------------------------------------------------------------
		 */

		fEnablebox = createCheckButton(group, "Enable Scanner");
		fEnablebox.addSelectionListener(new ButtonListener() {

			public void widgetSelected(SelectionEvent e) {
				fScanText.setEnabled(fEnablebox.getSelection());
				updateLaunchConfigurationDialog();
			}
		});
		{
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			fEnablebox.setLayoutData(gd);
		}

		/*
		 * ---------------------------------------------------------------------
		 */

		// Row4 Parent Loader Priority
		fEnableParentLoadPriorityBox = createCheckButton(group,
				"ParentLoadPriority");

		{
			GridData gd = new GridData();
			gd.horizontalAlignment = SWT.LEFT;
			fEnableParentLoadPriorityBox.setLayoutData(gd);
		}
		// update configuration directly when user select it.
		fEnableParentLoadPriorityBox.addSelectionListener(new ButtonListener() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});


		/*
		 * ---------------------------------------------------------------------
		 */

		UIUtil.createLink(group, SWT.NONE,
				"<a href=\"http://communitymapbuilder.org/display/JETTY/Classloading\">(?)</a>");

		/*
		 * ---------------------------------------------------------------------
		 */

		fEnableJNDI = createCheckButton(group, "JNDI Support");
		{
			GridData gd = new GridData();
			gd.horizontalAlignment = SWT.LEFT;
			fEnableJNDI.setLayoutData(gd);
		}
		// update configuration directly when user select it.
		fEnableJNDI.addSelectionListener(new ButtonListener() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		/*
		 * ---------------------------------------------------------------------
		 */

		Link systemProperties = UIUtil
				.createLink(
						group,
						SWT.NONE,
						"...You could set "
								+ "<a href=\"http://communitymapbuilder.org/display/JETTY/SystemProperties\">"
								+ "more control </a> in VM argument.(-Dkey=value) ");
		systemProperties.setLayoutData(createHFillGridData(6, SWT.RIGHT));

		return;
	}

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

	protected void setSSLSettingEnabled(boolean b) {
		fSSLPortText.setEnabled(b);
		fKeystoreText.setEnabled(b);
		fKeystoreButton.setEnabled(b);
		fPasswordText.setEnabled(b);
		fKeyPasswordText.setEnabled(b);
		fEnableNeedClientAuth.setEnabled(b);
		return;
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);
		try {
			String projectname = configuration.getAttribute(ATTR_PROJECT_NAME,
					"");
			fProjText.setText(projectname);

			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectname);
			if (project != null) {
				isMavenProject = ProjectUtil.isMavenProject(project);
				if (mavenGroup != null)
					mavenGroup.setVisible(isMavenProject);
			}

			fPortText.setText(configuration.getAttribute(Plugin.ATTR_PORT, ""));

			fEnableSSLbox.setSelection(configuration.getAttribute(
					Plugin.ATTR_ENABLE_SSL, false));
			fEnableNeedClientAuth.setSelection(configuration.getAttribute(
					Plugin.ATTR_ENABLE_NEED_CLIENT_AUTH, false));

			fSSLPortText.setText(configuration.getAttribute(
					Plugin.ATTR_SSL_PORT, ""));
			fKeystoreText.setText(configuration.getAttribute(
					Plugin.ATTR_KEYSTORE, ""));
			fPasswordText.setText(configuration.getAttribute(Plugin.ATTR_PWD,
					""));
			fKeyPasswordText.setText(configuration.getAttribute(
					Plugin.ATTR_KEY_PWD, ""));

			fContextText.setText(configuration.getAttribute(
					Plugin.ATTR_CONTEXT, ""));

			fWebAppDirText.setText(configuration.getAttribute(
					Plugin.ATTR_WEBAPPDIR, ""));

			fScanText.setText(configuration.getAttribute(
					Plugin.ATTR_SCANINTERVALSECONDS, ""));
			fEnablebox.setSelection(configuration.getAttribute(
					Plugin.ATTR_ENABLE_SCANNER, true));
			fEnableJNDI.setSelection(configuration.getAttribute(
					Plugin.ATTR_ENABLE_JNDI, false));
			fScanText.setEnabled(fEnablebox.getSelection());

			fEnableMavenDisableTestClassesBox.setSelection(configuration
					.getAttribute(Plugin.ATTR_ENABLE_MAVEN_TEST_CLASSES, true));

			fEnableParentLoadPriorityBox.setSelection(configuration
					.getAttribute(Plugin.ATTR_ENABLE_PARENT_LOADER_PRIORITY,
							true));

			String ver = configuration.getAttribute(
					Plugin.ATTR_SELECTED_JETTY_VERSION, "");


			/*
			 * If provider's length = 1 , the fJettyVersion controll will not be created.
			 */
			if (fJettyVersion != null) {
				fJettyVersion.select(0);
				IJettyPackageProvider[] providers = Plugin.getDefault().getProviders();
				if (!"".equals(ver)) {
					int i = 0;
					for (IJettyPackageProvider provider : providers) {

						String proVer = provider.getJettyVersion();
						if (proVer != null && proVer.equals(ver)) {
							fJettyVersion.select(i);
							if(ver.indexOf("8.0.0")!=-1){
								jettyVersionNote.setVisible(true);
							}else{
								jettyVersionNote.setVisible(false);
							}
						}
						++i;
					}
				}
			}

			setSSLSettingEnabled(fEnableSSLbox.getSelection());
		} catch (CoreException e) {
			Plugin.logError(e);
		}
	}

	private static String[] DEFAULT_WEBAPP_DIR_SET = new String[] {
			"WebContent", "src/main/webapp" };

	/**
	 * TODO review this later , do we should check this in RunJettyRunTab ?
	 *
	 * @param proj
	 * @return
	 */
	public static boolean isWebappProject(IProject proj) {
		return !"".equals(detectDefaultWebappdir(proj));
	}

	public static String detectDefaultWebappdir(String projectName) {
		return detectDefaultWebappdir(getProject(projectName));
	}

	public static String detectDefaultWebappdir(IProject project) {
		if (project != null) {
			for (String path : DEFAULT_WEBAPP_DIR_SET) {
				IFile file = project
						.getFile(new Path(path + "/WEB-INF/web.xml"));
				if (file.exists()) {
					return path;
				}
			}
			return scanWebAppDir(project.getName());
		}

		return "";
	}

	private static String scanWebAppDir(String projectName) {
		IProject project = getProject(projectName);
		if (project != null) {

			IFolder pwebinf = project.getFolder(new Path("WEB-INF"));
			if (pwebinf.exists() && pwebinf.getFile("web.xml").exists()) {
				return "/";
			}

			try {
				IContainer webInf = scanWEBINF(project);
				if (webInf != null && webInf.exists()
						&& webInf.getFile(new Path("web.xml")).exists()) {
					return ((IFolder) webInf).getParent()
							.getProjectRelativePath().toString();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "";
	}

	private static IContainer scanWEBINF(IContainer container)
			throws CoreException {
		IResource[] resuorces = container.members();

		// TODO use accept instead and detect folder instead , and check
		// scanWebappDir can merge.
		IContainer result = null;
		for (IResource ir : resuorces) {
			if (ir.getType() == IResource.FOLDER) {

				if ("WEB-INF".equals(ir.getName())) {
					if (((IFolder) ir).getFile("web.xml").exists())
						return (IFolder) ir;
				} else {
					result = scanWEBINF((IFolder) ir);
					if (result != null)
						return (IFolder) result;
				}
			}
		}
		return null;
	}

	private static IProject getProject(String projectName) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = null;
		IStatus status = workspace.validateName(projectName, IResource.PROJECT);
		if (status.isOK()) {
			project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectName);
			if (!project.exists()) {
				return null;
			}
			if (!project.isOpen()) {
				return null;
			}
		}

		return project;

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
				project = ResourcesPlugin.getWorkspace().getRoot()
						.getProject(projectName);
				if (!project.exists()) {
					setErrorMessage(MessageFormat.format(
							"Project {0} does not exist", projectName));
					fWebappDirButton.setEnabled(false);
					fWebappScanButton.setEnabled(false);
					return false;
				}
				if (!project.isOpen()) {
					setErrorMessage(MessageFormat.format(
							"Project {0} is closed", projectName));
					fWebappDirButton.setEnabled(false);
					fWebappScanButton.setEnabled(false);
					return false;
				}
			} else {
				setErrorMessage(MessageFormat.format(
						"Illegal project name: {0}", status.getMessage()));
				fWebappDirButton.setEnabled(false);
				fWebappScanButton.setEnabled(false);
				return false;
			}
			fWebappDirButton.setEnabled(true);
			fWebappScanButton.setEnabled(true);
		} else {
			setErrorMessage("No project selected");
			return false;
		}


		String text = fContextText.getText();
		if(text.length() == 0){
			setErrorMessage("Context path can't be empty.");
			return false;
		}else if(!text.startsWith("/")){
			setErrorMessage("Context path have to start with /.");
			return false;
		}else if(text.length() != 1 && text.endsWith("/")){
			setErrorMessage("Context path can't end with / unless it's root.");
			return false;
		}

		String directory = fWebAppDirText.getText().trim();
		if (!"".equals(directory.trim())) {
			// means use project folder as webapp folder

			IContainer folder = null;
			if ("/".equals(directory))
				folder = project;
			else
				folder = project.getFolder(directory);

			if (!folder.exists()) {
				setErrorMessage(MessageFormat.format(
						"Folder {0} does not exist in project {1}", directory,
						project.getName()));
				return false;
			}

			IFile file = folder.getFile(new Path("WEB-INF/web.xml"));
			if (!file.exists()) {
				setErrorMessage(MessageFormat
						.format("Directory {0} does not contain WEB-INF/web.xml; it is not a valid web application directory",
								directory));
				return false;
			}
		} else {
			setErrorMessage("Web application directory is not set");
			return false;
		}

		String port = fPortText.getText().trim();
		String sslPort = fSSLPortText.getText().trim();
		if (port.length() == 0 && sslPort.length() == 0) {
			setErrorMessage("Must specify at least one port");
			return false;
		}
		if (isInvalidPort(port))
			return false;
		if (isInvalidPort(sslPort))
			return false;

		if (fEnablebox.getSelection()) {
			String scan = fScanText.getText().trim();

			if (scan.length() == 0) {
				setErrorMessage("Must specify at least one scan interval seconds");
				return false;
			}
			if (isInvalidScan(scan))
				return false;
		}

		if (fEnableSSLbox.getSelection()) {
			// Validate that we have the necessary key store info.
			String keystore = fKeystoreText.getText().trim();
			String keyPwd = fKeyPasswordText.getText().trim();
			String password = fPasswordText.getText().trim();
			if (keystore.length() == 0) {
				setErrorMessage("Keystore location is not set");
				return false;
			} else if (!new File(keystore).isFile()) {
				setErrorMessage(MessageFormat.format(
						"Keystore file {0} does not exist", keystore));
				return false;
			}
			if (keyPwd.length() == 0) {
				setErrorMessage("Key Password is not set");
				return false;
			}
			if (password.length() == 0) {
				setErrorMessage("Password is not set");
				return false;
			}
		}

		return true;
	}

	private boolean isInvalidPort(String s) {

		boolean res = RunJettyRunLaunchConfigurationUtil.isInvalidPort(s);
		if (res)
			setErrorMessage(MessageFormat.format(
					"Not a valid TCP port number: {0}", s));

		return res;
	}

	private boolean isInvalidScan(String s) {
		boolean res = RunJettyRunLaunchConfigurationUtil.isInvalidPort(s);
		if (res)
			setErrorMessage(MessageFormat.format(
					"Not a valid scan number: {0}", s));
		return res;
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_PROJECT_NAME, fProjText.getText());

		configuration.setAttribute(Plugin.ATTR_PORT, fPortText.getText());

		configuration
				.setAttribute(Plugin.ATTR_SSL_PORT, fSSLPortText.getText());

		configuration.setAttribute(Plugin.ATTR_ENABLE_SSL,
				fEnableSSLbox.getSelection());
		configuration.setAttribute(Plugin.ATTR_ENABLE_NEED_CLIENT_AUTH,
				fEnableNeedClientAuth.getSelection());

		configuration.setAttribute(Plugin.ATTR_ENABLE_JNDI,
				fEnableJNDI.getSelection());

		configuration.setAttribute(Plugin.ATTR_KEYSTORE,
				fKeystoreText.getText());
		configuration.setAttribute(Plugin.ATTR_PWD, fPasswordText.getText());
		configuration.setAttribute(Plugin.ATTR_KEY_PWD,
				fKeyPasswordText.getText());

		configuration.setAttribute(Plugin.ATTR_CONTEXT, fContextText.getText());
		configuration.setAttribute(Plugin.ATTR_WEBAPPDIR,
				fWebAppDirText.getText());
		configuration.setAttribute(Plugin.ATTR_SCANINTERVALSECONDS,
				fScanText.getText());

		configuration.setAttribute(Plugin.ATTR_ENABLE_SCANNER,
				fEnablebox.getSelection());

		configuration.setAttribute(Plugin.ATTR_ENABLE_PARENT_LOADER_PRIORITY,
				fEnableParentLoadPriorityBox.getSelection());


		/*
		 * when provider only one , it's not event inited for fJettyVersion.
		 */
		if(fJettyVersion != null){
			configuration.setAttribute(Plugin.ATTR_SELECTED_JETTY_VERSION,
				fJettyVersion.getText());
		}

	}

	private void initProejctInformation(
			ILaunchConfigurationWorkingCopy configuration) {

		// TonyQ: 2011/1/3
		// Here RJR Assume it will go through java element,
		// but if we are editing a xml file , ex.ZK's zul file.

		// It won't working for us , here we only want to got the project
		// information ,
		// so I add some handle for text selection and got the project
		// information.

		setConfigurationProejct(configuration, null);
		IJavaElement javaElement = getContext();
		if (javaElement != null)
			initializeJavaProject(javaElement, configuration);

		IWorkbenchPage page = JDIDebugUIPlugin.getActivePage();
		if (page != null) {

			FileEditorInput editorinput = null;
			try {
				editorinput = (FileEditorInput) page.getActiveEditor()
						.getEditorInput().getAdapter(FileEditorInput.class);
				if (editorinput != null) {
					try {
						setConfigurationProejct(configuration, editorinput
								.getFile().getProject());
					} catch (Exception e) {
					}
				}

			} catch (NullPointerException npe) {
				// for a bug with ActiveEditor is null. (means user not editing
				// any item)
				// if it's a NPE , we just skip it directly...since it's a
				// add-on.
			}
		}

	}

	public static void initDefaultConfiguration(
			ILaunchConfigurationWorkingCopy configuration, IProject proj,
			String launchConfigName) {
		setConfigurationProejct(configuration, proj);

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
		String projectName = proj.getName();

		configuration.rename(launchConfigName); // and rename the config

		configuration.setAttribute(Plugin.ATTR_PORT, "8080");
		configuration.setAttribute(Plugin.ATTR_SSL_PORT, "8443");

		File userHomeDir = new File(System.getProperty("user.home"));
		File keystoreFile = new File(userHomeDir, ".keystore");
		String keystore = keystoreFile.getAbsolutePath();

		configuration.setAttribute(Plugin.ATTR_KEYSTORE, keystore);
		configuration.setAttribute(Plugin.ATTR_PWD, "changeit");
		configuration.setAttribute(Plugin.ATTR_KEY_PWD, "changeit");

		configuration.setAttribute(Plugin.ATTR_CONTEXT, "/" + projectName);

		configuration.setAttribute(Plugin.ATTR_WEBAPPDIR,
				detectDefaultWebappdir(projectName));
		configuration.setAttribute(Plugin.ATTR_ENABLE_SSL, false);

		configuration.setAttribute(Plugin.ATTR_ENABLE_NEED_CLIENT_AUTH, false);

		configuration.setAttribute(Plugin.ATTR_SCANINTERVALSECONDS, "5");
		configuration.setAttribute(Plugin.ATTR_ENABLE_SCANNER, true);

		configuration.setAttribute(Plugin.ATTR_ENABLE_MAVEN_TEST_CLASSES, true);
		configuration.setAttribute(Plugin.ATTR_ENABLE_PARENT_LOADER_PRIORITY,
				true);

		return;
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		initProejctInformation(configuration);

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
		String projectName = "";
		try {
			// try to base the launch config name on the current project
			launchConfigName = configuration.getAttribute(
					IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");

			projectName = launchConfigName;
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

		configuration.setAttribute(Plugin.ATTR_PORT, "8080");
		configuration.setAttribute(Plugin.ATTR_SSL_PORT, "8443");

		File userHomeDir = new File(System.getProperty("user.home"));
		File keystoreFile = new File(userHomeDir, ".keystore");
		String keystore = keystoreFile.getAbsolutePath();

		configuration.setAttribute(Plugin.ATTR_KEYSTORE, keystore);
		configuration.setAttribute(Plugin.ATTR_PWD, "changeit");
		configuration.setAttribute(Plugin.ATTR_KEY_PWD, "changeit");

		configuration.setAttribute(Plugin.ATTR_CONTEXT, "/" + projectName);

		configuration.setAttribute(Plugin.ATTR_WEBAPPDIR,
				detectDefaultWebappdir(projectName));
		configuration.setAttribute(Plugin.ATTR_ENABLE_SSL, false);

		configuration.setAttribute(Plugin.ATTR_ENABLE_NEED_CLIENT_AUTH, false);

		configuration.setAttribute(Plugin.ATTR_SCANINTERVALSECONDS, "5");
		configuration.setAttribute(Plugin.ATTR_ENABLE_SCANNER, true);

		configuration.setAttribute(Plugin.ATTR_ENABLE_MAVEN_TEST_CLASSES, true);
		configuration.setAttribute(Plugin.ATTR_ENABLE_PARENT_LOADER_PRIORITY,
				true);

		return;
	}

	private IJavaProject chooseJavaProject() {
		ILabelProvider labelProvider = new JavaElementLabelProvider(
				JavaElementLabelProvider.SHOW_DEFAULT);
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(
				getShell(), labelProvider);
		dialog.setTitle("Project Selection");
		dialog.setMessage("Select a project to constrain your search.");
		try {
			dialog.setElements(JavaCore.create(
					ResourcesPlugin.getWorkspace().getRoot()).getJavaProjects());
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
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(fProjText.getText());
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

		if (project == null)
			return;

		isMavenProject = ProjectUtil.isMavenProject(project.getProject());
		if (mavenGroup != null)
			mavenGroup.setVisible(isMavenProject);

		String projectName = project.getElementName();
		fProjText.setText(projectName);
	}

	protected void handleBrowseFileSystem() {
		String current = fKeystoreText.getText();
		if (current == null || current.trim().equals("")) {
			String userHome = System.getProperty("user.home");
			String fileSeparator = System.getProperty("file.separator");
			current = userHome + fileSeparator + ".keystore";
		}
		FileDialog dialog = new FileDialog(getControl().getShell());
		dialog.setFilterExtensions(new String[] { "*.keystore", "*" }); //$NON-NLS-1$
		dialog.setFilterPath(fKeystoreText.getText());
		dialog.setText("Choose a keystore file");
		String res = dialog.open();
		if (res != null)
			fKeystoreText.setText(res);
	}

	private static void setConfigurationProejct(
			ILaunchConfigurationWorkingCopy configuration, IProject proj) {
		if (proj == null)
			configuration.setAttribute(ATTR_PROJECT_NAME, "");
		else
			configuration.setAttribute(ATTR_PROJECT_NAME, proj.getName());
	}

	private static abstract class ButtonListener implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	/**
	 * If it's modified , just update the configuration directly.
	 *
	 * @author TonyQ
	 *
	 */
	private class UpdateModfiyListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}
	}
}
