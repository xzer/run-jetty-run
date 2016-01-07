/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package runjettyrun.tabs.action;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionListenerAction;

import runjettyrun.tabs.AbstractClasspathTab;
import runjettyrun.tabs.classpath.IClasspathViewer;

/**
 * Restores default entries in the runtime classpath.
 */
public class RestoreDefaultEntriesAction extends RuntimeClasspathAction {

	private AbstractClasspathTab fTab;
	private String customAttributeName ;
	/**
	 * Constructor
	 * @param viewer the associated classpath viewer
	 * @param tab the tab the viewer resides in
	 */
	public RestoreDefaultEntriesAction(IClasspathViewer viewer, AbstractClasspathTab tab
			,String customAttributeName
	) {
		super("R&estore Default Entries", viewer);
		this.customAttributeName = customAttributeName;
		fTab = tab;
	}

	/**
	 * Only does work if we are not currently using the default classpath
	 *
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		try {
			ILaunchConfiguration config = fTab.getLaunchConfiguration();

			ILaunchConfigurationWorkingCopy copy = null;
			if(config.isWorkingCopy()) {
				copy = (ILaunchConfigurationWorkingCopy) config;
			}
			else {
				copy = config.getWorkingCopy();
			}

			getViewer().setCustomEntries(new IRuntimeClasspathEntry[0]);
			copy.removeAttribute(customAttributeName);

		}
		catch (CoreException e) {return;}
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return true;
	}
}
