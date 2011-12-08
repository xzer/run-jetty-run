/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package runjettyrun.tabs.action;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jface.action.IAction;

import runjettyrun.tabs.classpath.IClasspathViewer;

/**
 * Adds an internal jar to the runtime class path.
 */
public class AddJarAction extends RuntimeClasspathAction {

	public AddJarAction(IClasspathViewer viewer) {
		super("Add &JARs...", viewer);
	}

	/**
	 * Prompts for a jar to add.
	 *
	 * @see IAction#run()
	 */
	public void run() {

		IPath[] paths = BuildPathDialogAccess.chooseJAREntries(getShell(), null, new IPath[0]);

		if (paths != null && paths.length > 0) {
			IRuntimeClasspathEntry[] res= new IRuntimeClasspathEntry[paths.length];
			for (int i= 0; i < res.length; i++) {
				IResource elem= ResourcesPlugin.getWorkspace().getRoot().getFile(paths[i]);
				res[i]= JavaRuntime.newArchiveRuntimeClasspathEntry(elem);
			}
			getViewer().addEntries(res);
		}
	}

	protected int getActionType() {
		return ADD;
	}
}
