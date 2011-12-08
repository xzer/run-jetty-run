package runjettyrun.tabs.classpath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import runjettyrun.tabs.AbstractClasspathTab;
import runjettyrun.utils.RunJettyRunClasspathResolver;

/**
 * Content provider that maintains a list of classpath entries which are shown in a tree
 * viewer.
 */
public class UserClassesClasspathContentProvider implements ITreeContentProvider {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(UserClassesClasspathContentProvider.class.getName());

	private TreeViewer treeViewer;
	private UserClassesClasspathModel model= null;
	private boolean refreshEnabled= false;
	private boolean refreshRequested= false;
	private AbstractClasspathTab fTab;

	public UserClassesClasspathContentProvider(AbstractClasspathTab tab) {
		fTab = tab;
	}

	public void add(IClasspathEntry parent, IRuntimeClasspathEntry child, Object beforeElement) {
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

	public void removeAll(IClasspathEntry parent) {
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
			IRuntimeClasspathEntry entry = ((ClasspathEntry) parentElement).getDelegate();

			if(entry.getType() != IRuntimeClasspathEntry.PROJECT){
				if(entry instanceof IRuntimeClasspathEntry2){
					IRuntimeClasspathEntry2 entry2 = (IRuntimeClasspathEntry2) entry;
					try {
						IRuntimeClasspathEntry[] entries = entry2.getRuntimeClasspathEntries(fTab.getLaunchConfiguration());
						return convertRuntimeToClasspathEntry( (ClasspathEntry) parentElement, entries);
					} catch (CoreException e) {
						logger.severe("Object - exception: " + e);
						return null;
					}
				}
			}else if(entry.getType() == IRuntimeClasspathEntry.PROJECT){
				try {

					IRuntimeClasspathEntry[] entries = filteBootstrapEntries(JavaRuntime.computeUnresolvedRuntimeClasspath((IJavaProject)JavaCore.create(entry.getResource())));
					entries = RunJettyRunClasspathResolver.resolveClasspath(entries, fTab.getLaunchConfiguration());
					entries = converProjectToSourceFolder(entry,entries);
					return convertRuntimeToClasspathEntry( (ClasspathEntry) parentElement, entries);
				} catch (CoreException e) {
					logger.severe("Object - exception: " + e);

					return null;
				}
			}
		}
		if (parentElement == null) {
			List<Object> all= new ArrayList<Object>();
			IClasspathEntry[] topEntries= model.getEntries();
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

	private IRuntimeClasspathEntry[] converProjectToSourceFolder(IRuntimeClasspathEntry projectEntry,IRuntimeClasspathEntry[] entries){
		if(entries==  null) {
			throw new IllegalArgumentException("Entries can't be null");
		}
		List<IRuntimeClasspathEntry> lists = new ArrayList<IRuntimeClasspathEntry>();
		for(IRuntimeClasspathEntry entry:entries){
			if(isSameProjectClasspathEntry(entry, projectEntry)){
				//This means prjoect only have default output location.
				IPath path= new Path(entry.getLocation()).makeAbsolute();
				lists.add(
					JavaRuntime.newArchiveRuntimeClasspathEntry(path)
				);
			}else{
				lists.add(entry);
			}
		}
		return lists.toArray(new IRuntimeClasspathEntry[0]);
	}

	private boolean isSameProjectClasspathEntry(IRuntimeClasspathEntry entry,IRuntimeClasspathEntry entry2){
		if(entry.getType() == IRuntimeClasspathEntry.PROJECT &&
				entry2.getType() == IRuntimeClasspathEntry.PROJECT
		){
			return entry.getResource().equals(entry2.getResource());
		}

		return false;
	}


	private IRuntimeClasspathEntry[] filteBootstrapEntries(IRuntimeClasspathEntry[] entries){
		if(entries==  null) {
			throw new IllegalArgumentException("Entries can't be null");
		}
		List<IRuntimeClasspathEntry> lists = new ArrayList<IRuntimeClasspathEntry>();
		for(IRuntimeClasspathEntry entry:entries){
			if(entry.getClasspathProperty() != IRuntimeClasspathEntry.STANDARD_CLASSES){
				lists.add(entry);
			}
		}
		return lists.toArray(new IRuntimeClasspathEntry[0]);
	}

	private IClasspathEntry[]  convertRuntimeToClasspathEntry(ClasspathEntry parentElement,IRuntimeClasspathEntry[] entries ){
		IClasspathEntry[] cps = new IClasspathEntry[entries.length];
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry childEntry = entries[i];
			cps[i] = new ClasspathEntry(childEntry, (ClasspathEntry) parentElement);
		}
		return cps;
	}

	public void removeAll(List<?> selection) {
		Object[] array= selection.toArray();
		model.removeAll(array);
		treeViewer.remove(array);
		refresh();
	}

	public IClasspathEntry[] getCustomClasspathEntries() {
		return model.getEntries(UserClassesClasspathModel.CUSTOM);
	}

	public IClasspathEntry[] getUserClasspathEntries() {
		return model.getEntries(UserClassesClasspathModel.USER);
	}

	public void handleMove(boolean direction, IClasspathEntry entry) {
		IClasspathEntry parent = (IClasspathEntry)getParent(entry);
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
