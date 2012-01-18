package runjettyrun.tabs.classpath;

import java.util.Iterator;


public class ClasspathGroup extends AbstractClasspathEntry {
	private String name;

	private boolean canBeRemoved= true;
	private boolean editable = false;

	public ClasspathGroup(String name, IRJRClasspathEntry parent, boolean canBeRemoved, boolean editable) {
		this.parent= parent;
		this.name= name;
		this.canBeRemoved= canBeRemoved;
		this.editable = editable;
	}

	public void addEntry(IRJRClasspathEntry entry, Object beforeEntry) {
		if (!childEntries.contains(entry)) {
			int index = -1;
			if (beforeEntry != null) {
				index = childEntries.indexOf(beforeEntry);
			}
			if (index >= 0) {
				childEntries.add(index, entry);
			} else {
				childEntries.add(entry);
			}
		}
	}

	public void removeEntry(IRJRClasspathEntry entry) {
		childEntries.remove(entry);
	}

	public boolean contains(IRJRClasspathEntry entry) {
		return childEntries.contains(entry);
	}

	public String toString() {
		return name;
	}

	public void removeAll() {
		@SuppressWarnings("rawtypes")
		Iterator iter= childEntries.iterator();
		while (iter.hasNext()) {
			Object entry = iter.next();
			if (entry instanceof ClasspathGroup) {
				((ClasspathGroup)entry).removeAll();
			}
		}
		childEntries.clear();
	}

	public boolean canBeRemoved() {
		return canBeRemoved;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEntry#isEditable()
	 */
	public boolean isEditable() {
		return editable;
	}

	public String getRealPath() {
		return name;
	}
}
