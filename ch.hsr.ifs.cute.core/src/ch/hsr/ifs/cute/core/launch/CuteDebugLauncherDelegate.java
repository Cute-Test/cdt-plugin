package ch.hsr.ifs.cute.core.launch;

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

	@Override
	public void launch(ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {

		if (ILaunchManager.DEBUG_MODE.equals(mode)) {

			try {
				// First verify we are dealing with a proper project.
				ICProject project = verifyCProject(config);

				// Now verify we know the program to debug.
				IPath exePath = LaunchUtils.verifyProgramPath(config, project);
				exePath = sourcelookupPath(config, exePath); // strips exe file from path....

				ILaunchConfigurationDelegate2 delegate = getPreferredDelegate(config, mode);

				delegate.launch(config, mode, launch, monitor);

				registerCuteConsoleListeners(launch, exePath);

			} finally {
			}
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

	// we only support DSF.gdb for now.
	public String getPreferredDelegateId() {
		return "org.eclipse.cdt.dsf.gdb.launch.localCLaunch"; //$NON-NLS-1$
	}

	@Override
	protected String getPluginID() {
		return ch.hsr.ifs.cute.core.CuteCorePlugin.PLUGIN_ID;
	}

}
