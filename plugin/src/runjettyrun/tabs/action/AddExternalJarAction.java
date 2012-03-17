/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package runjettyrun.tabs.action;


import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import runjettyrun.Plugin;
import runjettyrun.tabs.classpath.IClasspathViewer;

/**
 * Adds an external folder to the runtime class path.
 */
public class AddExternalJarAction extends RuntimeClasspathAction {

	private String prefix = "JavaClasspathTab";
	protected static final String LAST_PATH_SETTING = "LAST_PATH_SETTING"; //$NON-NLS-1$

	public AddExternalJarAction(IClasspathViewer viewer, String dialogSettingsPrefix) {
		super("Add E&xternal JARs...", viewer);
		this.prefix = dialogSettingsPrefix;
	}

	/**
	 * Prompts for a folder to add.
	 *
	 * @see IAction#run()
	 */
	public void run() {

		String lastUsedPath= getDialogSetting(LAST_PATH_SETTING);
		if (lastUsedPath == null) {
			lastUsedPath= ""; //$NON-NLS-1$
		}
		FileDialog dialog= new FileDialog(getShell(), SWT.MULTI);
		dialog.setFilterExtensions(new String[] {"*.jar;*.zip","*.*"}); //$NON-NLS-1$ //$NON-NLS-2$
		dialog.setText( "Jar Selection");
		dialog.setFilterPath(lastUsedPath);
		String res= dialog.open();
		if (res == null) {
			return;
		}
		String[] fileNames = dialog.getFileNames();
		int nChosen = fileNames.length;
		IPath filterPath= new Path(dialog.getFilterPath());
		IPath path = null;

		ArrayList<IRuntimeClasspathEntry> list = new ArrayList<IRuntimeClasspathEntry>();
		for (int i= 0; i < nChosen; i++) {
			path = filterPath.append(fileNames[i]).makeAbsolute();
			if(path.toFile().exists()) {
				list.add(JavaRuntime.newArchiveRuntimeClasspathEntry(path));
			}
		}

		setDialogSetting(LAST_PATH_SETTING, filterPath.toOSString());

		getViewer().addEntries(list.toArray(new IRuntimeClasspathEntry[0]));
	}

	/**
	 * Returns the value of the dialog setting, associated with the given
	 * settingName, resolved by the dialog setting prefix associated with this
	 * action.
	 *
	 * @param settingName unqualified setting name
	 * @return value or <code>null</code> if none
	 */
	protected String getDialogSetting(String settingName) {
		return getDialogSettings().get(getDialogSettingsPrefix() + "." + settingName); //$NON-NLS-1$
	}

	/**
	 * Sets the value of the dialog setting, associated with the given
	 * settingName, resolved by the dialog setting prefix associated with this
	 * action.
	 *
	 * @param settingName unqualified setting name
	 * @return value or <code>null</code> if none
	 */
	protected void setDialogSetting(String settingName, String value) {
		getDialogSettings().put(getDialogSettingsPrefix() + "." + settingName, value); //$NON-NLS-1$
	}

	/**
	 * Returns this plug-in's dialog settings.
	 *
	 * @return IDialogSettings
	 */
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = Plugin.getDefault().getDialogSettings();
		return settings;
	}

	/**
	 * Returns the prefix of the identifier used to store dialog settings for
	 * this action.
	 */
	protected String getDialogSettingsPrefix() {
		return prefix;
	}

	protected int getActionType() {
		return ADD;
	}
}
