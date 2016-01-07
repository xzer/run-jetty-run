package runjettyrun.tabs.action.helper;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.jdt.core.IPackageFragmentRoot;


/**
 * The LibraryFilter is a filter used to determine whether
 * a Java library is shown
 */
public class LibraryFilter extends ViewerFilter {

	/* (non-Javadoc)
	 * Method declared on ViewerFilter.
	 */
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IPackageFragmentRoot) {
			IPackageFragmentRoot root= (IPackageFragmentRoot)element;
			if (root.isArchive()) {
				// don't filter out JARs contained in the project itself
				IResource resource= root.getResource();
				if (resource != null) {
					IProject jarProject= resource.getProject();
					IProject container= root.getJavaProject().getProject();
					return container.equals(jarProject);
				}
				return false;
			}
		}
		return true;
	}
}
