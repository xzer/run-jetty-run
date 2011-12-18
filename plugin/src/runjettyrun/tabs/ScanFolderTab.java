package runjettyrun.tabs;

import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.swt.widgets.Composite;

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

public class ScanFolderTab extends AbstractClasspathTab {

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


}
