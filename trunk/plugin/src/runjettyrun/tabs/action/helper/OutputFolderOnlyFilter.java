package runjettyrun.tabs.action.helper;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
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
public class OutputFolderOnlyFilter extends ViewerFilter {

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
			IFolder folder = (IFolder) element;
			return select(folder.getProject(), folder);
		} else if (element instanceof IProject) {
			return select((IProject) element, (IProject) element);
		} else if (element instanceof IJavaProject) {
			return select(((IJavaProject) element).getProject(),
					((IJavaProject) element).getProject());
		}

		return true;
	}

	private boolean select(IProject proj,IResource folder) {
		try {
			if (!proj.hasNature(JavaCore.NATURE_ID))
				return false;

			IJavaProject jProject= JavaCore.create(proj);
			if (jProject == null || !jProject.exists())
				return false;

			// Check default output location
			IPath defaultOutputLocation= jProject.getOutputLocation();
			IPath folderPath= folder.getFullPath();

			//project as classpath , it's not the case we need.
			if(folder == proj && folderPath.equals(defaultOutputLocation)){
				return false;
			}

			if (defaultOutputLocation != null &&
					(folderPath.isPrefixOf(defaultOutputLocation) || defaultOutputLocation.equals(folderPath)) )
				return true;

			// Check output location for each class path entry
			IClasspathEntry[] cpEntries= jProject.getRawClasspath();
			for (int i= 0, length= cpEntries.length; i < length; i++) {
				IPath outputLocation= cpEntries[i].getOutputLocation();
				if (outputLocation != null &&
						(folderPath.isPrefixOf(outputLocation) || outputLocation.equals(folderPath)))
					return true;
			}
			return false;
		} catch (CoreException ex) {
			return true;
		}
	}
}
