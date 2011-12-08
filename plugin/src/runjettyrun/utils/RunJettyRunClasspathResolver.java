package runjettyrun.utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.DefaultProjectClasspathEntry;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry2;
import org.eclipse.jdt.launching.JavaRuntime;

import runjettyrun.Plugin;

/**
// * fix for issue #52 , RJR compatiblity with M2E proejct.
 * @author TonyQ
 *
 */
@SuppressWarnings("restriction")
public class RunJettyRunClasspathResolver {
	/**
	 * reference to M2E project , 20101115 version.
	 */
	private static String MAVEN_CONTAINER_ID = "org.maven.ide.eclipse.MAVEN2_CLASSPATH_CONTAINER";
	private static String WEBAPP_CONTAINER_PATH = "org.eclipse.jst.j2ee.internal.web.container";

	//path:org.eclipse.jst.server.core.container/org.eclipse.jst.server.jetty.runtimeTarget/Jetty v7.2
	private static String SERVER_RUNTIME_PATH_PREFIX = "org.eclipse.jst.server.core.container";

	public static IRuntimeClasspathEntry[] resolveClasspath(IRuntimeClasspathEntry[] entries,ILaunchConfiguration configuration ) throws CoreException {

		IJavaProject proj = JavaRuntime.getJavaProject(configuration);

		if (proj == null) {
			Plugin.logError("No project!");
			return entries;
		}

		return resolvedProjectClasspath(entries,configuration,ProjectUtil.isMavenProject(proj.getProject()));
	}

//	private static IRuntimeClasspathEntry[] resolvedProjectClasspathSelf(){
//		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(cpe.getPath().segment(0));
//		IJavaProject jp = JavaCore.create(p);
//
//		return JavaRuntime.resolveRuntimeClasspathEntry(new RuntimeClasspathEntry(cpe), jp);
//	}

