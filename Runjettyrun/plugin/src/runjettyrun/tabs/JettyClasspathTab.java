package runjettyrun.tabs;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

import runjettyrun.Plugin;
import runjettyrun.RunJettyRunMessages;
import runjettyrun.tabs.classpath.UserClassesClasspathModel;

public class JettyClasspathTab extends AbstractClasspathTab{

	public JettyClasspathTab() {
		super("jetty",RunJettyRunMessages.JettyClasspathTab_name);
	}

	public String getHeader() {
		return "Jetty Classpath , only for updating jetty version or add jetty related libs.";
	}

	public String getCustomAttributeName() {
		return Plugin.ATTR_JETTY_CUSTOM_CLASSPATH;
	}

	public String getNonCheckedAttributeName() {
		return  Plugin.ATTR_JETTY_CLASSPATH_NON_CHECKED;
	}

	public UserClassesClasspathModel createClasspathModel(ILaunchConfiguration configuration)
			throws Exception {
		UserClassesClasspathModel theModel= new UserClassesClasspathModel("Default Jetty Classpath","Custom Jetty Classpath");
		IRuntimeClasspathEntry[] entries= getClasspathProvider().computeUnresolvedJettyClasspath(configuration);
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry entry = entries[i];
			switch (entry.getClasspathProperty()) {
				case IRuntimeClasspathEntry.USER_CLASSES:
					theModel.addEntry(UserClassesClasspathModel.USER, entry);
					break;
			}
		}

		entries= getClasspathProvider().computeUnresolvedCustomClasspath(configuration,
				getCustomAttributeName());

		for (int i = 0; i < entries.length; i++) {
			theModel.addEntry(UserClassesClasspathModel.CUSTOM, entries[i]);
		}

		return theModel;

	}

}
