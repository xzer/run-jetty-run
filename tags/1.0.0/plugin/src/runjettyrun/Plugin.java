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

	/** used to calculate the jars to include. */
	public static final String JETTY_VERSION = "6.1.6";

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

		URL imageURL = getBundle().getEntry("/icons/jetty.gif");
		if (imageURL != null) {
			ImageDescriptor descriptor = ImageDescriptor
					.createFromURL(imageURL);
			reg.put(JETTY_ICON, descriptor);
		} else {
			logError("resource " + "/icons/jetty.gif" + " was not found");
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
