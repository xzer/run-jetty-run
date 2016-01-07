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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import runjettyrun.container.Jetty6PackageProvider;
import runjettyrun.extensions.IJettyPackageProvider;
import runjettyrun.preferences.PreferenceConstants;
import runjettyrun.utils.PortUtil;

/**
 * The activator class controls the plug-in life cycle.
 *
 * @author hillenius
 */
public class Plugin extends AbstractUIPlugin {
	/** The icon for the RunJettyRunWebApp launch type */
	private static final String JETTY_ICON_PATH = "/icons/jetty.gif"; //$NON-NLS-1$

	/** The plug-in ID. */
	public static final String PLUGIN_ID = "run_jetty_run"; //$NON-NLS-1$

	private static final String JETTY_ICON = PLUGIN_ID + ".jettyIcon"; //$NON-NLS-1$

	/** configuration attribute for the full class name of the bootstrap class. */
	public static final String BOOTSTRAP_CLASS_NAME = "runjettyrun.Bootstrap"; //$NON-NLS-1$

	/** configuration attribute for context of the web application. */
	public static final String ATTR_CONTEXT = Plugin.PLUGIN_ID
			+ ".CONTEXT_ATTR"; //$NON-NLS-1$

	/** configuration attribute for the web application directory. */
	public static final String ATTR_WEBAPPDIR = Plugin.PLUGIN_ID
			+ ".WEBAPPDIR_ATTR"; //$NON-NLS-1$

	/** configuration attribute for the port to run Jetty on. */
	public static final String ATTR_PORT = Plugin.PLUGIN_ID + ".PORT_ATTR"; //$NON-NLS-1$

	/**
	 * configuration attribute for the SSL enable to run Jetty on.
	 */
	public static final String ATTR_ENABLE_SSL = Plugin.PLUGIN_ID
			+ ".ENABLE_SSL_ATTR"; //$NON-NLS-1$

	/**
	 * configuration attribute for need client auth.
	 */
	public static final String ATTR_ENABLE_NEED_CLIENT_AUTH = Plugin.PLUGIN_ID
			+ ".ENABLE_NEED_CLIENT_AUTH_ATTR"; //$NON-NLS-1$

	/** configuration attribute for the SSL port to run Jetty on. */
	public static final String ATTR_SSL_PORT = Plugin.PLUGIN_ID
			+ ".SSL_PORT_ATTR"; //$NON-NLS-1$

	/** configuration attribute for the location of the keystore. */
	public static final String ATTR_KEYSTORE = Plugin.PLUGIN_ID
			+ ".KEYSTORE_ATTR"; //$NON-NLS-1$

	/** configuration attribute for the SSL port to run Jetty on. */
	public static final String ATTR_KEY_PWD = Plugin.PLUGIN_ID
			+ ".KEY_PWD_ATTR"; //$NON-NLS-1$

	/** configuration attribute for the SSL port to run Jetty on. */
	public static final String ATTR_PWD = Plugin.PLUGIN_ID + ".PWD_ATTR"; //$NON-NLS-1$
	/** configuration attribute for the scan interval seconds. */
	public static final String ATTR_SCANINTERVALSECONDS = Plugin.PLUGIN_ID
			+ ".SCANINTERVALSECONDS_ATTR"; //$NON-NLS-1$
	/** configuration attribute for the scan interval seconds. */
	public static final String ATTR_ENABLE_SCANNER = Plugin.PLUGIN_ID
			+ ".ENABLE_SCANNER_ATTR"; //$NON-NLS-1$

	/** used to calculate the jars to include. */
	public static final String JETTY_VERSION = "6.1.26"; //$NON-NLS-1$

	public static final String ATTR_SELECTED_JETTY_VERSION = Plugin.PLUGIN_ID
	+ ".SELECTED_JETTY_VERSION_ATTR"; //$NON-NLS-1$

	public static final String ATTR_ENABLE_PARENT_LOADER_PRIORITY = Plugin.PLUGIN_ID
			+ ".ENABLE_PARENT_LOADER_PRIORITY_ATTR"; //$NON-NLS-1$

	public static final String ATTR_JETTY_XML_PATH = Plugin.PLUGIN_ID
	+ ".JETTY_XML_PATH"; //$NON-NLS-1$


	public static final String CONTAINER_RJR_JETTY = "RJRJetty"; //$NON-NLS-1$
	public static final String CONTAINER_RJR_JETTY_JNDI = "RJRJettyJNDI"; //$NON-NLS-1$

	public static final String ATTR_ENABLE_JNDI = Plugin.PLUGIN_ID
			+ ".ENABLE_JNDI_ATTR"; //$NON-NLS-1$

	public static final String IPROVIDER_ID ="runjettyrun.jetty.providers"; //$NON-NLS-1$

	public static final String ATTR_SHOW_ADVANCE = Plugin.PLUGIN_ID
			+ RunJettyRunMessages.RJRPlugin_0;


	public static final String ATTR_JETTY_CLASSPATH_NON_CHECKED = Plugin.PLUGIN_ID
	+ ".JETTY_CLASSPATH_NON_CHECKED"; //$NON-NLS-1$

	public static final String ATTR_JETTY_CUSTOM_CLASSPATH = Plugin.PLUGIN_ID
	+ ".JETTY_CUSTOM_CLASSPATH"; //$NON-NLS-1$

