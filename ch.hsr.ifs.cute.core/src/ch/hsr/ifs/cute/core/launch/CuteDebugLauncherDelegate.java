package ch.hsr.ifs.cute.core.launch;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.InferiorRuntimeProcess;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate2;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.progress.UIJob;

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.core.event.CuteConsoleEventParser;
import ch.hsr.ifs.testframework.event.ConsoleEventParser;
import ch.hsr.ifs.testframework.event.TestEventHandler;
import ch.hsr.ifs.testframework.launch.ConsolePatternListener;
import ch.hsr.ifs.testframework.launch.CustomisedLaunchConfigTab;
import ch.hsr.ifs.testframework.model.ModellBuilder;
import ch.hsr.ifs.testframework.ui.ConsoleLinkHandler;
import ch.hsr.ifs.testframework.ui.ShowResultView;

/**
 * Launch delegate implementation that redirects its queries to the preferred
 * launch delegate, correcting the arguments attribute (to take into account
 * auto generated test module parameters) and setting up the custom process
 * factory (to handle testing process IO streams).
 */
public class CuteDebugLauncherDelegate extends AbstractCLaunchDelegate2 {//LaunchConfigurationDelegate {

	/** Stores the changes made to the launch configuration. */
	private final Map<String, String> changesToLaunchConfiguration = new HashMap<String, String>();

	@Override
	public ILaunch getLaunch(ILaunchConfiguration config, String mode) throws CoreException {
		return getPreferredDelegate(config, mode).getLaunch(config, mode);
	}

	@Override
	public boolean buildForLaunch(ILaunchConfiguration config, String mode, IProgressMonitor monitor) throws CoreException {
		return getPreferredDelegate(config, mode).buildForLaunch(config, mode, monitor);
	}

	@Override
	public boolean finalLaunchCheck(ILaunchConfiguration config, String mode, IProgressMonitor monitor) throws CoreException {
		return getPreferredDelegate(config, mode).finalLaunchCheck(config, mode, monitor);
	}

	@Override
	public boolean preLaunchCheck(ILaunchConfiguration config, String mode, IProgressMonitor monitor) throws CoreException {
		return getPreferredDelegate(config, mode).preLaunchCheck(config, mode, monitor);
	}

