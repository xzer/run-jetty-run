package runjettyrun.tabs.action.helper;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


/**
 * Filters out all output folders.
 * <p>
 * Note: Folder which are direct children of a Java element
 * are already filtered by the Java Model.
 * </p>
 *
 * @since 3.0
 */
public class FolderOnlyFilter extends ViewerFilter {

	/**
	 * Returns the result of this filter, when applied to the
	 * given element.
	 *
	 * @param viewer the viewer
	 * @param parent the parent
	 * @param element the element to test
	 * @return <code>true</code> if element should be included
	 * @since 3.0
	 */

	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof IPackageFragment || element instanceof IPackageFragmentRoot || element instanceof IFile ) {
			return false;
		} else if (element instanceof IFolder) {
			return true;
		} else if (element instanceof IProject) {
			return true;
		} else if (element instanceof IJavaProject) {
			return true;
		}

		return true;
	}
}
