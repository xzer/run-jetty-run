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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;

import runjettyrun.container.RunJettyRunContainerClasspathEntry;
import runjettyrun.extensions.IJettyPackageProvider;
import runjettyrun.utils.RunJettyRunClasspathResolver;
import runjettyrun.utils.RunJettyRunClasspathUtil;
import runjettyrun.utils.RunJettyRunLaunchConfigurationUtil;

public class JettyLaunchConfigurationClassPathProvider extends
		StandardClasspathProvider implements IRuntimeClasspathEntryResolver{

	public JettyLaunchConfigurationClassPathProvider() {
	}
	private Set<String> getScanLocations(ILaunchConfiguration configuration,IJavaProject proj) throws CoreException{

		IRuntimeClasspathEntry[] entries = RunJettyRunClasspathUtil	.filterWebInfLibs(
						JavaRuntime.computeUnresolvedRuntimeClasspath(proj),configuration);

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

		IRuntimeClasspathEntry[] customEntries =
			computeUnresolvedCustomClasspath(configuration, Plugin.ATTR_WEB_CONTEXT_CUSTOM_CLASSPATH);

		for(IRuntimeClasspathEntry entry:customEntries){
			entryList.add(entry);
		}
		// Resolve the entries to actual file/folder locations.

		entries = entryList.toArray(new IRuntimeClasspathEntry[0]);

		entries = RunJettyRunClasspathResolver.resolveClasspath(entries,
				configuration);

		// entries = JavaRuntime.resolveRuntimeClasspath(entries,
		// configuration);

		Set<String> locations = new LinkedHashSet<String>();
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry entry = entries[i];
			if (entry.getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
				String location = entry.getLocation();
				if (location != null) {
					File f =new File(location);
					if(f.exists() && f.isDirectory()){
						locations.add(location);
					}
				}
			}
		}

		return locations;
	}

	public Set<String> getAllScanPathList(ILaunchConfiguration configuration)
	throws CoreException {
		IJavaProject proj = JavaRuntime.getJavaProject(configuration);
		if (proj == null) {
			Plugin.logError("No project!");
			return new HashSet<String>();
		}

		Set<String> locations = getScanLocations(configuration,proj);
		locations.addAll(getCustomScanPathList(configuration));

		return locations;
	}
	public Set<String> getCustomScanPathList(ILaunchConfiguration configuration)
	throws CoreException {
		IJavaProject proj = JavaRuntime.getJavaProject(configuration);
		if (proj == null) {
			Plugin.logError("No project!");
			return new HashSet<String>();
		}

		IRuntimeClasspathEntry[] customEntries = computeUnresolvedCustomClasspath(configuration, Plugin.ATTR_CUSTOM_SCAN_FOLDER);
		Set<String> locations = new HashSet<String>();
		for(IRuntimeClasspathEntry entry:customEntries){
			locations.add(entry.getLocation());
		}
		return locations;
	}


	public List<IRuntimeClasspathEntry> getDefaultScanList(ILaunchConfiguration configuration)
			throws CoreException {

		IJavaProject proj = JavaRuntime.getJavaProject(configuration);
		if (proj == null) {
			Plugin.logError("No project!");
			return new ArrayList<IRuntimeClasspathEntry>();
		}

		Set<String> locations = getScanLocations(configuration,proj);

		List<IRuntimeClasspathEntry> scanlist = new ArrayList<IRuntimeClasspathEntry>();
		for(String location:locations){
			scanlist.add(JavaRuntime.newArchiveRuntimeClasspathEntry(new Path(location)));
		}

		return scanlist;
	}

	public IRuntimeClasspathEntry[] computeWebcontextClassPath(
			ILaunchConfiguration configuration) throws CoreException {

		IJavaProject proj = JavaRuntime.getJavaProject(configuration);
		if (proj == null) {
			Plugin.logError("No project!");
			return new IRuntimeClasspathEntry[0];
		}

		List<IRuntimeClasspathEntry> entryList =  RunJettyRunClasspathUtil.getProjectClasspathsForUserlibs(proj, false);
		return (IRuntimeClasspathEntry[]) entryList.toArray(new IRuntimeClasspathEntry[0]);
	}

	public IRuntimeClasspathEntry[] computeUnresolvedCustomClasspath(
			ILaunchConfiguration configuration, String attribute)
			throws CoreException {
		IRuntimeClasspathEntry[] classpath = new IRuntimeClasspathEntry[0];
		// recover persisted classpath
		classpath = recoverRuntimePath(configuration, attribute);

		return classpath;
	}

	public IRuntimeClasspathEntry[] computeUnresolvedJettyClasspath(
			ILaunchConfiguration configuration) throws CoreException {
		boolean useDefault = configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
		IRuntimeClasspathEntry[] classpath = new IRuntimeClasspathEntry[0];
		if (useDefault) {
			// classpath = RunJettyRunClasspathUtil.filterWebInfLibs(classpath,
			// configuration);
			classpath = addJetty(classpath, configuration);

		} else {
			// recover persisted classpath
			classpath = recoverRuntimePath(configuration,
					IJavaLaunchConfigurationConstants.ATTR_CLASSPATH);
		}
		try {
			if (configuration.getAttribute(Plugin.ATTR_ENABLE_JNDI, false)) {
				List<IRuntimeClasspathEntry> entries = new ArrayList<IRuntimeClasspathEntry>();
				entries.addAll(Arrays.asList(classpath));
				entries.add(new RunJettyRunContainerClasspathEntry(
						Plugin.CONTAINER_RJR_JETTY_JNDI,
						IRuntimeClasspathEntry.USER_CLASSES));
				return entries.toArray(new IRuntimeClasspathEntry[0]);
			}
		} catch (CoreException e) {
		}

		return classpath;
	}

	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(
			ILaunchConfiguration configuration) throws CoreException {
		IRuntimeClasspathEntry[] classpath = super
				.computeUnresolvedClasspath(configuration);

		boolean useDefault = configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
		if (useDefault) {
			classpath = addJetty(classpath, configuration);

		} else {
			// recover persisted classpath
			classpath = recoverRuntimePath(configuration,
					IJavaLaunchConfigurationConstants.ATTR_CLASSPATH);
		}
		try {
			if (configuration.getAttribute(Plugin.ATTR_ENABLE_JNDI, false)) {
				List<IRuntimeClasspathEntry> entries = new ArrayList<IRuntimeClasspathEntry>();
				entries.addAll(Arrays.asList(classpath));
				entries.add(new RunJettyRunContainerClasspathEntry(
						Plugin.CONTAINER_RJR_JETTY_JNDI,
						IRuntimeClasspathEntry.USER_CLASSES));
				return entries.toArray(new IRuntimeClasspathEntry[0]);
			}
		} catch (CoreException e) {
		}

		return classpath;
	}

	public IRuntimeClasspathEntry[] resolveChildClasspath(
			IRuntimeClasspathEntry entry, ILaunchConfiguration configuration)
			throws CoreException {
		return resolveClasspath(new IRuntimeClasspathEntry[] { entry },
				configuration, false);

	}

	/*
	 * James Synge: overriding so that I can block the inclusion of external
	 * libraries that should be found in WEB-INF/lib, and shouldn't be on the
	 * JVM's class path.
	 *
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.launching.StandardClasspathProvider#resolveClasspath(org
	 * .eclipse.jdt.launching.IRuntimeClasspathEntry[],
	 * org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] resolveClasspath(
			IRuntimeClasspathEntry[] entries, ILaunchConfiguration configuration)
			throws CoreException {
		return resolveClasspath(entries, configuration, false);
	}

	public IRuntimeClasspathEntry[] resolveClasspath(
			IRuntimeClasspathEntry[] entries,
			ILaunchConfiguration configuration, boolean ignoreProject)
			throws CoreException {

		Set<IRuntimeClasspathEntry> all = new LinkedHashSet<IRuntimeClasspathEntry>(
				entries.length);
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry entry = entries[i];
			IResource resource = entry.getResource();
			if (ignoreProject && resource instanceof IProject) {
				continue;
			}

			if (Plugin.CONTAINER_RJR_JETTY.equals(entry.getVariableName())) {
				all.addAll(Arrays.asList(RunJettyRunLaunchConfigurationUtil
						.loadPackage(configuration,
								IJettyPackageProvider.TYPE_JETTY_BUNDLE)));
			} else if (Plugin.CONTAINER_RJR_JETTY_JNDI.equals(entry
					.getVariableName())) {
				all.addAll(Arrays.asList(RunJettyRunLaunchConfigurationUtil
						.loadPackage(configuration,
								IJettyPackageProvider.TYPE_UTIL)));
			} else {
				// resloved by default
				// here's as same as StandardClasspathProvider
				all.addAll(Arrays.asList(JavaRuntime
						.resolveRuntimeClasspathEntry(entry, configuration)));
			}

		}

		return all.toArray(new IRuntimeClasspathEntry[0]);
	}

	/* private helper */
	private IRuntimeClasspathEntry[] addJetty(
			IRuntimeClasspathEntry[] existing, ILaunchConfiguration config) {

		List<IRuntimeClasspathEntry> entries = new ArrayList<IRuntimeClasspathEntry>();
		entries.addAll(Arrays.asList(existing));
		entries.add(new RunJettyRunContainerClasspathEntry(
				Plugin.CONTAINER_RJR_JETTY, IRuntimeClasspathEntry.USER_CLASSES));

		return entries.toArray(new IRuntimeClasspathEntry[0]);

	}

	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(
			IRuntimeClasspathEntry entry, ILaunchConfiguration configuration)
			throws CoreException {
		return resolveClasspath(new IRuntimeClasspathEntry[]{entry},configuration );
	}

	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(
			IRuntimeClasspathEntry entry, IJavaProject project)
			throws CoreException {
		throw new UnsupportedOperationException();
	}

	public IVMInstall resolveVMInstall(IClasspathEntry entry)
			throws CoreException {
		throw new UnsupportedOperationException();
	}

}