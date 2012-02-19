package runjettyrun.tabs.classpath;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class AbstractClasspathEntry implements IRJRClasspathEntry,Iterable<IRJRClasspathEntry> {

	protected List<IRJRClasspathEntry> childEntries = new ArrayList<IRJRClasspathEntry>();
	protected IRJRClasspathEntry parent = null;

	protected boolean custom = false;

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.IClasspathEntry#moveChild(int)
	 */
	public void moveChild(boolean up, IRJRClasspathEntry child) {
		int index= childEntries.indexOf(child);
		int direction= 1;
		if (up) {
			direction= -1;
		}
		IRJRClasspathEntry moved= childEntries.get(index+direction);
		childEntries.set(index + direction, child);
		childEntries.set(index, moved);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.IClasspathEntry#getEntries()
	 */
	public IRJRClasspathEntry[] getEntries() {
		return (IRJRClasspathEntry[])childEntries.toArray(new IRJRClasspathEntry[childEntries.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.IClasspathEntry#hasEntries()
	 */
	public boolean hasEntries() {
		return !childEntries.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.IClasspathEntry#getParent()
	 */
	public IRJRClasspathEntry getParent() {
		return parent;
	}

	/**
	 * @param parent The parent to set.
	 */
	public void setParent(IRJRClasspathEntry parent) {
		this.parent = parent;
	}


	public boolean isCustom() {
		return custom;
	}

	public void setCustom(boolean custom) {
		this.custom = custom;
	}

	public Iterator<IRJRClasspathEntry> iterator(){
		return childEntries.iterator();
	}
}
