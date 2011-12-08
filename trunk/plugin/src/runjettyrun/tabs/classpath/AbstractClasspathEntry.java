package runjettyrun.tabs.classpath;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractClasspathEntry implements IClasspathEntry {

	protected List<Object> childEntries = new ArrayList<Object>();
	protected IClasspathEntry parent = null;

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.IClasspathEntry#moveChild(int)
	 */
	public void moveChild(boolean up, IClasspathEntry child) {
		int index= childEntries.indexOf(child);
		int direction= 1;
		if (up) {
			direction= -1;
		}
		Object moved= childEntries.get(index+direction);
		childEntries.set(index + direction, child);
		childEntries.set(index, moved);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.preferences.IClasspathEntry#getEntries()
	 */
	public IClasspathEntry[] getEntries() {
		return (IClasspathEntry[])childEntries.toArray(new IClasspathEntry[childEntries.size()]);
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
	public IClasspathEntry getParent() {
		return parent;
	}

	/**
	 * @param parent The parent to set.
	 */
	public void setParent(IClasspathEntry parent) {
		this.parent = parent;
	}
}
