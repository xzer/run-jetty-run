package runjettyrun.tabs.classpath;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

import runjettyrun.utils.RunJettyRunClasspathResolver;
import runjettyrun.utils.RunJettyRunClasspathUtil;

public class ClasspathEntry extends AbstractClasspathEntry implements
		IRuntimeClasspathEntry, IAdaptable {

	private IRuntimeClasspathEntry entry = null;

	private boolean maven;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getJavaProject()
	 */
	public IJavaProject getJavaProject() {
		return entry.getJavaProject();
	}

	public ClasspathEntry(IRuntimeClasspathEntry entry,
			IRJRClasspathEntry parent) {
		this.parent = parent;
		this.entry = entry;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ClasspathEntry) {
			ClasspathEntry other = (ClasspathEntry) obj;
			if (entry != null) {
				return entry.equals(other.entry);
			}
		} else if (obj instanceof IRuntimeClasspathEntry) {
			return entry.equals(obj);
		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return entry.hashCode();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return entry.getPath().toOSString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getType()
	 */
	public int getType() {
		return entry.getType();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getMemento()
	 */
	public String getMemento() throws CoreException {
		return entry.getMemento();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getPath()
	 */
	public IPath getPath() {
		return entry.getPath();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getResource()
	 */
	public IResource getResource() {
		return entry.getResource();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.launching.IRuntimeClasspathEntry#getSourceAttachmentPath
	 * ()
	 */
	public IPath getSourceAttachmentPath() {
		return entry.getSourceAttachmentPath();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.launching.IRuntimeClasspathEntry#setSourceAttachmentPath
	 * (org.eclipse.core.runtime.IPath)
	 */
	public void setSourceAttachmentPath(IPath path) {
		entry.setSourceAttachmentPath(path);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.launching.IRuntimeClasspathEntry#getSourceAttachmentRootPath
	 * ()
	 */
	public IPath getSourceAttachmentRootPath() {
		return entry.getSourceAttachmentRootPath();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.launching.IRuntimeClasspathEntry#setSourceAttachmentRootPath
	 * (org.eclipse.core.runtime.IPath)
	 */
	public void setSourceAttachmentRootPath(IPath path) {
		entry.setSourceAttachmentRootPath(path);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.launching.IRuntimeClasspathEntry#getClasspathProperty()
	 */
	public int getClasspathProperty() {
		return entry.getClasspathProperty();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.launching.IRuntimeClasspathEntry#setClasspathProperty
	 * (int)
	 */
	public void setClasspathProperty(int location) {
		entry.setClasspathProperty(location);

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#getLocation()
	 */
	public String getLocation() {
		return entry.getLocation();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.launching.IRuntimeClasspathEntry#getSourceAttachmentLocation
	 * ()
	 */
	public String getSourceAttachmentLocation() {
		return entry.getSourceAttachmentLocation();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntry#
	 * getSourceAttachmentRootLocation()
	 */
	public String getSourceAttachmentRootLocation() {
		return entry.getSourceAttachmentRootLocation();
	}

	public String getVariableName() {
		return entry.getVariableName();
	}

	public IClasspathEntry getClasspathEntry() {
		return entry.getClasspathEntry();
	}

	public IRuntimeClasspathEntry getDelegate() {
		return entry;
	}

	public boolean hasChildren() {
		IRuntimeClasspathEntry delegate = getDelegate();
		if (delegate.getType() == IRuntimeClasspathEntry.ARCHIVE) {
			return false;
		} else{
			return true;
		}
	}

	public IRJRClasspathEntry[] getChildren(ILaunchConfiguration configuration) {

		try {
			IRuntimeClasspathEntry delegate = getDelegate();
			if (delegate.getType() == IRuntimeClasspathEntry.PROJECT) {
				List<IRuntimeClasspathEntry> childs;
				IResource ir = delegate.getResource();
				IJavaProject project = JavaCore.create(ir.getProject());
				childs = RunJettyRunClasspathUtil.getProjectClasspathsForUserlibs(project, maven);
				return create(childs, maven);

			} else if (delegate.getType() == IRuntimeClasspathEntry.CONTAINER) {

				// Note: 2011/12/14 Tony:
				// Here the reason we also handle the webapplication container for maven resolving issue is,
				// the web app is impossible to have project as web app .

				// In general case , WTP resolved jars in WEB-INF/lib ,
				// when we have M2E to resolved pom file , sometimes it will load dependency in WEBAPP Container ,

				// yep , it's weird , I mean it should only use existing M2E Container ,
				// but it does happened in some case , I decide to check the project entry in WEB APP Conainer.

				//There shouldn't be proejct entrys in general case, so it should be working fine.
				if (RunJettyRunClasspathResolver.isM2EMavenContainer(delegate) ||
						RunJettyRunClasspathResolver.isWebAppContainer(delegate)
				) {
					IClasspathContainer container = JavaCore.getClasspathContainer(delegate.getPath(),	delegate.getJavaProject());
					if (container == null) {
						return null;
					}
					IClasspathEntry[] cpes = container.getClasspathEntries();
					if (cpes == null) {
						return null;
					}
					IRuntimeClasspathEntry[] entries = new IRuntimeClasspathEntry[cpes.length];
					for (int i = 0; i < cpes.length; ++i) {
						IClasspathEntry cpy = cpes[i];
						entries[i] = new RuntimeClasspathEntry(cpy);
					}
					return create(entries, true);
				}
			}
			IRuntimeClasspathEntry[] entries = JavaRuntime.resolveRuntimeClasspathEntry(delegate, configuration);
			if(entries != null && entries.length ==1 && entries[0] == entry){ //same one
				return new IRJRClasspathEntry[0];
			}else{
				return create(entries, maven);
			}
		} catch (CoreException e) {
			return null;
		}
	}

	private IRJRClasspathEntry[] create(List<IRuntimeClasspathEntry> entries,
			boolean maven) {
		return create(entries.toArray(new IRuntimeClasspathEntry[0]), maven);
	}

	private IRJRClasspathEntry[] create(IRuntimeClasspathEntry[] entries,
			boolean maven) {
		ClasspathEntry[] cps = new ClasspathEntry[entries.length];
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry childEntry = entries[i];
			cps[i] = new ClasspathEntry(childEntry, this);
			cps[i].setMaven(maven);
			cps[i].setCustom(custom);
		}
		return cps;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEntry#isEditable()
	 */
	public boolean isEditable() {
		if (!(getParent() instanceof ClasspathGroup)) {
			return false;
		}

		ClasspathGroup parent = (ClasspathGroup) getParent();
		return parent.isEditable();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (getDelegate() instanceof IAdaptable) {
			return ((IAdaptable) getDelegate()).getAdapter(adapter);
		}
		return null;
	}

	public boolean isMaven() {
		return maven;
	}

	public void setMaven(boolean maven) {
		this.maven = maven;
	}

}
