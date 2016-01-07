/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package runjettyrun.tabs.action.helper;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.debug.ui.IJavaDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * A dialog for selecting projects to add to a classpath or source
 * lookup path. Optionally specifies whether
 * exported entries and required projects should also be added.
 */
public class ProjectSelectionDialog extends SelectionDialog {

	private boolean fAddExportedEntries = true;
	private boolean fAddRequiredProjects = true;

	private List<?> fProjects;

	protected CheckboxTableViewer fViewer = null;

	/**
	 * @see ListSelectionDialog
	 */
	public ProjectSelectionDialog(Shell parentShell, List<?> projects){
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fProjects = projects;
	}

	protected void okPressed() {
		Object[] elements =  fViewer.getCheckedElements();
		setResult(Arrays.asList(elements));
		super.okPressed();
	}

	protected CheckboxTableViewer createViewer(Composite parent){
		//by default return a checkbox table viewer
		Table table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.CHECK);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 150;
		gd.widthHint = 250;
		table.setLayoutData(gd);
		return new CheckboxTableViewer(table);
	}

	protected Control createDialogArea(Composite parent) {
		initializeDialogUnits(parent);
		Composite comp = (Composite) super.createDialogArea(parent);
		String label = getMessage();
		if(label != null && !"".equals(label)) {
			SWTFactory.createWrapLabel(comp, label, 1);
		}
		label = getViewerLabel();
		if(label != null && !"".equals(label)) {
			SWTFactory.createWrapLabel(comp, label, 1);
		}
		fViewer = createViewer(comp);
		fViewer.setLabelProvider(new ProjectLabelProvider());
		fViewer.setContentProvider(new ArrayContentProvider());
		fViewer.setInput(getViewerInput());
		List<?> selectedElements = getInitialElementSelections();
		if (selectedElements != null && !selectedElements.isEmpty()){
			fViewer.setSelection(new StructuredSelection(selectedElements));
		}
		addViewerListeners(fViewer);
		addCustomFotterControls(comp);
		Dialog.applyDialogFont(comp);
		String help = getHelpContextId();
		if(help != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(comp, help);
		}
		return comp;
	}

	protected void addViewerListeners(StructuredViewer viewer) {
		fViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				getButton(IDialogConstants.OK_ID).setEnabled(isValid());
			}
		});
	}

	protected boolean isValid() {
		return fViewer.getCheckedElements().length > 0;
	}

	private void addCustomFotterControls(Composite parent){
		Composite comp = SWTFactory.createComposite(parent, 2, 1, GridData.FILL_HORIZONTAL);
		GridData gd = (GridData) comp.getLayoutData();
		gd.horizontalAlignment = SWT.END;
		Button button = SWTFactory.createPushButton(comp, "&Select All", null);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fViewer.setAllChecked(true);
				getButton(IDialogConstants.OK_ID).setEnabled(isValid());
			}
		});
		button = SWTFactory.createPushButton(comp, "&Deselect All", null);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fViewer.setAllChecked(false);
				getButton(IDialogConstants.OK_ID).setEnabled(isValid());
			}
		});
	}

	/**
	 * Returns whether the user has selected to add exported entries.
	 *
	 * @return whether the user has selected to add exported entries
	 */
	public boolean isAddExportedEntries() {
		return fAddExportedEntries;
	}

	/**
	 * Returns whether the user has selected to add required projects.
	 *
	 * @return whether the user has selected to add required projects
	 */
	public boolean isAddRequiredProjects() {
		return fAddRequiredProjects;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getDialogSettingsId()
	 */
	protected String getDialogSettingsId() {
		return IJavaDebugUIConstants.PLUGIN_ID + ".PROJECT_SELECTION_DIALOG_SECTION"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return "select_project_dialog";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerInput()
	 */
	protected Object getViewerInput() {
		return fProjects;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.launchConfigurations.AbstractDebugSelectionDialog#getViewerLabel()
	 */
	protected String getViewerLabel() {
		return "Choose &project(s) to add:";
	}
}
