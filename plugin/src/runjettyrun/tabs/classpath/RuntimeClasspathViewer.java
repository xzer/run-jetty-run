package runjettyrun.tabs.classpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import runjettyrun.tabs.action.RuntimeClasspathAction;

/**
 * A viewer that displays and manipulates runtime classpath entries.
 */
public class RuntimeClasspathViewer extends ContainerCheckedTreeViewer implements IClasspathViewer {

	/**
	 * Entry changed listeners
	 */
	private ListenerList fListeners = new ListenerList();

	private IRJRClasspathEntry fCurrentParent= null;

	/**
	 * Creates a runtime classpath viewer with the given parent.
	 *
	 * @param parent the parent control
	 */
	public RuntimeClasspathViewer(Composite parent) {
		super(parent);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 3;
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.heightHint = getTree().getItemHeight();
		getTree().setLayoutData(data);

		getTree().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (updateSelection(RuntimeClasspathAction.REMOVE, (IStructuredSelection)getSelection()) && event.character == SWT.DEL && event.stateMask == 0) {
					List<?> selection= getSelectionFromWidget();
					getClasspathContentProvider().removeAll(selection);
					notifyChanged();
				}
			}
		});

		getTree().addTreeListener(new TreeListener() {
			public void treeExpanded(TreeEvent e) {
				refresh();
			}

			public void treeCollapsed(TreeEvent e) {

			}
		});

		setAutoExpandLevel(5);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#setEntries(org.eclipse.jdt.launching.IRuntimeClasspathEntry[])
	 */
	public void setEntries(IRuntimeClasspathEntry[] entries) {
		getClasspathContentProvider().setRefreshEnabled(false);
		resolveCurrentParent(getSelection());
		getClasspathContentProvider().removeAll(fCurrentParent);
		getClasspathContentProvider().setEntries(entries);
		getClasspathContentProvider().setRefreshEnabled(true);
		notifyChanged();
	}

	public void setCustomEntries(IRuntimeClasspathEntry[] entries) {
		getClasspathContentProvider().setRefreshEnabled(false);
//		resolveCurrentParent(getSelection());
//		getClasspathContentProvider().removeAll(fCurrentParent);
		getClasspathContentProvider().setCustomEntries(entries);
		getClasspathContentProvider().setRefreshEnabled(true);
		notifyChanged();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getEntries()
	 */
	public IRuntimeClasspathEntry[] getEntries() {
		return getClasspathContentProvider().getModel().getAllEntries();
	}

	public IRuntimeClasspathEntry[] getCustomEntries() {
		return getClasspathContentProvider().getModel().getCustomEntries();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#addEntries(org.eclipse.jdt.launching.IRuntimeClasspathEntry[])
	 */
	public void addEntries(IRuntimeClasspathEntry[] entries) {
		getClasspathContentProvider().setRefreshEnabled(false);
		IStructuredSelection sel = (IStructuredSelection) getSelection();
		Object beforeElement = sel.getFirstElement();
		resolveCurrentParent(getSelection());
		List<IRJRClasspathEntry> existingEntries= Arrays.asList(fCurrentParent.getEntries());
		for (int i = 0; i < entries.length; i++) {
			if ( !existingEntries.contains(entries[i])) {
				getClasspathContentProvider().add(fCurrentParent, entries[i], beforeElement);
			}
		}
		getClasspathContentProvider().setRefreshEnabled(true);
		notifyChanged();
	}

	private boolean resolveCurrentParent(ISelection selection) {
		fCurrentParent= null;
		Iterator<?> selected= ((IStructuredSelection)selection).iterator();

		while (selected.hasNext()) {
			Object element = selected.next();
			if (element instanceof ClasspathEntry) {
				IRJRClasspathEntry parent= ((IRJRClasspathEntry)element).getParent();
				if (fCurrentParent != null) {
					if (!fCurrentParent.equals(parent)) {
						return false;
					}
				} else {
					fCurrentParent= parent;
				}
			} else {
				if (fCurrentParent != null) {
					if (!fCurrentParent.equals(element)) {
						return false;
					}
				} else {
					fCurrentParent= (IRJRClasspathEntry)element;
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#isEnabled()
	 */
	public boolean isEnabled() {
		return true;
	}

	/**
	 * Sets the launch configuration context for this viewer, if any
	 */
	public void setLaunchConfiguration(ILaunchConfiguration configuration) {
		if (getLabelProvider() != null) {
			((ClasspathLabelProvider)getLabelProvider()).setLaunchConfiguration(configuration);
		}
	}

	public void addEntriesChangedListener(IEntriesChangedListener listener) {
		fListeners.add(listener);
	}

	public void removeEntriesChangedListener(IEntriesChangedListener listener) {
		fListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#notifyChanged()
	 */
	public void notifyChanged() {
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IEntriesChangedListener)listeners[i]).entriesChanged(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#indexOf(org.eclipse.jdt.launching.IRuntimeClasspathEntry)
	 */
	public int indexOf(IRuntimeClasspathEntry entry) {
		IRJRClasspathEntry[] entries= getClasspathContentProvider().getCustomClasspathEntries();
		for (int i = 0; i < entries.length; i++) {
			IRJRClasspathEntry existingEntry = entries[i];
			if (existingEntry.equals(entry)) {
				return 1;
			}
		}
		entries=  getClasspathContentProvider().getUserClasspathEntries();
		for (int i = 0; i < entries.length; i++) {
			IRJRClasspathEntry existingEntry = entries[i];
			if (existingEntry.equals(entry)) {
				return 1;
			}
		}

		return -1;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getShell()
	 */
	public Shell getShell() {
		return getControl().getShell();
	}

	private UserClassesClasspathContentProvider getClasspathContentProvider() {
		return (UserClassesClasspathContentProvider)super.getContentProvider();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#updateSelection(int, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public boolean updateSelection(int actionType, IStructuredSelection selection) {

		if (selection.isEmpty()) {
			return false;
		}
		switch (actionType) {
			case RuntimeClasspathAction.ADD :
				Iterator<?> selected= selection.iterator();
				while (selected.hasNext()) {
					IRJRClasspathEntry entry = (IRJRClasspathEntry)selected.next();
					if(entry != null){
						IRJRClasspathEntry point = entry ;
						while(point!=null){
							if(point instanceof ClasspathGroup){
								if(((ClasspathGroup)point).isEditable()){
									return true;
								}
							}

							point = point.getParent();
						}
					}
				}
				return false;
			case RuntimeClasspathAction.REMOVE :
				selected= selection.iterator();
				while (selected.hasNext()) {
					IRJRClasspathEntry entry = (IRJRClasspathEntry)selected.next();
					if(entry instanceof ClasspathGroup && !((ClasspathGroup)entry).canBeRemoved()){
						return false;
					}else if (!entry.isEditable()) {
						return false;
					}
				}
				return selection.size() > 0;
			case RuntimeClasspathAction.MOVE :
				selected= selection.iterator();
				while (selected.hasNext()) {
					IRJRClasspathEntry entry = (IRJRClasspathEntry)selected.next();
					if (!entry.isEditable()) {
						return false;
					}
				}
				return selection.size() > 0;
			default :
				break;
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.launcher.IClasspathViewer#getSelectedEntries()
	 */
	public ISelection getSelectedEntries() {
		IStructuredSelection selection= (IStructuredSelection)getSelection();
		List<IRJRClasspathEntry> entries= new ArrayList<IRJRClasspathEntry>(selection.size() * 2);
		Iterator<?> itr= selection.iterator();
		while (itr.hasNext()) {
			IRJRClasspathEntry element = (IRJRClasspathEntry) itr.next();
			if (element.hasEntries()) {
				entries.addAll(Arrays.asList(element.getEntries()));
			} else {
				entries.add(element);
			}
		}

		return new StructuredSelection(entries);
	}

}
