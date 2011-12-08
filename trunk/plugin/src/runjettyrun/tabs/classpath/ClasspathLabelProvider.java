package runjettyrun.tabs.classpath;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * Label provider for classpath elements
 */
public class ClasspathLabelProvider implements ILabelProvider, IColorProvider {

	private RuntimeUserClasspathEntryLabelProvider runtimeClasspathLabelProvider= new RuntimeUserClasspathEntryLabelProvider();

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (element instanceof ClasspathEntry) {
			ClasspathEntry entry = (ClasspathEntry) element;
			return runtimeClasspathLabelProvider.getImage(entry);
		}

		return JavaClasspathTab.getClasspathImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof ClasspathEntry) {
			ClasspathEntry entry = (ClasspathEntry) element;
			return runtimeClasspathLabelProvider.getText(entry.getDelegate());
		}
		return element.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		if (element instanceof ClasspathGroup) {
			Display display= Display.getCurrent();
			return display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		} else if (element instanceof ClasspathEntry) {

		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		runtimeClasspathLabelProvider.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

	/**
	 * @param configuration
	 */
	public void setLaunchConfiguration(ILaunchConfiguration configuration) {
		runtimeClasspathLabelProvider.setLaunchConfiguration(configuration);
	}
}
