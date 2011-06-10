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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import runjettyrun.container.Jetty6PackageProvider;
import runjettyrun.container.RunJettyRunContainerResolver;
import runjettyrun.extensions.IJettyPackageProvider;

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

	/**
	 * configuration attribute for need client auth.
	 */
	public static final String ATTR_ENABLE_NEED_CLIENT_AUTH = Plugin.PLUGIN_ID
			+ ".ENABLE_NEED_CLIENT_AUTH_ATTR";

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

	public static final String ATTR_SELECTED_JETTY_VERSION = Plugin.PLUGIN_ID
	+ ".SELECTED_JETTY_VERSION_ATTR";

	public static final String ATTR_ENABLE_PARENT_LOADER_PRIORITY = Plugin.PLUGIN_ID
			+ ".ENABLE_PARENT_LOADER_PRIORITY_ATTR";

	public static final String CONTAINER_RJR_JETTY = "RJRJetty";
	public static final String CONTAINER_RJR_JETTY_JNDI = "RJRJetty6JNDI";

	public static final String ATTR_ENABLE_JNDI = Plugin.PLUGIN_ID
			+ ".ENABLE_JNDI_ATTR";

	public static final String IPROVIDER_ID ="runjettyrun.jetty.providers";

	// The shared instance
	private static Plugin plugin;

	private List<IJettyPackageProvider> extensions;

	public Plugin() {
		JavaRuntime.addContainerResolver(new RunJettyRunContainerResolver(),
				CONTAINER_RJR_JETTY);
		JavaRuntime.addContainerResolver(new RunJettyRunContainerResolver(),
				CONTAINER_RJR_JETTY_JNDI);
		// containerIdentifier
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		extensions = new ArrayList<IJettyPackageProvider>();
		extensions.add(new Jetty6PackageProvider());
		initProviders();
		plugin = this;
	}

	private void initProviders(){
		IConfigurationElement[] config = Platform.getExtensionRegistry()
		.getConfigurationElementsFor(IPROVIDER_ID);
		for (IConfigurationElement e : config) {
			try {

					final Object o = e.createExecutableExtension("class");
					if (o instanceof IJettyPackageProvider) {
						extensions.add(((IJettyPackageProvider) o));
					}

			} catch (CoreException ex) {
				System.err.println(ex.getMessage());
			} catch (Exception ex){
				System.err.println(ex.getMessage());
			}
		}
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		extensions.clear();
		extensions = null;
	}

	public void addRunJettyRunPackageProvider(IJettyPackageProvider provider) {
		if (provider.getName() == null) {
			throw new IllegalArgumentException("provider's name can't be null.");
		}
		this.extensions.add(provider);
	}

	public void removeRunJettyRunPackageProvider(IJettyPackageProvider provider) {

		ArrayList<IJettyPackageProvider> list = new ArrayList<IJettyPackageProvider>();

		for (IJettyPackageProvider jpp : extensions) {
			if (jpp.getName() == null || provider.getName() == null)
				throw new IllegalStateException(
						"provider's name can't be null.");

			if (jpp.getName().equals(provider.getName()))
				list.add(jpp);

		}
		this.extensions.removeAll(list);
	}

	public IJettyPackageProvider[] getProviders(){
		return extensions.toArray(new IJettyPackageProvider[0]);
	}

	public boolean supportJetty(String version,int type){
		for (IJettyPackageProvider jpp : extensions) {
			if(jpp.accpet(version) && jpp.acceptType(type)){
				return true;
			}
		}
		return false;
	}

	public IRuntimeClasspathEntry[] getDefaultPackages(int type){
			//we assume here's the 0 be jetty 6
		IJettyPackageProvider pro = this.extensions.get(0);
		return pro.getPackage(pro.getJettyVersion(), type);
	}


	public IRuntimeClasspathEntry[] getPackages(String version,int type){
		for (IJettyPackageProvider jpp : extensions) {
			if(jpp.accpet(version) && jpp.acceptType(type)){
				return jpp.getPackage(version, type);
			}
		}
		return new IRuntimeClasspathEntry[0];
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
