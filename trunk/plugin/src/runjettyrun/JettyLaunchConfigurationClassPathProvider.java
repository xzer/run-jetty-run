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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.StandardClasspathProvider;

import runjettyrun.container.RunJettyRunContainerClasspathEntry;
import runjettyrun.utils.RunJettyRunClasspathUtil;

public class JettyLaunchConfigurationClassPathProvider extends
    StandardClasspathProvider {

  public JettyLaunchConfigurationClassPathProvider() {
  }

  public IRuntimeClasspathEntry[] computeUnresolvedClasspath(
      ILaunchConfiguration configuration) throws CoreException {
    IRuntimeClasspathEntry[] classpath = super
        .computeUnresolvedClasspath(configuration);
    boolean useDefault = configuration.getAttribute(
        IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, true);
    if (useDefault) {
      classpath = RunJettyRunClasspathUtil.filterWebInfLibs(classpath, configuration);
      classpath = addJetty(classpath, configuration);

    } else {
      // recover persisted classpath
      classpath =  recoverRuntimePath(configuration,
          IJavaLaunchConfigurationConstants.ATTR_CLASSPATH);
    }
    try {
		if(configuration.getAttribute(Plugin.ATTR_ENABLE_JNDI,false)){
		    List<IRuntimeClasspathEntry> entries = new ArrayList<IRuntimeClasspathEntry>();
		    entries.addAll(Arrays.asList(classpath));
			entries.add(new RunJettyRunContainerClasspathEntry(Plugin.CONTAINER_RJR_JETTY_JNDI,IRuntimeClasspathEntry.USER_CLASSES));
			return entries.toArray(new IRuntimeClasspathEntry[0]);
		}
	} catch (CoreException e) {
	}

    return classpath;
  }

  private IRuntimeClasspathEntry[] addJetty(
      IRuntimeClasspathEntry[] existing, ILaunchConfiguration config) {


    List<IRuntimeClasspathEntry> entries = new ArrayList<IRuntimeClasspathEntry>();
    entries.addAll(Arrays.asList(existing));
    entries.add(new RunJettyRunContainerClasspathEntry(Plugin.CONTAINER_RJR_JETTY,IRuntimeClasspathEntry.USER_CLASSES));

    return entries.toArray(new IRuntimeClasspathEntry[0]);

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

    Set<IRuntimeClasspathEntry> all = new LinkedHashSet<IRuntimeClasspathEntry>(
        entries.length);
    for (int i = 0; i < entries.length; i++) {
      IRuntimeClasspathEntry entry = entries[i];
      IResource resource = entry.getResource();
      if (resource instanceof IProject) {
        continue;
      }

      IRuntimeClasspathEntry[] resolved = JavaRuntime
          .resolveRuntimeClasspathEntry(entry, configuration);
      all.addAll(Arrays.asList(resolved));

    }

    IRuntimeClasspathEntry[] resolvedClasspath = all
        .toArray(new IRuntimeClasspathEntry[0]);

    return resolvedClasspath;
  }
}
