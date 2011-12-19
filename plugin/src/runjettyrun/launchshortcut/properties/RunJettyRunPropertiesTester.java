package runjettyrun.launchshortcut.properties;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IResource;

import runjettyrun.tabs.RunJettyRunTab;

public class RunJettyRunPropertiesTester extends PropertyTester
{

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if(!(receiver instanceof IResource ))
			return false;

		IResource ir = (IResource) receiver;

		return RunJettyRunTab.isWebappProject(ir.getProject());
	}

}
