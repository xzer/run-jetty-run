package runjettyrun.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import runjettyrun.Plugin;

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
		addField(
				new BooleanFieldEditor(PreferenceConstants.P_EnableEclipseListener,
						"&Enable Eclipse Listener to prevent Jetty leaks when Eclipse crash (Take effect after Eclipse restarted).",
						getFieldEditorParent()));
		addField(
				new BooleanFieldEditor(PreferenceConstants.P_DefaultEnableScanner,
						"Default Enable &Scanner when create new run configuration.",
						getFieldEditorParent()));
	}

	public void init(IWorkbench workbench) {

	}
}