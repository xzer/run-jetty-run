package runjettyrun.tabs.classpath;


import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Label provider for runtime classpath entries.
 */
public class RuntimeUserClasspathEntryLabelProvider extends LabelProvider {

	private WorkbenchLabelProvider lp = new WorkbenchLabelProvider();

	/**
	 * Context in which to render containers, or <code>null</code>
	 */
	private ILaunchConfiguration fLaunchConfiguration;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry)element;
		IResource resource = entry.getResource();
		switch (entry.getType()) {
			case IRuntimeClasspathEntry.PROJECT:
				IJavaElement proj = JavaCore.create(resource);
				if(proj == null) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT_CLOSED);
				}
				else {
					return lp.getImage(proj);
				}
			case IRuntimeClasspathEntry.ARCHIVE:
				if (resource instanceof IContainer) {
					return lp.getImage(resource);
				}
				if(resource !=  null && resource.getLocation().toFile().isDirectory()){
					return PlatformUI.getWorkbench().getSharedImages().getImage(
							org.eclipse.ui.ISharedImages.IMG_OBJ_FOLDER);
				}

				boolean external = resource == null;
				boolean source = true ;//(entry.getSourceAttachmentPath() != null && !Path.EMPTY.equals(entry.getSourceAttachmentPath()));
				String key = null;
				if (external) {
					IPath path = entry.getPath();
					if (path != null)
					{
						File file = path.toFile();
						if (file.exists() && file.isDirectory()) {
							key = ISharedImages.IMG_OBJS_PACKFRAG_ROOT;
						} else {
							key = ISharedImages.IMG_OBJS_EXTERNAL_ARCHIVE;
						}
					}

				} else {
					if (source) {
						key = ISharedImages.IMG_OBJS_JAR_WITH_SOURCE;
					} else {
						key = ISharedImages.IMG_OBJS_JAR;
					}
				}
				return JavaUI.getSharedImages().getImage(key);
			case IRuntimeClasspathEntry.VARIABLE:
				return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_ENV_VAR);
			case IRuntimeClasspathEntry.CONTAINER:
                return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
			case IRuntimeClasspathEntry.OTHER:
				IRuntimeClasspathEntry delegate = entry;
				if (entry instanceof ClasspathEntry) {
					delegate = ((ClasspathEntry)entry).getDelegate();
				}
				Image image = lp.getImage(delegate);
				if (image != null) {
					return image;
				}
				if (resource == null) {
                    return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_LIBRARY);
				}
				return lp.getImage(resource);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry)element;
		switch (entry.getType()) {
			case IRuntimeClasspathEntry.PROJECT:
				IResource res = entry.getResource();
				IJavaElement proj = JavaCore.create(res);
				if(proj == null) {
					return entry.getPath().lastSegment();
				}
				else {
					return lp.getText(proj);
				}
			case IRuntimeClasspathEntry.ARCHIVE:
				IPath path = entry.getPath();
				if (path == null) {
                    return MessageFormat.format("Invalid path: {0}", new Object[]{"null"}); //$NON-NLS-1$
                }
                if (!path.isAbsolute() || !path.isValidPath(path.toString())) {
					return MessageFormat.format("Invalid path: {0}", new Object[]{path.toOSString()});
				}
				String[] segments = path.segments();
				StringBuffer displayPath = new StringBuffer();
				if (segments.length > 0) {
					String device = path.getDevice();
					if (device != null) {
						displayPath.append(device);
						displayPath.append(File.separator);
					}
					for (int i = 0; i < segments.length -1; i++) {
						displayPath.append(segments[i]).append(File.separator);
					}
					displayPath.append(segments[segments.length - 1]);

					//getDevice means that's a absolute path.
					if(path.getDevice() != null && !path.toFile().exists()){
						displayPath.append(" (missing) ");
					}
				} else {
					displayPath.append(path.toOSString());
				}
				return displayPath.toString();
			case IRuntimeClasspathEntry.VARIABLE:
				path = entry.getPath();
				IPath srcPath = entry.getSourceAttachmentPath();
				StringBuffer buf = new StringBuffer(path.toString());
				if (srcPath != null) {
					buf.append(" ["); //$NON-NLS-1$
					buf.append(srcPath.toString());
					IPath rootPath = entry.getSourceAttachmentRootPath();
					if (rootPath != null) {
						buf.append(IPath.SEPARATOR);
						buf.append(rootPath.toString());
					}
					buf.append(']');
				}
				// append JRE name if we can compute it
				if (path.equals(new Path(JavaRuntime.JRELIB_VARIABLE)) && fLaunchConfiguration != null) {
					try {
						IVMInstall vm = JavaRuntime.computeVMInstall(fLaunchConfiguration);
						buf.append(" - "); //$NON-NLS-1$
						buf.append(vm.getName());
					} catch (CoreException e) {
					}
				}
				return buf.toString();
			case IRuntimeClasspathEntry.CONTAINER:
				path = entry.getPath();
				if (fLaunchConfiguration != null) {
					try {
						IJavaProject project = null;
						try {
							project = JavaRuntime.getJavaProject(fLaunchConfiguration);
						} catch (CoreException e) {
						}
						if (project == null) {
						} else {
							IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
							if (container != null) {
								if(container.getDescription().startsWith("Persisted container")){
									return container.getPath().toString();
								}else{
									return container.getDescription();
								}
							}
						}
					} catch (CoreException e) {
					}
				}
				return entry.getPath().toString();
			case IRuntimeClasspathEntry.OTHER:
				IRuntimeClasspathEntry delegate = entry;
				if (entry instanceof ClasspathEntry) {
					delegate = ((ClasspathEntry)entry).getDelegate();
				}
				String name = lp.getText(delegate);
				if (name == null || name.length() == 0) {
					return ((IRuntimeClasspathEntry2)delegate).getName();
				}
				return name;
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		lp.dispose();
	}

	/**
	 * Sets the launch configuration context for this label provider
	 */
	public void setLaunchConfiguration(ILaunchConfiguration configuration) {
		fLaunchConfiguration = configuration;
		fireLabelProviderChanged(new LabelProviderChangedEvent(this));
	}
}
