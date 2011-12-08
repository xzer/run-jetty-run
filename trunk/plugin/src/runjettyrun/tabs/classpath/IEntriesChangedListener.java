package runjettyrun.tabs.classpath;


/**
 * Listener interface to receive notification when entries in a runtime
 * classpath entry viewer change in some way.
 */
public interface IEntriesChangedListener {

	/**
	 * Notification entries have changed in the viewer
	 */
	public void entriesChanged(IClasspathViewer viewer);
}
