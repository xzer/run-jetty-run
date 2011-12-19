package runjettyrun.tabs;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import runjettyrun.Plugin;
import runjettyrun.tabs.action.AddClassFolderAction;
import runjettyrun.tabs.action.AddExternalFileAction;
import runjettyrun.tabs.action.AddExternalFolderAction;
import runjettyrun.tabs.action.AddFolderAction;
import runjettyrun.tabs.action.RemoveAction;
import runjettyrun.tabs.action.RestoreDefaultEntriesAction;
import runjettyrun.tabs.action.RestoreDefaultSelectionAction;
import runjettyrun.tabs.action.RuntimeClasspathAction;
import runjettyrun.tabs.classpath.UserClassesClasspathModel;
import runjettyrun.utils.RunJettyRunLaunchConfigurationUtil;

public class ScanFolderTab extends AbstractClasspathTab {
	private Button fEnableScannerbox;
	private Button fEnableIgnoreClassWhenDebugging;
	private Text fScanText;
	private UpdateModfiyListener _updatedListener = new UpdateModfiyListener();

	public ScanFolderTab() {
		super("sourceScan", "Source Monitor List");
	}

	public String getCustomAttributeName() {
		return Plugin.ATTR_CUSTOM_SCAN_FOLDER;
	}

	public String getNonCheckedAttributeName() {
		return Plugin.ATTR_SCAN_FOLDER_NON_CHECKED;
	}

	public String getHeader() {
		return "A collection for resource watch list , will restart server if the resource in watch list changed.(Including all sub-folder and files.)" ;
	}

	public void createHeaderControl(Composite parent){
		Font font = parent.getFont();
		/*
		 * ---------------------------------------------------------------------
		 */

		fEnableScannerbox = createCheckButton(parent, "Enable Scanner");
		fEnableScannerbox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				fScanText.setEnabled(fEnableScannerbox.getSelection());
				updateLaunchConfigurationDialog();
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		/*
		 * ---------------------------------------------------------------------
		 */
		new Label(parent, SWT.LEFT).setText("Scan Interval");

		/*
		 * ---------------------------------------------------------------------
		 */
		fScanText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		fScanText.addModifyListener(_updatedListener);

		fScanText.setLayoutData(createHFillGridData(1, -1));
		fScanText.setFont(font);
		fScanText.setTextLimit(5);

		/*
		 * ---------------------------------------------------------------------
		 */
		new Label(parent, SWT.LEFT).setText(" seconds");
		/*
		 * ---------------------------------------------------------------------
		 */


		fEnableIgnoreClassWhenDebugging = createCheckButton(parent, "Ignore .class file changes when run in Debug Mode.");
		fEnableIgnoreClassWhenDebugging.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		{
			GridData data = new GridData();
			data.horizontalSpan =4 ;
			data.horizontalAlignment = SWT.LEFT;
			fEnableIgnoreClassWhenDebugging.setLayoutData(data);
		}

	}
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		super.performApply(configuration);

		configuration.setAttribute(Plugin.ATTR_SCANINTERVALSECONDS,
				fScanText.getText());

		configuration.setAttribute(Plugin.ATTR_ENABLE_SCANNER,
				fEnableScannerbox.getSelection());

		configuration.setAttribute(Plugin.ATTR_IGNORE_SCAN_CLASS_WHEN_DEBUG_MODE, fEnableIgnoreClassWhenDebugging.getSelection());
	}

	public boolean isValid(ILaunchConfiguration launchConfig) {
		boolean valid =  super.isValid(launchConfig);
		if(!valid ) {
			return false;
		}

		if (fEnableScannerbox.getSelection()) {
			String scan = fScanText.getText().trim();

			if (scan.length() == 0) {
				setErrorMessage("Must specify at least one scan interval seconds");
				return false;
			}
			if (isInvalidScan(scan))
				return false;
		}

		return true;
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);

		try{

			fEnableIgnoreClassWhenDebugging.setSelection(configuration.getAttribute(Plugin.ATTR_IGNORE_SCAN_CLASS_WHEN_DEBUG_MODE, true));
			fScanText.setText(configuration.getAttribute(
					Plugin.ATTR_SCANINTERVALSECONDS, ""));

			fEnableScannerbox.setSelection(configuration.getAttribute(
					Plugin.ATTR_ENABLE_SCANNER, true));
			fScanText.setEnabled(fEnableScannerbox.getSelection());
		} catch (CoreException e) {
			Plugin.logError(e);
		}
	}
	public UserClassesClasspathModel createClasspathModel(
			ILaunchConfiguration configuration) throws Exception {
		UserClassesClasspathModel theModel = new UserClassesClasspathModel("Project Scan Folders","Custom Scan Folder and Files");
		List<IRuntimeClasspathEntry> entries = getClasspathProvider().getDefaultScanList(configuration);
		for (IRuntimeClasspathEntry entry:entries) {
			switch (entry.getClasspathProperty()) {
				case IRuntimeClasspathEntry.USER_CLASSES:
					theModel.addEntry(UserClassesClasspathModel.USER, entry);
					break;
			}
		}

		IRuntimeClasspathEntry[] customentries = getClasspathProvider().computeUnresolvedCustomClasspath(
				configuration, getCustomAttributeName());

		for (int i = 0; i < customentries.length; i++) {
			theModel.addEntry(UserClassesClasspathModel.CUSTOM, customentries[i]);
		}

		return theModel;

	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		super.setDefaults(configuration);
		configuration.setAttribute(Plugin.ATTR_IGNORE_SCAN_CLASS_WHEN_DEBUG_MODE, true);

	}

	protected void createPathButtons(Composite pathButtonComp) {
		createButton(pathButtonComp, new RemoveAction(fClasspathViewer));
		createButton(pathButtonComp, new AddFolderAction(fClasspathViewer));
		createButton(pathButtonComp, new AddClassFolderAction(fClasspathViewer));

		createButton(pathButtonComp, new AddExternalFileAction(fClasspathViewer,DIALOG_SETTINGS_PREFIX));
		createButton(pathButtonComp, new AddExternalFolderAction(fClasspathViewer, DIALOG_SETTINGS_PREFIX));

		RuntimeClasspathAction restoreSelectionAction = new RestoreDefaultSelectionAction(
				fClasspathViewer, this, this.getNonCheckedAttributeName());
		createButton(pathButtonComp, restoreSelectionAction);
		restoreSelectionAction.setEnabled(true);


		RuntimeClasspathAction action =  new RestoreDefaultEntriesAction(fClasspathViewer,
				this,this.getCustomAttributeName());

		createButton(pathButtonComp, action);
		action.setEnabled(true);
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


	private boolean isInvalidScan(String s) {
		boolean res = RunJettyRunLaunchConfigurationUtil.isInvalidPort(s);
		if (res)
			setErrorMessage(MessageFormat.format(
					"Not a valid scan number: {0}", s));
		return res;
	}

}
