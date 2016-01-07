package runjettyrun.tabs.action.helper;


import org.eclipse.core.resources.IResource;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.jdt.core.IJavaElement;

/**
 * Filters closed projects
 */
public class ClosedProjectFilter extends ViewerFilter {

	/*
	 * @see ViewerFilter
	 */
	public boolean select(Viewer viewer, Object parent, Object element) {
		if (element instanceof IJavaElement)
			return ((IJavaElement)element).getJavaProject().getProject().isOpen();
		if (element instanceof IResource)
			return ((IResource)element).getProject().isOpen();
		return true;
	}
}
