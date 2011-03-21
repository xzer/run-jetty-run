package runjettyrun.container;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.w3c.dom.Element;

@SuppressWarnings("restriction")
public class RunJettyRunContainerRuntimeClasspathEntry extends RuntimeClasspathEntry implements IRuntimeClasspathEntry2 {

	public RunJettyRunContainerRuntimeClasspathEntry(String containerName,
			int classpathProperty) {
		this(new RunJettyRunContainerClasspathEntry(containerName), classpathProperty);
	}


	//new RunJettyRunContainerClasspathEntry(Plugin.RJR_CONTAINER)
	public RunJettyRunContainerRuntimeClasspathEntry(IClasspathEntry entry,
			int classpathProperty) {
		super(entry, classpathProperty);
	}

	@Override
	public void initializeFrom(Element memento) throws CoreException {
	}

	@Override
	public String getTypeId() {
		return "runjettyrun.cotnainer";
	}

	@Override
	public boolean isComposite() {
		return true;
	}

	@Override
	public IRuntimeClasspathEntry[] getRuntimeClasspathEntries(
			ILaunchConfiguration configuration) throws CoreException {
		return JavaRuntime.resolveRuntimeClasspathEntry(this, configuration);
	}

	@Override
	public String getName() {
		return getVariableName();
	}

}
