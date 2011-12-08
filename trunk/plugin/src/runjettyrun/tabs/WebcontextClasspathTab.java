package runjettyrun.tabs;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

import runjettyrun.Plugin;
import runjettyrun.tabs.classpath.UserClassesClasspathModel;

public class WebcontextClasspathTab extends AbstractClasspathTab {

	public WebcontextClasspathTab() {
		super("webcontext", "Webapp Classpath");
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
			switch (entry.getClasspathProperty()) {
				case IRuntimeClasspathEntry.USER_CLASSES:
					theModel.addEntry(UserClassesClasspathModel.USER, entry);
					break;
			}
		}

		entries = getClasspathProvider().computeUnresolvedCustomClasspath(
				configuration, getCustomAttributeName());

		for (int i = 0; i < entries.length; i++) {
			theModel.addEntry(UserClassesClasspathModel.CUSTOM, entries[i]);
		}

		return theModel;

	}

}
