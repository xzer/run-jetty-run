/*
 * $Id$
 * $HeadURL$
 *
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package runjettyrun;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author hillenius
 */
public class Plugin extends AbstractUIPlugin {
	/** The icon for the RunJettyRunWebApp launch type */
	private static final String JETTY_ICON_PATH = "/icons/jetty.gif";

	/** The plug-in ID. */
	public static final String PLUGIN_ID = "run_jetty_run";

	private static final String JETTY_ICON = PLUGIN_ID + ".jettyIcon";

	/** configuration attribute for the full class name of the bootstrap class. */
	public static final String BOOTSTRAP_CLASS_NAME = "runjettyrun.Bootstrap";

	/** configuration attribute for context of the web application. */
	public static final String ATTR_CONTEXT = Plugin.PLUGIN_ID
			+ ".CONTEXT_ATTR";

	/** configuration attribute for the web application directory. */
	public static final String ATTR_WEBAPPDIR = Plugin.PLUGIN_ID
			+ ".WEBAPPDIR_ATTR";

	/** configuration attribute for the port to run Jetty on. */
	public static final String ATTR_PORT = Plugin.PLUGIN_ID + ".PORT_ATTR";

	/**
	 * configuration attribute for the SSL enable to run Jetty on.
	 */
	public static final String ATTR_ENABLE_SSL = Plugin.PLUGIN_ID
			+ ".ENABLE_SSL_ATTR";

	/** configuration attribute for the SSL port to run Jetty on. */
	public static final String ATTR_SSL_PORT = Plugin.PLUGIN_ID
			+ ".SSL_PORT_ATTR";

	/** configuration attribute for the location of the keystore. */
	public static final String ATTR_KEYSTORE = Plugin.PLUGIN_ID
			+ ".KEYSTORE_ATTR";

	/** configuration attribute for the SSL port to run Jetty on. */
	public static final String ATTR_KEY_PWD = Plugin.PLUGIN_ID
			+ ".KEY_PWD_ATTR";

	/** configuration attribute for the SSL port to run Jetty on. */
	public static final String ATTR_PWD = Plugin.PLUGIN_ID + ".PWD_ATTR";
	/** configuration attribute for the scan interval seconds. */
	public static final String ATTR_SCANINTERVALSECONDS = Plugin.PLUGIN_ID
			+ ".SCANINTERVALSECONDS_ATTR";
	/** configuration attribute for the scan interval seconds. */
	public static final String ATTR_ENABLE_SCANNER = Plugin.PLUGIN_ID
			+ ".ENABLE_SCANNER_ATTR";

	/** used to calculate the jars to include. */
	public static final String JETTY_VERSION = "6.1.26";

	/**
	 * filter test-classes or not
	 */
	public static final String ATTR_ENABLE_MAVEN_TEST_CLASSES = Plugin.PLUGIN_ID
			+ ".ENABLE_MAVEN_TEST_CLASSES_ATTR";

	// The shared instance
	private static Plugin plugin;

	public Plugin() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Plugin getDefault() {
		return plugin;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {

		URL imageURL = getBundle().getEntry(JETTY_ICON_PATH);
		if (imageURL != null) {
			ImageDescriptor descriptor = ImageDescriptor
					.createFromURL(imageURL);
			reg.put(JETTY_ICON, descriptor);
		} else {
			logError("resource " + JETTY_ICON_PATH + " was not found");
		}
	}

	public static Image getJettyIcon() {
		return plugin.getImageRegistry().get(JETTY_ICON);
	}

	static public void logError(Exception e) {
		ILog log = plugin.getLog();
		StringWriter stringWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stringWriter));
		String msg = stringWriter.getBuffer().toString();
		Status status = new Status(IStatus.ERROR, getDefault().getBundle()
				.getSymbolicName(), IStatus.ERROR, msg, null);
		log.log(status);
	}

	static public void logError(String msg) {
		ILog log = plugin.getLog();
		Status status = new Status(IStatus.ERROR, getDefault().getBundle()
				.getSymbolicName(), IStatus.ERROR, msg + "\n", null);
		log.log(status);
	}
}