	private static IRuntimeClasspathEntry[] resolvedProjectClasspath(
			IRuntimeClasspathEntry[] entries,ILaunchConfiguration configuration,boolean isMaven)throws CoreException{
		Set<IRuntimeClasspathEntry> all = new LinkedHashSet<IRuntimeClasspathEntry>(entries.length);
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry[] resolved = resolveRuntimeClasspathEntry(entries[i], configuration, isMaven);
			for (int j = 0; j < resolved.length; j++) {
				all.add(resolved[j]);
			}
		}
		return (IRuntimeClasspathEntry[])all.toArray(new IRuntimeClasspathEntry[all.size()]);

	}
	/**
	 * @see JavaRuntime#resolveRuntimeClasspathEntry
	 * @param entries
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private static IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(
			IRuntimeClasspathEntry entry,ILaunchConfiguration configuration,boolean isMaven)throws CoreException{

		if(entry instanceof DefaultProjectClasspathEntry){
			IRuntimeClasspathEntry2 entry2 = (IRuntimeClasspathEntry2)entry;
			IRuntimeClasspathEntry[] entries = entry2.getRuntimeClasspathEntries(configuration);
			List<IRuntimeClasspathEntry> resolved = new ArrayList<IRuntimeClasspathEntry>();
			for (int i = 0; i < entries.length; i++) {
				IRuntimeClasspathEntry[] temp = null;
				/**
				 * we need to handle a special case for MAVEN_CONTAINER with workspace project.
				 */
				IRuntimeClasspathEntry entryCur = entries[i];

				//We skip server runtime directly since we didn't need server runtime ,
				//and it will conflict with our Jetty bundle.
				if(isServerRuntimeContainer(entryCur)){
					continue;
				}

				if(isMaven && isProjectIncludedByM2E(entryCur)){
					temp = computeMavenContainerEntries(entryCur,configuration);
				}else {
					temp = JavaRuntime.resolveRuntimeClasspathEntry(entryCur, configuration);
				}


				boolean skipTestClasses = configuration.getAttribute(Plugin.ATTR_ENABLE_MAVEN_TEST_CLASSES,true);
				for (int j = 0; j < temp.length; j++) {
					if(skipTestClasses && temp[j].getLocation() != null && temp[j].getLocation().endsWith("test-classes")){
						continue;
					}
					resolved.add(temp[j]);
				}
			}

			return (IRuntimeClasspathEntry[]) resolved.toArray(new IRuntimeClasspathEntry[resolved.size()]);
		}else{
			return JavaRuntime.resolveRuntimeClasspathEntry(entry, configuration);

		}
	}

	private static boolean isServerRuntimeContainer(IRuntimeClasspathEntry entryCur){
		String entryPath = entryCur.getPath() == null ? "" : entryCur.getPath().toString();
		return (entryPath.indexOf(SERVER_RUNTIME_PATH_PREFIX)!=-1);

	}

	/**
	 * Sometimes for M2E , if you set package type as war , it will load some dependency to WEB App Container,
	 * as
	 * @param entryCur
	 * @return
	 */
	private static boolean isProjectIncludedByM2E(IRuntimeClasspathEntry entryCur){
		String entryPath = entryCur.getPath() == null ? "" : entryCur.getPath().toString();
		return entryCur.getType()== IRuntimeClasspathEntry.CONTAINER &&
			( MAVEN_CONTAINER_ID.equals(entryCur.getVariableName()) || WEBAPP_CONTAINER_PATH.equals(entryPath));
	}
	/**
	 * Performs default resolution for a container entry.
	 * Delegates to the Java model.
	 */
	private static IRuntimeClasspathEntry[] computeMavenContainerEntries(IRuntimeClasspathEntry entry, ILaunchConfiguration config) throws CoreException {
		IJavaProject project = entry.getJavaProject();
		if (project == null) {
			project = JavaRuntime.getJavaProject(config);
		}
		if (project == null || entry == null) {
			// cannot resolve without entry or project context
			return new IRuntimeClasspathEntry[0];
		}
		IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
		if (container == null) {
			return null;
		}
		IClasspathEntry[] cpes = container.getClasspathEntries();
		int property = -1;
		switch (container.getKind()) {
			case IClasspathContainer.K_APPLICATION:
				property = IRuntimeClasspathEntry.USER_CLASSES;
				break;
			case IClasspathContainer.K_DEFAULT_SYSTEM:
				property = IRuntimeClasspathEntry.STANDARD_CLASSES;
				break;
			case IClasspathContainer.K_SYSTEM:
				property = IRuntimeClasspathEntry.BOOTSTRAP_CLASSES;
				break;
		}
		List<IRuntimeClasspathEntry> resolved = new ArrayList<IRuntimeClasspathEntry>(cpes.length);

			for (int i = 0; i < cpes.length; i++) {
				IClasspathEntry cpe = cpes[i];
				if (cpe.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
					/**
					 * the core patch for project included by M2E , we only load the output location ,
					 * instead of solving all the dependency for it.
					 */
					IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(cpe.getPath().segment(0));
					IJavaProject jp = JavaCore.create(p);

					IRuntimeClasspathEntry[] entries = JavaRuntime.resolveRuntimeClasspathEntry(new RuntimeClasspathEntry(cpe), jp);
					for (int j = 0; j < entries.length; j++) {
						IRuntimeClasspathEntry e =  entries[j];

						//skip test-classes for included maven project.
						boolean testClasses =  e.getLocation()!=null && e.getLocation().endsWith("test-classes");

						if (!(resolved.contains(e) || testClasses))
							resolved.add(e);

					}
					/**
					 * end
					 */
				} else {
					IRuntimeClasspathEntry e = new RuntimeClasspathEntry(cpe);
					if (!resolved.contains(e)) {
						resolved.add(e);
					}
				}
			}

		// set classpath property
		IRuntimeClasspathEntry[] result = new IRuntimeClasspathEntry[resolved.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = (IRuntimeClasspathEntry) resolved.get(i);
			result[i].setClasspathProperty(property);
		}
		return result;
	}

}
