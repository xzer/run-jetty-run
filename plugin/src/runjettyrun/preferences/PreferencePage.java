package runjettyrun.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import runjettyrun.Plugin;
import runjettyrun.extensions.IJettyPackageProvider;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class PreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Plugin.getDefault().getPreferenceStore());
		setDescription("RunJettyRun Settings");
	}
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(
				PreferenceConstants.P_ENABLE_ECLIPSE_LISTENER,
				"&Enable Eclipse Listener to prevent Jetty leaks when Eclipse crash (Take effect after Eclipse restarted).",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				PreferenceConstants.P_DEFAULT_ENABLE_SCANNER,
				"Default Enable &Scanner when create new run configuration.",
				getFieldEditorParent()));
		addField(new BooleanFieldEditor(
				PreferenceConstants.P_AUTO_PORT,
				"Find a &unused port that between 10000~ 15000 when creating new run configuration.",
				getFieldEditorParent()));
		addField(new ComboFieldEditor(PreferenceConstants.P_DEFAULT_JETTY_VERSION, 
				"Default Jetty &version to use:", getJettyVersions(), getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {

	}

	private String[][] getJettyVersions() {
		IJettyPackageProvider[] providers = Plugin.getDefault().getProviders();
		String[][] result = new String[providers.length][2];
		for (int i = 0; i < providers.length; i++) {
			result[i][0] = providers[i].getJettyVersion();
			result[i][1] = providers[i].getJettyVersion();
		}
		return result;
	}
}