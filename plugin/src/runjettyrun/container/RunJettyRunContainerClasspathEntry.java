package runjettyrun.container;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import runjettyrun.Plugin;

public class RunJettyRunContainerClasspathEntry implements IClasspathEntry,
		IRuntimeClasspathEntry,IRuntimeClasspathEntry2 {
	
	int property;
	/*
	 * Default access rules
	 */
	public final static IAccessRule[] NO_ACCESS_RULES = {};

	private IPath containerPath = null;
	private IClasspathAttribute[] attribute;

	public RunJettyRunContainerClasspathEntry(String containerName,int properties) {
		
		this(containerName, new IClasspathAttribute[0],properties);
	}

	public RunJettyRunContainerClasspathEntry(String containerName,
			List<IClasspathAttribute> attribute,int properties) {
		this(containerName, attribute.toArray(new IClasspathAttribute[0]),properties);
	}

	public RunJettyRunContainerClasspathEntry(String containerName,
			IClasspathAttribute[] attribute,int properties) {
		this.attribute = attribute;
		this.property = properties;
		containerPath = new Path(containerName);
	}

	public boolean combineAccessRules() {
		return false;
	}

	public IAccessRule[] getAccessRules() {
		return NO_ACCESS_RULES;
	}

	public int getContentKind() {
		return 0;
	}

	public int getEntryKind() {
		return IClasspathEntry.CPE_CONTAINER;
	}

	public IPath[] getExclusionPatterns() {
		return new IPath[0];
	}

	public IClasspathAttribute[] getExtraAttributes() {
		return attribute;
	}

	public IPath[] getInclusionPatterns() {
		return new IPath[0];
	}

	public IPath getOutputLocation() {
		return null;
	}

	public IPath getPath() {
		return containerPath;
	}

	public IPath getSourceAttachmentPath() {
		return null;
	}

	public IPath getSourceAttachmentRootPath() {
		return null;
	}

	public IClasspathEntry getReferencingEntry() {
		return null;
	}

	public boolean isExported() {
		return false;
	}

	/**
	 * @see IClasspathEntry
	 * @deprecated
	 */
	public IClasspathEntry getResolvedEntry() {
		return JavaCore.getResolvedClasspathEntry(this);
	}

	public int getType() {
		return IRuntimeClasspathEntry.CONTAINER;
	}

	public String getMemento() throws CoreException {
		Document doc = DebugPlugin.newDocument();
		Element node = doc.createElement("runtimeClasspathEntry"); //$NON-NLS-1$
		doc.appendChild(node);
		node.setAttribute("type", (new Integer(getType())).toString()); //$NON-NLS-1$
		node.setAttribute(
				"path", (new Integer(getClasspathProperty())).toString()); //$NON-NLS-1$
		switch (getType()) {
		case PROJECT:
			node.setAttribute("projectName", getPath().lastSegment()); //$NON-NLS-1$
			break;
		case ARCHIVE:
			IResource res = getResource();
			if (res == null) {
				node.setAttribute("externalArchive", getPath().toString()); //$NON-NLS-1$
			} else {
				node.setAttribute(
						"internalArchive", res.getFullPath().toString()); //$NON-NLS-1$
			}
			break;
		case VARIABLE:
		case CONTAINER:
			node.setAttribute("containerPath", getPath().toString()); //$NON-NLS-1$
			break;
		}
		if (getSourceAttachmentPath() != null) {
			node.setAttribute(
					"sourceAttachmentPath", getSourceAttachmentPath().toString()); //$NON-NLS-1$
		}
		if (getSourceAttachmentRootPath() != null) {
			node.setAttribute(
					"sourceRootPath", getSourceAttachmentRootPath().toString()); //$NON-NLS-1$
		}
		if (getJavaProject() != null) {
			node.setAttribute("javaProject", getJavaProject().getElementName()); //$NON-NLS-1$
		}
		return DebugPlugin.serializeDocument(doc);
	}

	public IResource getResource() {
		return null;
	}

	public void setSourceAttachmentPath(IPath path) {
		throw new UnsupportedOperationException();
	}

	public void setSourceAttachmentRootPath(IPath path) {
		throw new UnsupportedOperationException();
	}


	public int getClasspathProperty() {
		return property;
	}

	public void setClasspathProperty(int location) {
		property = location;
	}

	public String getLocation() {

		IPath path = null;
		switch (getType()) {
			case PROJECT :
				IJavaProject pro = (IJavaProject) JavaCore.create(getResource());
				if (pro != null) {
					try {
						path = pro.getOutputLocation();
					} catch (JavaModelException e) {
						Plugin.logError(e);
					}
				}
				break;
			case ARCHIVE :
				path = getPath();
				break;
			case VARIABLE :
				IClasspathEntry resolved = getResolvedEntry();
				if (resolved != null) {
					path = resolved.getPath();
				}
				break;
			case CONTAINER :
				break;
		}
		return resolveToOSPath(path);
	}

	/**
	 * Returns the OS path for the given aboslute or workspace relative path
	 */
	protected String resolveToOSPath(IPath path) {
		if (path != null) {
			IResource res = null;
			if (path.getDevice() == null) {
				// if there is no device specified, find the resource
				res = getResource(path);
			}
			if (res == null) {
				return path.toOSString();
			} 
			IPath location = res.getLocation();
			if (location != null) {
				return location.toOSString();
			}
		}
		return null;		
	}
	protected IResource getResource(IPath path) {
		if (path != null) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			// look for files or folders with the given path
			IFile[] files = root.findFilesForLocationURI(path.toFile().toURI());
			if (files.length > 0) {
				return files[0];
			}
			IContainer[] containers = root.findContainersForLocationURI(path.toFile().toURI());
			if (containers.length > 0) {
				return containers[0];
			}
			if (path.getDevice() == null) {
				// search relative to the workspace if no device present
				return root.findMember(path);
			} 
		}		
		return null;
	}
	public String getSourceAttachmentLocation() {
		return null;
	}

	public String getSourceAttachmentRootLocation() {
		return null;
	}

	public String getVariableName() {
		if (getType() == IRuntimeClasspathEntry.VARIABLE || getType() == IRuntimeClasspathEntry.CONTAINER) {
			return getPath().segment(0);
		}
		return null;
	}

	public IClasspathEntry getClasspathEntry() {
		return this;
	}

	public IJavaProject getJavaProject() {
		return null;
	}

	public void initializeFrom(Element memento) throws CoreException {
	}

	public boolean isComposite() {
		return true;
	}
	
	public String getTypeId() {
		return "runjettyrun.cotnainer." + getVariableName();
	}

	public IRuntimeClasspathEntry[] getRuntimeClasspathEntries(
			ILaunchConfiguration configuration) throws CoreException {
		return JavaRuntime.resolveRuntimeClasspathEntry(this, configuration);
	}

	public String getName() {
		return getVariableName();
	}

}