	public static final String ATTR_WEB_CONTEXT_CLASSPATH_NON_CHECKED = Plugin.PLUGIN_ID
	+ ".WEB_CONTEXT_CLASSPATH_NON_CHECKED"; //$NON-NLS-1$

	public static final String ATTR_WEB_CONTEXT_CUSTOM_CLASSPATH = Plugin.PLUGIN_ID
	+ ".WEB_CONTEXT_CUSTOM_CLASSPATH"; //$NON-NLS-1$


	public static final String ATTR_SCAN_FOLDER_NON_CHECKED = Plugin.PLUGIN_ID
	+ ".SCAN_FOLDER_NON_CHECKED"; //$NON-NLS-1$

	public static final String ATTR_CUSTOM_SCAN_FOLDER = Plugin.PLUGIN_ID
	+ ".CUSTOM_SCAN_FOLDER"; //$NON-NLS-1$

	public static final String ATTR_IGNORE_SCAN_CLASS_WHEN_DEBUG_MODE = Plugin.PLUGIN_ID
	+ ".IGNORE_SCAN_CLASS_WHEN_DEBUG_MODE"; //$NON-NLS-1$

	// The shared instance
	private static Plugin plugin;

	private int controlPort = -1;
	private boolean listenerEnabled = false;

	private List<IJettyPackageProvider> extensions;

	public Plugin() {
	}

	public boolean isListenerEnable(){
		return  getPreferenceStore().getBoolean(PreferenceConstants.P_ENABLE_ECLIPSE_LISTENER);
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		extensions = new ArrayList<IJettyPackageProvider>();
		extensions.add(new Jetty6PackageProvider());
		initProviders();
		plugin = this;

		if(isListenerEnable()) enableListenter();
	}

	/**
	 * We create a listener for RJR to handle Jetty instance leak issue.
	 */
	public void enableListenter(){
		if(!listenerEnabled){
			listenerEnabled = true;
			controlPort = PortUtil.findAAvailablePort(50000,60000);

			if(controlPort != -1 ){
				Thread runnable= new Thread() {
					public void run()  {

						try {
							ServerSocket server = new ServerSocket(controlPort);

							while(true) {
								try{
									Socket sock = server.accept();
									sock.getOutputStream().write(new byte[]{1,2});
									sock.getOutputStream().close();
									Thread.sleep(5000L);
								}catch(Exception er){
								}
							}

						} catch (IOException e) {
						}

					}
				};
				runnable.start();
			}
		}
	}

	private void initProviders(){
		IConfigurationElement[] config = Platform.getExtensionRegistry()
		.getConfigurationElementsFor(IPROVIDER_ID);
		for (IConfigurationElement e : config) {
			try {

					final Object o = e.createExecutableExtension("class"); //$NON-NLS-1$
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
			throw new IllegalArgumentException(RunJettyRunMessages.RJRPlugin_provider_null);
		}
		this.extensions.add(provider);
	}

	public void removeRunJettyRunPackageProvider(IJettyPackageProvider provider) {

		ArrayList<IJettyPackageProvider> list = new ArrayList<IJettyPackageProvider>();

		for (IJettyPackageProvider jpp : extensions) {
			if (jpp.getName() == null || provider.getName() == null)
				throw new IllegalStateException(
						RunJettyRunMessages.RJRPlugin_provider_null);

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
		String defaultVer = getPreferenceStore().getString(
				PreferenceConstants.P_DEFAULT_JETTY_VERSION);
		for (IJettyPackageProvider pro : this.extensions) {
			String jettyVer = pro.getJettyVersion();
			if (jettyVer.equalsIgnoreCase(defaultVer)) {
				return pro.getPackage(jettyVer, type);
			}
		}
		//back to jetty 6
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

	public int getListenerPort() {
		return controlPort;
	}
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {

		URL imageURL = getBundle().getEntry(JETTY_ICON_PATH);
		if (imageURL != null) {
			ImageDescriptor descriptor = ImageDescriptor
					.createFromURL(imageURL);
			reg.put(JETTY_ICON, descriptor);
		} else {
			logError(MessageFormat.format(RunJettyRunMessages.RJRPlugin_resource_not_found, new Object[] {JETTY_ICON_PATH}));
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
				.getSymbolicName(), IStatus.ERROR, msg + "\n", null); //$NON-NLS-1$
		log.log(status);
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}
	public static void statusDialog(IStatus status) {
		switch (status.getSeverity()) {
		case IStatus.ERROR:
			statusDialog(RunJettyRunMessages.RJRPlugin_error, status);
			break;
		case IStatus.WARNING:
			statusDialog(RunJettyRunMessages.RJRPlugin_warning, status);
			break;
		case IStatus.INFO:
			statusDialog(RunJettyRunMessages.RJRPlugin_info, status);
			break;
		}
	}

	public static void statusDialog(String title, IStatus status) {
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			switch (status.getSeverity()) {
			case IStatus.ERROR:
				ErrorDialog.openError(shell, title, null, status);
				break;
			case IStatus.WARNING:
				MessageDialog.openWarning(shell, title, status.getMessage());
				break;
			case IStatus.INFO:
				MessageDialog.openInformation(shell, title, status.getMessage());
				break;
			}
		}
	}

}
