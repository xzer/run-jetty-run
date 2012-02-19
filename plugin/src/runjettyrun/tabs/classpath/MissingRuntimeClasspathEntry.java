package runjettyrun.tabs.classpath;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

public class MissingRuntimeClasspathEntry implements IRuntimeClasspathEntry{

	private IRuntimeClasspathEntry delegate;

	private String message ;

	public MissingRuntimeClasspathEntry(IRuntimeClasspathEntry delegate) {
		super();
		this.delegate = delegate;
	}

	public MissingRuntimeClasspathEntry(IRuntimeClasspathEntry delegate,
			String message) {
		super();
		this.delegate = delegate;
		this.message = message;
	}

	public int getType() {
		return delegate.getType();
	}

	public String getMemento() throws CoreException {
		return delegate.getMemento();
	}

	public IPath getPath() {
		return delegate.getPath();
	}

	public IResource getResource() {
		return delegate.getResource();
	}

	public IPath getSourceAttachmentPath() {
		return delegate.getSourceAttachmentPath();
	}

	public void setSourceAttachmentPath(IPath path) {
		delegate.setSourceAttachmentPath(path);
	}

	public IPath getSourceAttachmentRootPath() {
		return delegate.getSourceAttachmentRootPath();
	}

	public void setSourceAttachmentRootPath(IPath path) {
		delegate.setSourceAttachmentRootPath(path);
	}

	public int getClasspathProperty() {
		return delegate.getClasspathProperty();
	}

	public void setClasspathProperty(int location) {
		delegate.setClasspathProperty(location);
	}

	public String getLocation() {
		return delegate.getLocation();
	}

	public String getSourceAttachmentLocation() {
		return delegate.getSourceAttachmentLocation();
	}

	public String getSourceAttachmentRootLocation() {
		return delegate.getSourceAttachmentRootLocation();
	}

	public String getVariableName() {
		return delegate.getVariableName();
	}

	public IClasspathEntry getClasspathEntry() {
		return delegate.getClasspathEntry();
	}

	public IJavaProject getJavaProject() {
		return delegate.getJavaProject();
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


}
