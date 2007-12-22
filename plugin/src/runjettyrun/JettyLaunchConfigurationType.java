package runjettyrun;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

/**
 * Launch configuration type for Jetty. Based on {@link JavaLaunchDelegate}.
 * 
 * @author hillenius
 */
public class JettyLaunchConfigurationType extends
		AbstractJavaLaunchConfigurationDelegate {

	public JettyLaunchConfigurationType() {
	}

	@SuppressWarnings("unchecked")
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask(MessageFormat.format(
				"{0}...", configuration.getName()), 3); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		try {
			monitor.subTask("verifying installation");

			String mainTypeName = Plugin.BOOTSTRAP_CLASS_NAME;
			IVMRunner runner = getVMRunner(configuration, mode);

			File workingDir = verifyWorkingDirectory(configuration);
			String workingDirName = null;
			if (workingDir != null) {
				workingDirName = workingDir.getAbsolutePath();
			}

			// Environment variables
			String[] envp = getEnvironment(configuration);

			// Program & VM arguments
			String pgmArgs = getProgramArguments(configuration);
			String vmArgs = getVMArguments(configuration);
			ExecutionArguments execArgs = new ExecutionArguments(vmArgs,
					pgmArgs);

			// VM-specific attributes
			Map vmAttributesMap = getVMSpecificAttributesMap(configuration);

			// Class path
			String[] classpath = getClasspath(configuration);

			// Create VM configuration
			VMRunnerConfiguration runConfig = new VMRunnerConfiguration(
					mainTypeName, classpath);
			runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
			runConfig.setEnvironment(envp);

			List<String> runtimeVmArgs = new ArrayList<String>();
			runtimeVmArgs.add("-Drjrcontext="
					+ configuration.getAttribute(Plugin.ATTR_CONTEXT, ""));
			runtimeVmArgs.add("-Drjrwebapp="
					+ configuration.getAttribute(Plugin.ATTR_WEBAPPDIR, ""));
			runtimeVmArgs.add("-Drjrport="
					+ configuration.getAttribute(Plugin.ATTR_PORT, ""));
			runtimeVmArgs.addAll(Arrays.asList(execArgs.getVMArgumentsArray()));

			runConfig.setVMArguments(runtimeVmArgs
					.toArray(new String[runtimeVmArgs.size()]));
			runConfig.setWorkingDirectory(workingDirName);
			runConfig.setVMSpecificAttributesMap(vmAttributesMap);

			// Boot path
			runConfig.setBootClassPath(getBootpath(configuration));

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}

			// stop in main
			prepareStopInMain(configuration);

			// done the verification phase
			monitor.worked(1);

			monitor.subTask("Creating source locator");
			// set the default source locator if required
			setDefaultSourceLocator(launch, configuration);
			monitor.worked(1);

			// Launch the configuration - 1 unit of work
			runner.run(runConfig, launch, monitor);

			// check for cancellation
			if (monitor.isCanceled()) {
				return;
			}
		} finally {
			monitor.done();
		}
	}
}
