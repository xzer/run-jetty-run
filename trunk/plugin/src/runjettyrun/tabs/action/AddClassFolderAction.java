/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;

import runjettyrun.Plugin;
import runjettyrun.tabs.action.helper.ClosedProjectFilter;
import runjettyrun.tabs.action.helper.OutputFolderOnlyFilter;
import runjettyrun.tabs.action.helper.SWTFactory;
import runjettyrun.tabs.classpath.IClasspathViewer;

/**
 * Adds a project to the runtime class path.
 */
public class AddClassFolderAction extends RuntimeClasspathAction {


	public AddClassFolderAction(IClasspathViewer viewer) {
		super("Add Class f&olders...", viewer);
	}

	/**
	 * Prompts for a project to add.
	 *
	 * @see IAction#run()
	 */
	public void run() {

		List<ViewerFilter> filters = new ArrayList<ViewerFilter>();
		filters.add(new ClosedProjectFilter());
		filters.add(new OutputFolderOnlyFilter());
		ElementTreeSelectionDialog dialog= SWTFactory.createAllWorkspaceFileSelectionDialog(
				getShell(),
				"Classes folder Selection",
				"Choose the class-folder to add to classpath ",
				filters
		);

		MultiStatus status = new MultiStatus(Plugin.PLUGIN_ID, IJavaDebugUIConstants.INTERNAL_ERROR,
				"One or more exceptions occurred while adding projects.", null);  //$NON-NLS-1$

		if (dialog.open() == Window.OK) {
			Object[] selections = dialog.getResult();

			List<IFolder> additions = new ArrayList<IFolder>(selections.length);
			for (int i = 0; i < selections.length; i++) {
				IFolder jp = (IFolder)selections[i];
				additions.add(jp);
			}

			List<IRuntimeClasspathEntry> runtimeEntries = new ArrayList<IRuntimeClasspathEntry>(additions.size());
			Iterator<IFolder> iter = additions.iterator();
			while (iter.hasNext()) {
				IFolder jp = (IFolder)iter.next();
				runtimeEntries.add(JavaRuntime.newArchiveRuntimeClasspathEntry(jp));
			}
			IRuntimeClasspathEntry[] entries = (IRuntimeClasspathEntry[])runtimeEntries.toArray(new IRuntimeClasspathEntry[0]);
			getViewer().addEntries(entries);

		}

		if (!status.isOK()) {
			Plugin.statusDialog(status);
		}
	}

	/**
	 * @see SelectionListenerAction#updateSelection(IStructuredSelection)
	 */
	protected boolean updateSelection(IStructuredSelection selection) {
		return getViewer().updateSelection(getActionType(), selection) && !getPossibleAdditions().isEmpty();
	}

	protected int getActionType() {
		return ADD;
	}

	/**
	 * Returns the possible projects that can be added
	 */
	protected List<IJavaProject> getPossibleAdditions() {
		IJavaProject[] projects;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		try {
			projects= JavaCore.create(root).getJavaProjects();
		} catch (JavaModelException e) {
			Plugin.logError(e);
			projects= new IJavaProject[0];
		}
		List<IJavaProject> remaining = new ArrayList<IJavaProject>();
		for (int i = 0; i < projects.length; i++) {
			remaining.add(projects[i]);
		}
		List<IJavaProject> alreadySelected = new ArrayList<IJavaProject>();
		IRuntimeClasspathEntry[] entries = getViewer().getEntries();
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getType() == IRuntimeClasspathEntry.PROJECT) {
				IResource res = root.findMember(entries[i].getPath());
				IJavaProject jp = (IJavaProject)JavaCore.create(res);
				alreadySelected.add(jp);
			}
		}
		remaining.removeAll(alreadySelected);
		return remaining;
	}

	/**
	 * Adds all projects required by <code>proj</code> to the list
	 * <code>res</code>
	 *
	 * @param proj the project for which to compute required
	 *  projects
	 * @param res the list to add all required projects too
	 */
	protected void collectRequiredProjects(IJavaProject proj, List<IJavaProject> res) throws JavaModelException {
		if (!res.contains(proj)) {
			res.add(proj);

			IJavaModel model= proj.getJavaModel();

			IClasspathEntry[] entries= proj.getRawClasspath();
			for (int i= 0; i < entries.length; i++) {
				IClasspathEntry curr= entries[i];
				if (curr.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
					IJavaProject ref= model.getJavaProject(curr.getPath().segment(0));
					if (ref.exists()) {
						collectRequiredProjects(ref, res);
					}
				}
			}
		}
	}

	/**
	 * Adds all exported entries defined by <code>proj</code> to the list
	 * <code>runtimeEntries</code>.
	 *
	 * @param proj
	 * @param runtimeEntries
	 * @throws JavaModelException
	 */
	protected void collectExportedEntries(IJavaProject proj, List<IRuntimeClasspathEntry> runtimeEntries) throws CoreException {
		IClasspathEntry[] entries = proj.getRawClasspath();
		for (int i = 0; i < entries.length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.isExported()) {
				IRuntimeClasspathEntry rte = null;
				switch (entry.getEntryKind()) {
					case IClasspathEntry.CPE_CONTAINER:
						IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), proj);
						int kind = 0;
						switch (container.getKind()) {
							case IClasspathContainer.K_APPLICATION:
								kind = IRuntimeClasspathEntry.USER_CLASSES;
								break;
							case IClasspathContainer.K_SYSTEM:
								kind = IRuntimeClasspathEntry.BOOTSTRAP_CLASSES;
								break;
							case IClasspathContainer.K_DEFAULT_SYSTEM:
								kind = IRuntimeClasspathEntry.STANDARD_CLASSES;
								break;
						}
						rte = JavaRuntime.newRuntimeContainerClasspathEntry(entry.getPath(), kind, proj);
						break;
					case IClasspathEntry.CPE_LIBRARY:
						rte = JavaRuntime.newArchiveRuntimeClasspathEntry(entry.getPath());
						rte.setSourceAttachmentPath(entry.getSourceAttachmentPath());
						rte.setSourceAttachmentRootPath(entry.getSourceAttachmentRootPath());
						break;
					case IClasspathEntry.CPE_PROJECT:
						String name = entry.getPath().segment(0);
						IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
						if (p.exists()) {
							IJavaProject jp = JavaCore.create(p);
							if (jp.exists()) {
								rte = JavaRuntime.newProjectRuntimeClasspathEntry(jp);
							}
						}
						break;
					case IClasspathEntry.CPE_VARIABLE:
						rte = JavaRuntime.newVariableRuntimeClasspathEntry(entry.getPath());
						break;
					default:
						break;
				}
				if (rte != null) {
					if (!runtimeEntries.contains(rte)) {
						runtimeEntries.add(rte);
					}
				}
			}
		}
	}
}