	// TODO: needs to be adjusted for CUTE!!!!
	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (/*mode.equals(ILaunchManager.RUN_MODE) ||*/mode.equals(ILaunchManager.DEBUG_MODE)) {

			// NOTE: The modified working copy of launch configuration cannot be passed directly 
			// to the preferred delegate because in this case the LaunchHistory will not work
			// properly (and the rerun last launched configuration action will fail). So we
			// just modify the existing configuration and revert all the changes back after
			// the launch is done.

			try {
				// Changes launch configuration a bit and redirect it to the preferred C/C++ Application Launch delegate 
				updatedLaunchConfiguration(config);
				// First verify we are dealing with a proper project.
				ICProject project = verifyCProject(config);

				// Now verify we know the program to debug.
				IPath exePath = LaunchUtils.verifyProgramPath(config, project);
				exePath = sourcelookupPath(config, exePath); // strips exe file from path....
				ILaunchConfigurationDelegate2 delegate = getPreferredDelegate(config, mode);

				delegate.launch(config, mode, launch, monitor);

				registerCuteConsoleListeners(launch, exePath);

			} finally {
				revertChangedToLaunchConfiguration(config);
			}
			//			activateTestingView();
		}
	}

	public IPath sourcelookupPath(ILaunchConfiguration config, IPath exePath) {
		try {
			if (config != null && config.getAttribute(CustomisedLaunchConfigTab.USE_CUSTOM_SRC_PATH, false)) {
				String rootpath = org.eclipse.core.runtime.Platform.getLocation().toOSString();
				String customSrcPath = config.getAttribute(CustomisedLaunchConfigTab.CUSTOM_SRC_PATH, ""); //$NON-NLS-1$
				String fileSeparator = System.getProperty("file.separator"); //$NON-NLS-1$
				return new org.eclipse.core.runtime.Path(rootpath + customSrcPath + fileSeparator);
			} else {
				return exePath.removeLastSegments(1);
			}
		} catch (CoreException ce) {
			CuteCorePlugin.getDefault().getLog().log(ce.getStatus());
		}
		return exePath;//on error, log and make no changes
	}

	public void registerCuteConsoleListeners(ILaunch launch, IPath exePath) {
		IProcess[] procs = launch.getProcesses();
		for (IProcess proc : procs) {
			if (proc instanceof InferiorRuntimeProcess) {
				IConsole console = DebugUITools.getConsole(proc);
				if (console instanceof TextConsole) {
					UIJob job = new ShowResultView();
					job.schedule();
					try {
						job.join();
					} catch (InterruptedException e) {
					}
					TextConsole textCons = (TextConsole) console;

					registerPatternMatchListener(launch, exePath, textCons);
				}
			}
		}
	}

	protected void registerPatternMatchListener(ILaunch launch, IPath exePath, TextConsole textCons) {
		TestEventHandler handler = new ConsoleLinkHandler(exePath, textCons);
		ModellBuilder modelHandler = new ModellBuilder(exePath, launch);
		ConsolePatternListener listener = new ConsolePatternListener(getConsoleEventParser());
		listener.addHandler(handler);
		listener.addHandler(modelHandler);
		textCons.addPatternMatchListener(listener);
	}

	protected ConsoleEventParser getConsoleEventParser() {
		return new CuteConsoleEventParser();
	}

	/**
	 * Revert the changes to launch configuration previously made with
	 * <code>updatedLaunchConfigurationAttribute()</code>.
	 * 
	 * @param config
	 *            launch configuration to revert
	 */
	private void revertChangedToLaunchConfiguration(ILaunchConfiguration config) throws CoreException {
		ILaunchConfigurationWorkingCopy configWC = config.getWorkingCopy();
		for (Map.Entry<String, String> changeEntry : changesToLaunchConfiguration.entrySet()) {
			configWC.setAttribute(changeEntry.getKey(), changeEntry.getValue());
		}
		configWC.doSave();
		changesToLaunchConfiguration.clear();
	}

	/**
	 * Saves the current value of the specified attribute (to be reverted later)
	 * and update its value in launch configuration.
	 * 
	 * @param config
	 *            launch configuration which attribute should be updated
	 * @param attributeName
	 *            attribute name
	 * @param value
	 *            new value of the specified attribute
	 */
	private void updatedLaunchConfigurationAttribute(ILaunchConfigurationWorkingCopy config, String attributeName, String value) throws CoreException {
		changesToLaunchConfiguration.put(attributeName, config.getAttribute(attributeName, "")); //$NON-NLS-1$
		config.setAttribute(attributeName, value);
	}

	/**
	 * Makes the necessary changes to the launch configuration before passing it
	 * to the underlying delegate. Currently, updates the program arguments with
	 * the value that was obtained from Tests Runner provider plug-in.
	 * 
	 * @param config
	 *            launch configuration
	 */
	private void updatedLaunchConfiguration(ILaunchConfiguration config) throws CoreException {
		changesToLaunchConfiguration.clear();
		ILaunchConfigurationWorkingCopy configWC = config.getWorkingCopy();
		setProgramArguments(configWC);
		configWC.doSave();
	}

	/**
	 * Updates the program arguments with the value that was obtained from Tests
	 * Runner provider plug-in.
	 * 
	 * @param config
	 *            launch configuration
	 */
	private void setProgramArguments(ILaunchConfigurationWorkingCopy config) throws CoreException {
		// this is not needed for cute, because we set the run configuration from the action for runselected.

		//		List<?> packedTestsFilter = config.getAttribute(ITestsLaunchConfigurationConstants.ATTR_TESTS_FILTER, Collections.EMPTY_LIST);
		//		String[][] testsFilter = TestPathUtils.unpackTestPaths(packedTestsFilter.toArray(new String[packedTestsFilter.size()]));

		// Configure test module run parameters with a Tests Runner 
		//String[] params = null;

		//		try {
		//			params = getTestsRunner(config).getAdditionalLaunchParameters(testsFilter);
		//
		//		} catch (TestingException e) {
		//			throw new CoreException(new Status(IStatus.ERROR, TestsRunnerPlugin.getUniqueIdentifier(), e.getLocalizedMessage(), null));
		//		}

		// Rewrite ATTR_PROGRAM_ARGUMENTS attribute of launch configuration
		//		if (params != null && params.length >= 1) {
		//			StringBuilder sb = new StringBuilder();
		//			sb.append(config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "")); //$NON-NLS-1$
		//			for (String param : params) {
		//				sb.append(' ');
		//				sb.append(param);
		//			}
		//			updatedLaunchConfigurationAttribute(config, ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, sb.toString());
		//		}
	}

	/**
	 * Resolves the preferred launch delegate for the specified configuration to
	 * launch C/C++ Local Application in the specified mode. The preferred
	 * launch delegate ID is taken from <code>getPreferredDelegateId()</code>.
	 * 
	 * @param config
	 *            launch configuration
	 * @param mode
	 *            mode
	 * @return launch delegate
	 */
	@SuppressWarnings("unchecked")
	private ILaunchConfigurationDelegate2 getPreferredDelegate(ILaunchConfiguration config, String mode) throws CoreException {
		ILaunchManager launchMgr = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType localCfg = launchMgr.getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP);
		Set<String> modes = config.getModes();
		modes.add(mode);
		String preferredDelegateId = getPreferredDelegateId();
		for (ILaunchDelegate delegate : localCfg.getDelegates(modes)) {
			if (preferredDelegateId.equals(delegate.getId())) {
				return (ILaunchConfigurationDelegate2) delegate.getDelegate();
			}
		}
		return null;
	}

	/**
	 * Returns the launch delegate id which should be used to redirect the
	 * launch.
	 * 
	 * @return launch delegate ID
	 */
	public String getPreferredDelegateId() {
		return "org.eclipse.cdt.dsf.gdb.launch.localCLaunch"; //$NON-NLS-1$
	}

	@Override
	protected String getPluginID() {
		// TODO Auto-generated method stub
		return ch.hsr.ifs.cute.core.CuteCorePlugin.PLUGIN_ID;
	}

}
