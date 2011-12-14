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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

import runjettyrun.utils.RunJettyRunClasspathResolver;
import runjettyrun.utils.RunJettyRunClasspathUtil;
import runjettyrun.utils.RunJettyRunLaunchConfigurationUtil;

/**
 * Launch configuration type for Jetty. Based on
 * org.eclipse.jdt.launching.JavaLaunchDelegate.
 *
 * @author hillenius
 */
public class JettyLaunchConfigurationType extends
		AbstractJavaLaunchConfigurationDelegate {

	private static HashMap<String, ILaunch> launcher = new HashMap<String, ILaunch>();
	private JettyLaunchConfigurationClassPathProvider provider = new JettyLaunchConfigurationClassPathProvider();

	/**
	 * Here's what the WebApp classpath. That means all the classpath here just
	 * like WEB-INF/classes or WEB-INF/lib , which is only for the specific
	 * webapp project and will not used for the Jetty Instance.
	 *
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private String getWebappClasspath(ILaunchConfiguration configuration)
			throws CoreException {
		String[] webAppClasspathArray = getProjectClasspath(configuration);
		String webAppClasspath = "";

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < webAppClasspathArray.length; i++) {
			String path = webAppClasspathArray[i];
			if (sb.length() > 0)
				sb.append(File.pathSeparator);
			sb.append(path);
		}
		webAppClasspath = sb.toString();

		/**
		 * The smallest limit for windows XP is 2048
		 */
		if (webAppClasspath.length() > 1024) {
			File f = prepareClasspathFile(configuration, webAppClasspath);
			webAppClasspath = "file://" + f.getAbsolutePath();
		}

		return webAppClasspath;

	}

	private String getScanlist(ILaunchConfiguration configuration) throws CoreException{
		Set<String> paths = provider.getAllScanPathList(configuration);
		Map<String,String> nonChecked = getAttributeFromLaunchConfiguration(configuration, Plugin.ATTR_SCAN_FOLDER_NON_CHECKED);

		StringBuffer scanList = new StringBuffer();

		for(String path:paths){
			if(nonChecked.containsKey(path) && nonChecked.get(path).equals("1")){
				continue;
			}
			if (scanList.length() > 0){
				scanList.append(File.pathSeparator);
			}
			scanList.append(path);
		}
		File f = prepareConfigFile(configuration, scanList.toString(),".scanlist");
		return "file://" + f.getAbsolutePath();

	}

	/**
	 * Get working directory's absolute folder path.
	 *
	 * @param configuration
	 * @return return the path if exist, or return null.
	 * @throws CoreException
	 */
	private String getWorkingDirectoryAbsolutePath(
			ILaunchConfiguration configuration) throws CoreException {
		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null)
			workingDirName = workingDir.getAbsolutePath();

		return workingDirName;
	}

	/**
	 * I prefer to change the name to JettyClasspath to make it more clear. This
	 * classpath means how the RunJettyRun to get the Jetty bundle.
	 *
	 * Note:it only used the USER_CLASSES classpaths , not including
	 * BOOTSTRAP_CLASSES. If you change the classpath , that might means you are
	 * changing the Jetty version or something on it.
	 *
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private String[] getJettyClasspath(ILaunchConfiguration configuration)
			throws CoreException {
		String[] paths = getResolvedJettyClasspath(configuration);
		Set<String> finalPaths = new HashSet<String>();
		for (String path : paths) {
			finalPaths.add(path);
		}
		finalPaths.addAll(getJettyCustomClasspath(configuration));

		Map<String,String> checked = getAttributeFromLaunchConfiguration(configuration, Plugin.ATTR_JETTY_CLASSPATH_NON_CHECKED);

		HashSet<String> results = new HashSet<String>();

		for (String path : finalPaths) {
			if (!checked.containsKey(path) ||checked.get(path).equals("0")) {
				results.add(path);
			}
		}

		return results.toArray(new String[0]);

	}
	public String[] getResolvedJettyClasspath(ILaunchConfiguration configuration)
		throws CoreException {
		IRuntimeClasspathEntry[] entries = JavaRuntime
				.computeUnresolvedRuntimeClasspath(configuration);

		entries = JavaRuntime.resolveRuntimeClasspath(filterProejctEntries(entries), configuration);
		List<String> userEntries = new ArrayList<String>(entries.length);
		Set<String> set = new HashSet<String>(entries.length);
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
				String location = entries[i].getLocation();
				if (location != null) {
					if (!set.contains(location)) {
						userEntries.add(location);
						set.add(location);
					}
				}
			}
		}
		return (String[]) userEntries.toArray(new String[userEntries.size()]);
	}

	private IRuntimeClasspathEntry[] filterProejctEntries(IRuntimeClasspathEntry[] entries){

		if(entries == null) {
			return null;
		}
		List<IRuntimeClasspathEntry> items = new ArrayList<IRuntimeClasspathEntry>();

		for(IRuntimeClasspathEntry entry:entries){
			if(entry.getType() == IRuntimeClasspathEntry.PROJECT){
				continue;
			}

			if(RunJettyRunClasspathUtil.isDefaultProjectClasspathEntry(entry)){
				continue;
			}

			items.add(entry);

		}
		return items.toArray(new IRuntimeClasspathEntry[0]);

	}

	@SuppressWarnings("unchecked")
	private Map<String,String> getAttributeFromLaunchConfiguration(
			ILaunchConfiguration configuration, String attribute) {
		Map<String,String> checked = null;
		try {
			checked = (Map<String,String>) configuration.getAttribute(attribute,
					(Map<String,String>) null);
		} catch (CoreException e) {
		}
		if(checked == null){
			checked = new HashMap<String,String>();
		}

		return checked;

	}

	private Set<String> getCustomClasspath(ILaunchConfiguration configuration,
			String attribute) throws CoreException {
		IRuntimeClasspathEntry[] entries = provider
				.computeUnresolvedCustomClasspath(configuration, attribute);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);

		Set<String> set = new HashSet<String>(entries.length);
		for (int i = 0; i < entries.length; i++) {
			String location = entries[i].getLocation();
			if (location != null) {
				if (!set.contains(location)) {
					set.add(location);
				}
			}
		}
		return set;
	}

	private Set<String> getJettyCustomClasspath(
			ILaunchConfiguration configuration) throws CoreException {
		return getCustomClasspath(configuration,
				Plugin.ATTR_JETTY_CUSTOM_CLASSPATH);
	}

	private Set<String> getWebappCustomClasspath(
			ILaunchConfiguration configuration) throws CoreException {
		return getCustomClasspath(configuration,
				Plugin.ATTR_WEB_CONTEXT_CUSTOM_CLASSPATH);
	}

	/**
	 * get Runtime arguments , and prepare the webapp classpath for the program.
	 *
	 * @param configuration
	 * @param oringinalVMArguments
	 * @return
	 * @throws CoreException
	 */
	private String[] getRuntimeArguments(ILaunchConfiguration configuration,
			String[] oringinalVMArguments)
			throws CoreException {
		List<String> runtimeVmArgs = getJettyArgs(configuration);

		boolean maxperm = false;
		for (String str : oringinalVMArguments) {
			if (str != null && str.indexOf("-XX:MaxPermSize") != -1)
				maxperm = true;
		}

		if (!maxperm) {
			runtimeVmArgs.add("-XX:MaxPermSize=128m");
		}

		// Here the classpath is really for web app.
		runtimeVmArgs.add("-Drjrclasspath=" +
				getWebappClasspath(configuration));

		runtimeVmArgs.add("-Drjrscanlist=" + getScanlist(configuration));

		runtimeVmArgs.add("-DrjrResourceMapping="
				+ getLinkedResourceMapping(configuration));

		if (Plugin.getDefault().isListenerEnable()) {
			runtimeVmArgs.add("-DrjrEclipseListener="
					+ Plugin.getDefault().getListenerPort());
		}

		runtimeVmArgs.addAll(Arrays.asList(oringinalVMArguments));

		return runtimeVmArgs.toArray(new String[runtimeVmArgs.size()]);
	}

	private String getLinkedResourceInResource(IContainer root,
			IContainer folder) {
		StringBuffer sb = new StringBuffer();
		try {
			for (IResource ir : folder.members()) {
				if (ir instanceof IFolder) {
					if (ir.isLinked()) {
						sb.append(ir.getProjectRelativePath().makeRelativeTo(
								root.getProjectRelativePath())
								+ "=" + ir.getRawLocation() + ";");
					}
					sb.append(getLinkedResourceInResource(root, (IFolder) ir));
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	private String getLinkedResourceMapping(ILaunchConfiguration conf) {
		try {
			IJavaProject proj = getJavaProject(conf);
			String webappPath = conf.getAttribute(Plugin.ATTR_WEBAPPDIR, "");
			if (proj == null || "".equals(webappPath))
				return null;

			IContainer webappDir = null;
			if ("/".equals(webappPath)){
				webappDir = proj.getProject();
			}else{
				webappDir = proj.getProject().getFolder(webappPath);
			}

			if (webappDir.exists()){
				return getLinkedResourceInResource(webappDir, webappDir);
			}

		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * The launcher !
	 */
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		/*
		 * for those terminate by our self .
		 *
		 * @see #terminateOldRJRLauncher
		 */
		if (!RunJettyRunLaunchConfigurationUtil.validation(configuration)) {
			throw new CoreException(
					new Status(
							IStatus.ERROR,
							Plugin.PLUGIN_ID,
							01,
							" Invalid run configuration , please check the configuration ",
							null));
		}

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(
				MessageFormat.format("{0}...", configuration.getName()), 3); //$NON-NLS-1$

		// check for cancellation
		if (monitor.isCanceled())
			return;

		try {
			monitor.subTask("verifying installation");

			// Program & VM arguments
			ExecutionArguments execArgs = new ExecutionArguments(
					getVMArguments(configuration),
					getProgramArguments(configuration));

			// Create VM configuration
			// here the classpath means for the Jetty Server , not for the
			// application! by TonyQ 2011/3/7
			VMRunnerConfiguration runConfig = new VMRunnerConfiguration(
					Plugin.BOOTSTRAP_CLASS_NAME,
					getJettyClasspath(configuration));

			// logger to list classpaths
			// for(String path:getJettyClasspath(configuration)){
			// System.out.println("path:"+path);
			// }
			runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());

			// Environment variables
			runConfig.setEnvironment(getEnvironment(configuration));

			// Here prepare the classpath is really for webapp in Runtime
			// Arguments , too.
			runConfig.setVMArguments(getRuntimeArguments(configuration,
					execArgs.getVMArgumentsArray()));

			runConfig
					.setWorkingDirectory(getWorkingDirectoryAbsolutePath(configuration));
			runConfig
					.setVMSpecificAttributesMap(getVMSpecificAttributesMap(configuration));

			// Boot path
			runConfig.setBootClassPath(getBootpath(configuration));

			// check for cancellation
			if (monitor.isCanceled())
				return;

			// stop in main
			prepareStopInMain(configuration);

			// done the verification phase
			monitor.worked(1);
			monitor.subTask("Creating source locator");
			// set the default source locator if required
			setDefaultSourceLocator(launch, configuration);

			monitor.worked(1);

			synchronized (configuration) {
				terminateOldRJRLauncher(configuration, launch);
				// Launch the configuration - 1 unit of work
				getVMRunner(configuration, mode)
						.run(runConfig, launch, monitor);
				registerRJRLauncher(configuration, launch);
			}

			// check for cancellation
			if (monitor.isCanceled())
				return;

		} finally {
			monitor.done();
		}

	}

	/**
	 * A private helper to prepare a classpath file to workspace metadata, we
	 * use this to prevent classpath too long which was caused the problem for
	 * reaching Windows command length limitation.
	 *
	 * @param configuration
	 * @param classpath
	 * @return
	 */
	private File prepareClasspathFile(ILaunchConfiguration configuration,
			String classpath) {
		return prepareConfigFile(configuration,classpath,".classpath");
	}

	private File prepareConfigFile(ILaunchConfiguration configuration,
			String content ,String extension) {
		IPath path = Plugin.getDefault().getStateLocation()
				.append(configuration.getName() + extension);
		File f = path.toFile();
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(f, false), "UTF8"));
			out.write(content);
			out.close();
			return f;
		} catch (IOException e) {
			return null;
		}

	}

	/**
	 * Terminate old Run-Jetty-Run launcher which use same port if exist.
	 *
	 * @param configuration
	 * @param launch
	 * @throws CoreException
	 */
	private static void terminateOldRJRLauncher(
			ILaunchConfiguration configuration, ILaunch launch)
			throws CoreException {
		String port = configuration.getAttribute(Plugin.ATTR_PORT, "");
		String sslPort = configuration.getAttribute(Plugin.ATTR_SSL_PORT, "");
		boolean enableSSL = configuration.getAttribute(Plugin.ATTR_ENABLE_SSL,
				false);

		if (!"".equals(port) && launcher.containsKey(port)) {
			terminateLaunch(launcher.get(port));
			launcher.remove(port);
		}
		if (enableSSL && !"".equals(sslPort) && launcher.containsKey(sslPort)) {
			terminateLaunch(launcher.get(sslPort));
			launcher.remove(sslPort);
		}
	}

	private static void terminateLaunch(ILaunch launch) throws DebugException {
		if (!launch.isTerminated()) {
			IProcess[] processes = launch.getProcesses();
			if (processes != null) {
				for (IProcess proc : processes) {
					if (proc != null)
						proc.terminate();
				}
			}
		}
	}

	/**
	 * register a port for RJR Launcher
	 *
	 * @param configuration
	 * @param launch
	 * @throws CoreException
	 */
	private static void registerRJRLauncher(ILaunchConfiguration configuration,
			ILaunch launch) throws CoreException {
		String port = configuration.getAttribute(Plugin.ATTR_PORT, "");
		String sslPort = configuration.getAttribute(Plugin.ATTR_SSL_PORT, "");
		boolean enableSSL = configuration.getAttribute(Plugin.ATTR_ENABLE_SSL,
				false);

		if (!"".equals(port))
			launcher.put(port, launch);
		if (enableSSL && !"".equals(sslPort))
			launcher.put(sslPort, launch);
	}

	private List<String> getJettyArgs(ILaunchConfiguration configuration)
			throws CoreException {

		List<String> runtimeVmArgs = new ArrayList<String>();

		addOptionalAttr(configuration, runtimeVmArgs, Plugin.ATTR_CONTEXT,
				"context");
		addWebappAttr(configuration, runtimeVmArgs, Plugin.ATTR_WEBAPPDIR);

		addOptionalAttr(configuration, runtimeVmArgs, Plugin.ATTR_PORT, "port");

		addOptionalAttr(configuration, runtimeVmArgs, Plugin.ATTR_SSL_PORT,
				"sslport");
		addOptionalAttr(configuration, runtimeVmArgs, Plugin.ATTR_KEYSTORE,
				"keystore");
		addOptionalAttr(configuration, runtimeVmArgs, Plugin.ATTR_KEY_PWD,
				"keypassword");
		addOptionalAttr(configuration, runtimeVmArgs, Plugin.ATTR_PWD,
				"password");

		addOptionalAttr(configuration, runtimeVmArgs,
				Plugin.ATTR_JETTY_XML_PATH, "jettyXMLPath");

		addOptionalAttr(configuration, runtimeVmArgs,
				Plugin.ATTR_SCANINTERVALSECONDS, "scanintervalseconds");

		addOptionalAttr(configuration, runtimeVmArgs,
				Plugin.ATTR_EXCLUDE_CLASSPATH, "excludedclasspath");

		addOptionalAttrx(configuration, runtimeVmArgs,
				Plugin.ATTR_ENABLE_SCANNER, "enablescanner");

		addOptionalAttrx(configuration, runtimeVmArgs,
				Plugin.ATTR_SCANNER_SCAN_WEBINF, "scanWEBINF");

		addOptionalAttrx(configuration, runtimeVmArgs, Plugin.ATTR_ENABLE_SSL,
				"enablessl");

		addOptionalAttrx(configuration, runtimeVmArgs,
				Plugin.ATTR_ENABLE_NEED_CLIENT_AUTH, "needclientauth");

		addOptionalAttrx(configuration, runtimeVmArgs,
				Plugin.ATTR_ENABLE_PARENT_LOADER_PRIORITY,
				"parentloaderpriority");

		addOptionalAttrx(configuration, runtimeVmArgs, Plugin.ATTR_ENABLE_JNDI,
				"enbaleJNDI");

		return runtimeVmArgs;
	}

	private void addWebappAttr(ILaunchConfiguration configuration,
			List<String> runtimeVmArgs, String cfgAttr) throws CoreException {
		IJavaProject proj = this.getJavaProject(configuration);
		if (proj == null)
			return;

		String value = configuration.getAttribute(cfgAttr, "");

		if ("/".equals(value)) {
			IPath path = (IPath) proj.getResource().getLocation().clone();
			path.makeAbsolute();
			value = path.toOSString();
		} else {
			if (proj.getProject().getFolder(value).getRawLocation() == null) {
				throw new IllegalStateException(
						"raw location shouldn't be null");
			}
			value = proj.getProject().getFolder(value).getRawLocation()
					.toOSString();
		}

		if (value.length() == 0)
			return;

		String arg = "-Drjr" + "webapp" + "=" + value + "";
		runtimeVmArgs.add(arg);
		return;
	}

	private void addOptionalAttr(ILaunchConfiguration configuration,
			List<String> runtimeVmArgs, String cfgAttr, String argName)
			throws CoreException {
		String value = configuration.getAttribute(cfgAttr, "");

		if (value.length() == 0)
			return;
		String arg = "-Drjr" + argName + "=" + value;
		runtimeVmArgs.add(arg);
		return;
	}

	private void addOptionalAttrx(ILaunchConfiguration configuration,
			List<String> runtimeVmArgs, String cfgAttr, String argName)
			throws CoreException {
		Boolean value = configuration.getAttribute(cfgAttr, false);
		String arg = "-Drjr" + argName + "=" + value;
		runtimeVmArgs.add(arg);
		return;
	}

	/**
	 * Returns the class path to be used by the web app context (not by Jetty,
	 * or as JRE Bootstrap).
	 * <p>
	 * Added by James Synge.
	 *
	 * Copied from {@link AbstractJavaLaunchConfigurationDelegate} so that I can
	 * eliminate everything that should be in WEB-INF/lib, but is not supposed
	 * to be in the project's classpath.
	 *
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#getClasspath(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	private String[] getProjectClasspath(ILaunchConfiguration configuration)
			throws CoreException {
		try {
			IRuntimeClasspathEntry[] entries = RunJettyRunClasspathUtil.getDefaultWebAppClasspaths(configuration);
			entries = RunJettyRunClasspathResolver.resolveClasspath(entries,
					configuration);

			// entries = JavaRuntime.resolveRuntimeClasspath(entries,
			// configuration);

			Set<String> locations = searchUserClass(entries);
			locations.addAll(getWebappCustomClasspath(configuration));
			locations.addAll(RunJettyRunClasspathUtil
					.getWebInfLibLocations(configuration));

			List<String> result = new ArrayList<String>();
			Map<String,String>  checked = getAttributeFromLaunchConfiguration(configuration, Plugin.ATTR_WEB_CONTEXT_CLASSPATH_NON_CHECKED);

			for(String item:locations){
				if(!checked.containsKey(item) || checked.get(item).equals("0")){
					result.add("-y-"+item);
				}else{
					result.add("-n-"+item);
				}
			}
			return (String[]) result.toArray(new String[0]);
		} catch (IllegalArgumentException e) {
			return new String[0];
		}
	}

	private Set<String> searchUserClass(IRuntimeClasspathEntry[] entries) {
		Set<String> locations = new LinkedHashSet<String>();
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry entry = entries[i];
			if (entry.getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
				String location = entry.getLocation();
				if (location != null) {
					locations.add(location);
				}
			}
		}
		return locations;
	}

}
