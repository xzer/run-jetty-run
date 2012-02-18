package runjettyrun.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import runjettyrun.Plugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_EnableEclipseListener, true);
		store.setDefault(PreferenceConstants.P_DefaultEnableScanner, false);
		store.setDefault(PreferenceConstants.P_AUTO_PORT, false);

	}

}
