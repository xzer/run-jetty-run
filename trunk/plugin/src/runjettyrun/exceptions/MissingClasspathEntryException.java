package runjettyrun.exceptions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

public class MissingClasspathEntryException extends Exception{
	private static final long serialVersionUID = -8259339108508030627L;

	private CoreException ex;
	private IRuntimeClasspathEntry resolvingEntry;

	public MissingClasspathEntryException(CoreException ex,IRuntimeClasspathEntry resolvingEntry) {
		super();
		this.ex = ex;
		this.resolvingEntry = resolvingEntry;
	}

	public CoreException getOriginalException() {
		return ex;
	}

	public IRuntimeClasspathEntry getResolvingEntry() {
		return resolvingEntry;
	}

	public void setResolvingEntry(IRuntimeClasspathEntry resolvingEntry) {
		this.resolvingEntry = resolvingEntry;
	}

	public String getMessage() {
		return ex.getMessage();
	}

	public String getLocalizedMessage() {
		return ex.getLocalizedMessage();
	}


}
