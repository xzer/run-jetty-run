package runjettyrun.tabs;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

import runjettyrun.Plugin;
import runjettyrun.tabs.classpath.UserClassesClasspathModel;

public class WebcontextClasspathTab extends AbstractClasspathTab {

	public WebcontextClasspathTab() {
		super("webcontext", "Webapp Classpath");
	}

	public String getHeader() {
		return "If you don't want or can't change project classpath for some reason , you could edit it here.";
	}

	public String getCustomAttributeName() {
		return Plugin.ATTR_WEB_CONTEXT_CUSTOM_CLASSPATH;
	}

	public String getNonCheckedAttributeName() {
		return Plugin.ATTR_WEB_CONTEXT_CLASSPATH_NON_CHECKED;
	}

	public UserClassesClasspathModel createClasspathModel(
			ILaunchConfiguration configuration) throws Exception {
		UserClassesClasspathModel theModel = new UserClassesClasspathModel();
		IRuntimeClasspathEntry[] entries = getClasspathProvider().computeWebcontextClassPath(configuration);
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry entry = entries[i];
			theModel.addEntry(UserClassesClasspathModel.USER, entry);
		}

		entries = getClasspathProvider().computeUnresolvedCustomClasspath(
				configuration, getCustomAttributeName());

		for (int i = 0; i < entries.length; i++) {
			theModel.addEntry(UserClassesClasspathModel.CUSTOM, entries[i]);
		}

		return theModel;

	}

}
