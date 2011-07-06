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
import java.util.LinkedHashSet;
import java.util.List;
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

	private static HashMap<String,ILaunch> launcher = new HashMap<String,ILaunch>();


	/**
	 * Here's what the WebApp classpath. That means all the classpath here just like WEB-INF/classes or WEB-INF/lib ,
	 * which is only for the specific webapp project and will not used for the Jetty Instance.
	 *
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private String getWebappClasspath(ILaunchConfiguration configuration) throws CoreException{
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
		if(webAppClasspath.length() > 1024){
			File f = prepareClasspathFile(configuration,webAppClasspath);
			webAppClasspath = "file://"+f.getAbsolutePath();
		}

		return webAppClasspath;

	}

	/**
	 * Get working directory's absolute folder path.
	 *
	 * @param configuration
	 * @return return the path if exist, or return null.
	 * @throws CoreException
	 */
	private String getWorkingDirectoryAbsolutePath(ILaunchConfiguration configuration) throws CoreException{
		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null)
			workingDirName = workingDir.getAbsolutePath();

		return workingDirName;
	}

	/**
	 * I prefer to change the name to JettyClasspath to make it more clear.
	 * This classpath means how the RunJettyRun to get the Jetty bundle.
	 *
	 * If you change the classpath , that might means you are changing the Jetty version or something on it.
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private String[] getJettyClasspath(ILaunchConfiguration configuration) throws CoreException {
		return getClasspath(configuration);
	}


	/**
	 * get Runtime arguments , and prepare the webapp classpath for the program.
	 * @param configuration
	 * @param oringinalVMArguments
	 * @return
	 * @throws CoreException
	 */
	private String[] getRuntimeArguments(ILaunchConfiguration configuration,String[] oringinalVMArguments,String webappClasspath ) throws CoreException{
		List<String> runtimeVmArgs = getJettyArgs(configuration);

		boolean maxperm = false;
		for(String str:oringinalVMArguments){
			if(str != null && str.indexOf("-XX:MaxPermSize") != -1 )
				maxperm = true;
		}

		if(!maxperm){
			runtimeVmArgs.add("-XX:MaxPermSize=64m");
		}

		//Here the classpath is really for web app.
		runtimeVmArgs.add("-Drjrclasspath=" +  webappClasspath);

		runtimeVmArgs.add("-DrjrResourceMapping=" +  getLinkedResourceMapping(configuration));

		if(Plugin.getDefault().isListenerEnable()){
			runtimeVmArgs.add("-DrjrEclipseListener=" + Plugin.getDefault().getListenerPort());
		}

		runtimeVmArgs.addAll(Arrays.asList(oringinalVMArguments));

		return runtimeVmArgs.toArray(new String[runtimeVmArgs.size()]);
	}


	private String getLinkedResourceInResource(IContainer root,IContainer folder){
		StringBuffer sb = new StringBuffer();
		try {
			for(IResource ir:folder.members()){
				if(ir instanceof IFolder ){
					if(ir.isLinked()){
						sb.append(
							ir.getProjectRelativePath().
								makeRelativeTo(root.getProjectRelativePath())
								+"="+ir.getRawLocation()+";");
					}
					sb.append(getLinkedResourceInResource(root,(IFolder)ir));
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}
	private String getLinkedResourceMapping(ILaunchConfiguration conf){
		try {
			IJavaProject proj = getJavaProject(conf);
			String webappPath = conf.getAttribute(Plugin.ATTR_WEBAPPDIR, "");
			if( proj == null || "".equals(webappPath))
				return null;

			IContainer webappDir = null;
			if("/".equals(webappPath)) webappDir = proj.getProject();
			else webappDir = proj.getProject().getFolder(webappPath);

			if(webappDir.exists())
				return getLinkedResourceInResource(webappDir,webappDir);

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
		 * @see #terminateOldRJRLauncher
		 */
		if (!RunJettyRunLaunchConfigurationUtil.validation(configuration)) {
			throw new CoreException(new Status(IStatus.ERROR,Plugin.PLUGIN_ID,
					01, " Invalid run configuration , please check the configuration ", null));
		}


		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(
				MessageFormat.format("{0}...", configuration.getName()), 3); //$NON-NLS-1$

		// check for cancellation
		if (monitor.isCanceled()) return;

		try {
			monitor.subTask("verifying installation");

			// Program & VM arguments
			ExecutionArguments execArgs = new ExecutionArguments(getVMArguments(configuration),
					getProgramArguments(configuration));

			// Create VM configuration
			//here the classpath means for the Jetty Server , not for the application! by TonyQ 2011/3/7
			VMRunnerConfiguration runConfig = new VMRunnerConfiguration(
					Plugin.BOOTSTRAP_CLASS_NAME , getJettyClasspath(configuration));
			runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());

			// Environment variables
			runConfig.setEnvironment(getEnvironment(configuration));

			//Here prepare the classpath is really for webapp in Runtime Arguments , too.
			runConfig.setVMArguments(getRuntimeArguments(configuration,execArgs.getVMArgumentsArray(),
					getWebappClasspath(configuration)));

			runConfig.setWorkingDirectory(getWorkingDirectoryAbsolutePath(configuration));
			runConfig.setVMSpecificAttributesMap(getVMSpecificAttributesMap(configuration));

			// Boot path
			runConfig.setBootClassPath(getBootpath(configuration));

			// check for cancellation
			if (monitor.isCanceled()) return;

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
				getVMRunner(configuration, mode).run(runConfig, launch, monitor);
				registerRJRLauncher(configuration, launch);
			}

			// check for cancellation
			if (monitor.isCanceled()) return;

		} finally {
			monitor.done();
		}

	}

	/**
	 * A private helper to prepare a classpath file to workspace metadata,
	 * we use this to prevent classpath too long which was caused the problem
	 * for reaching Windows command length limitation.
	 *
	 * @param configuration
	 * @param classpath
	 * @return
	 */
	private File prepareClasspathFile(ILaunchConfiguration configuration,String classpath){
		IPath path = Plugin.getDefault().getStateLocation().append(configuration.getName()+".classpath");
		File f = path.toFile();
		try{
		    BufferedWriter out = new BufferedWriter(new OutputStreamWriter
                    (new FileOutputStream(f,false),"UTF8"));
			out.write(classpath);
			out.close();
			return f;
		}catch(IOException e){
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
	private static void  terminateOldRJRLauncher(ILaunchConfiguration configuration, ILaunch launch) throws CoreException{
		String port = configuration.getAttribute(Plugin.ATTR_PORT,"");
		String sslPort = configuration.getAttribute(Plugin.ATTR_SSL_PORT,"");
		boolean enableSSL = configuration.getAttribute(Plugin.ATTR_ENABLE_SSL,false);

		if(!"".equals(port) && launcher.containsKey(port)){
			terminateLaunch(launcher.get(port));
			launcher.remove(port);
		}
		if(enableSSL && !"".equals(sslPort) && launcher.containsKey(sslPort)){
			terminateLaunch(launcher.get(sslPort));
			launcher.remove(sslPort);
		}
	}

	private static void terminateLaunch(ILaunch launch) throws DebugException{
		if(!launch.isTerminated()){
			IProcess[] processes = launch.getProcesses();
			if(processes != null){
				for(IProcess proc : processes){
					if(proc != null ) proc.terminate();
				}
			}
		}
	}
	/**
	 * register a port for RJR Launcher
	 * @param configuration
	 * @param launch
	 * @throws CoreException
	 */
	private static void registerRJRLauncher(ILaunchConfiguration configuration, ILaunch launch) throws CoreException{
		String port = configuration.getAttribute(Plugin.ATTR_PORT,"");
		String sslPort = configuration.getAttribute(Plugin.ATTR_SSL_PORT,"");
		boolean enableSSL = configuration.getAttribute(Plugin.ATTR_ENABLE_SSL,false);

		if(!"".equals(port) ) launcher.put(port,launch);
		if(enableSSL && !"".equals(sslPort))
			launcher.put(sslPort,launch);
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
				Plugin.ATTR_SCANINTERVALSECONDS, "scanintervalseconds");

		addOptionalAttr(configuration, runtimeVmArgs,
				Plugin.ATTR_EXCLUDE_CLASSPATH, "excludedclasspath");

		addOptionalAttrx(configuration, runtimeVmArgs,
				Plugin.ATTR_ENABLE_SCANNER, "enablescanner");

		addOptionalAttrx(configuration, runtimeVmArgs,
				Plugin.ATTR_SCANNER_SCAN_WEBINF, "scanWEBINF");

		addOptionalAttrx(configuration, runtimeVmArgs,
				Plugin.ATTR_ENABLE_SSL, "enablessl");

		addOptionalAttrx(configuration, runtimeVmArgs,
				Plugin.ATTR_ENABLE_NEED_CLIENT_AUTH, "needclientauth");

		addOptionalAttrx(configuration, runtimeVmArgs,
				Plugin.ATTR_ENABLE_PARENT_LOADER_PRIORITY, "parentloaderpriority");

		addOptionalAttrx(configuration, runtimeVmArgs,
				Plugin.ATTR_ENABLE_JNDI, "enbaleJNDI");

		return runtimeVmArgs;
	}


	private void addWebappAttr(ILaunchConfiguration configuration,
			List<String> runtimeVmArgs, String cfgAttr)
		throws CoreException {
		IJavaProject proj =this.getJavaProject(configuration);
		if(proj == null)
			return;

		String value = configuration.getAttribute(cfgAttr, "");

		if("/".equals(value)){
			value = proj.getResource().getRawLocation().toOSString();
		}else{
			value = proj.getProject().getFolder(value).getRawLocation().toOSString();
		}


		if (value.length() == 0)
			return;

		String arg = "-Drjr" + "webapp" + "=" + value+"";
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

		IJavaProject proj = JavaRuntime.getJavaProject(configuration);
		if (proj == null) {
			Plugin.logError("No project!");
			return new String[0];
		}

		IRuntimeClasspathEntry[] entries =
			RunJettyRunClasspathUtil.filterWebInfLibs(JavaRuntime.computeUnresolvedRuntimeClasspath(proj),configuration);


		// Remove JRE entry/entries.

		IRuntimeClasspathEntry stdJreEntry = JavaRuntime
				.computeJREEntry(configuration);
		IRuntimeClasspathEntry projJreEntry = JavaRuntime.computeJREEntry(proj);
		List<IRuntimeClasspathEntry> entryList = new ArrayList<IRuntimeClasspathEntry>(
				entries.length);

		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry entry = entries[i];
			if (entry.equals(stdJreEntry))
				continue;
			if (entry.equals(projJreEntry))
				continue;
			entryList.add(entry);
		}

		// Resolve the entries to actual file/folder locations.

		entries = entryList.toArray(new IRuntimeClasspathEntry[0]);

		entries = RunJettyRunClasspathResolver.resolveClasspath(entries, configuration);

		// entries = JavaRuntime.resolveRuntimeClasspath(entries,
		// configuration);

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

		locations.addAll( RunJettyRunClasspathUtil.getWebInfLibLocations(configuration) );

		return (String[]) locations.toArray(new String[locations.size()]);
	}

}
