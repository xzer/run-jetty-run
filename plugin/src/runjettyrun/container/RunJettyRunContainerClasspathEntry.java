package runjettyrun.container;

import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.internal.core.ClasspathAttribute;
import org.eclipse.jdt.internal.core.ClasspathEntry;

public class RunJettyRunContainerClasspathEntry extends ClasspathEntry {

	public RunJettyRunContainerClasspathEntry(String containerName) {
	    this(containerName,new ClasspathAttribute[0]);
	}

	public RunJettyRunContainerClasspathEntry(String containerName,List<ClasspathAttribute> attribute) {
	    this(containerName,attribute.toArray(new ClasspathAttribute[0]));
	}

	public RunJettyRunContainerClasspathEntry(String containerName,ClasspathAttribute[] attribute) {
	    super(0, ClasspathEntry.CPE_CONTAINER,
	    		new Path(containerName), new IPath[0], new IPath[0], null, null, null, null, false, null, false,
	    		attribute);
	}

}
