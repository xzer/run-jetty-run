package runjettyrun.tabs.classpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import runjettyrun.tabs.AbstractClasspathTab;

/**
 * Content provider that maintains a list of classpath entries which are shown in a tree
 * viewer.
 */
public class UserClassesClasspathContentProvider implements ITreeContentProvider {

	private TreeViewer treeViewer;
	private UserClassesClasspathModel model= null;
	private boolean refreshEnabled= false;
	private boolean refreshRequested= false;
	private AbstractClasspathTab fTab;

	public UserClassesClasspathContentProvider(AbstractClasspathTab tab) {
		fTab = tab;
	}

	public void add(IRJRClasspathEntry parent, IRuntimeClasspathEntry child, Object beforeElement) {
		Object newEntry= null;
		if (parent == null || parent == model) {
			newEntry= model.addEntry(child);
			parent= model;
		} else if (parent instanceof ClasspathGroup) {
			newEntry= model.createEntry(child, parent);
			((ClasspathGroup)parent).addEntry((ClasspathEntry)newEntry, beforeElement);
		}
		if (newEntry != null) {
			treeViewer.add(parent, newEntry);
			treeViewer.setExpandedState(parent, true);
			treeViewer.reveal(newEntry);
			refresh();
		}
	}

	public void add(int entryType, IRuntimeClasspathEntry child) {
		Object newEntry= model.addEntry(entryType, child);
		if (newEntry != null) {
			treeViewer.add(getParent(newEntry), newEntry);
			refresh();
		}
	}

	public void removeAll() {
		model.removeAll();
		refresh();
	}

	private void refresh() {
		if (refreshEnabled) {
			treeViewer.refresh();
			refreshRequested= false;
		} else {
			refreshRequested= true;
		}
	}

	public void removeAll(IRJRClasspathEntry parent) {
		if (parent instanceof ClasspathGroup) {
			((ClasspathGroup)parent).removeAll();
		}
		refresh();
	}

	/**
	 * @see ITreeContentProvider#getParent(Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof ClasspathEntry) {
			return ((ClasspathEntry)element).getParent();
		}
		if (element instanceof ClasspathGroup) {
			return model;
		}

		return null;
	}

	/**
	 * @see ITreeContentProvider#hasChildren(Object)
	 */
	public boolean hasChildren(Object element) {
		if (element instanceof ClasspathEntry) {
			return (((ClasspathEntry)element).hasChildren());
		}
		if (element instanceof ClasspathGroup) {
			return ((ClasspathGroup)element).hasEntries();

		}

		if (element instanceof UserClassesClasspathModel) {
			return ((UserClassesClasspathModel) element).hasEntries();
		}
		return false;
	}

	/**
	 * @see IStructuredContentProvider#getElements(Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		treeViewer = (TreeViewer) viewer;

		if (newInput != null) {
			model= (UserClassesClasspathModel)newInput;
		} else {
			if (model != null) {
				model.removeAll();
			}
			model= null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ClasspathGroup) {
			Object[] returnObjectArray = ((ClasspathGroup) parentElement)
					.getEntries();
			return returnObjectArray;
		}
		if (parentElement instanceof UserClassesClasspathModel) {
			Object[] returnObjectArray = ((UserClassesClasspathModel) parentElement)
					.getEntries();
			return returnObjectArray;
		}
		if (parentElement instanceof ClasspathEntry) {
			 return ((ClasspathEntry)parentElement).getChildren(fTab.getLaunchConfiguration());
		}
		if (parentElement == null) {
			List<Object> all= new ArrayList<Object>();
			IRJRClasspathEntry[] topEntries= model.getEntries();
			for (int i = 0; i < topEntries.length; i++) {
				Object object = topEntries[i];
				if (object instanceof ClasspathEntry) {
					all.add(object);
				} else if (object instanceof ClasspathGroup) {
					all.addAll(Arrays.asList(((ClasspathGroup)object).getEntries()));
				}
			}
			Object[] returnObjectArray = all.toArray();
			return returnObjectArray;
		}

		return null;
	}

	public void removeAll(List<?> selection) {
		Object[] array= selection.toArray();
		model.removeAll(array);
		treeViewer.remove(array);
		refresh();
	}

	public IRJRClasspathEntry[] getCustomClasspathEntries() {
		return model.getEntries(UserClassesClasspathModel.CUSTOM);
	}

	public IRJRClasspathEntry[] getUserClasspathEntries() {
		return model.getEntries(UserClassesClasspathModel.USER);
	}

	public void handleMove(boolean direction, IRJRClasspathEntry entry) {
		IRJRClasspathEntry parent = (IRJRClasspathEntry)getParent(entry);
		parent.moveChild(direction, entry);
	}

	public UserClassesClasspathModel getModel() {
		return model;
	}

	public void setRefreshEnabled(boolean refreshEnabled) {
		this.refreshEnabled = refreshEnabled;
		treeViewer.getTree().setRedraw(refreshEnabled);
		if (refreshEnabled && refreshRequested) {
			refresh();
		}
	}
	public void setCustomEntries(IRuntimeClasspathEntry[] entries) {
		model.cleanRootGroup(UserClassesClasspathModel.CUSTOM);
		for (int i = 0; i < entries.length; i++) {
			model.addEntry(UserClassesClasspathModel.CUSTOM, entries[i]);
		}
		refresh();
	}

	public void setEntries(IRuntimeClasspathEntry[] entries) {
		model.cleanRootGroup(UserClassesClasspathModel.USER);
		IRuntimeClasspathEntry entry;
		for (int i = 0; i < entries.length; i++) {
			entry= entries[i];
			switch (entry.getClasspathProperty()) {
				case IRuntimeClasspathEntry.USER_CLASSES:
					model.addEntry(UserClassesClasspathModel.USER, entry);
					break;
			}
		}
		refresh();
	}
}
