package runjettyrun.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import runjettyrun.Plugin;
import runjettyrun.container.Jetty6PackageProvider;

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
		store.setDefault(PreferenceConstants.P_ENABLE_ECLIPSE_LISTENER, true);
		store.setDefault(PreferenceConstants.P_DEFAULT_ENABLE_SCANNER, false);
		store.setDefault(PreferenceConstants.P_AUTO_PORT, false);
		store.setDefault(PreferenceConstants.P_DEFAULT_JETTY_VERSION, Jetty6PackageProvider.VERSION);
	}

}
