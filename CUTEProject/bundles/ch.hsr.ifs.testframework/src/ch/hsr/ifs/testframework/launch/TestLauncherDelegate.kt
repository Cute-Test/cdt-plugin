/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.launch

import ch.hsr.ifs.testframework.TestFrameworkPlugin
import ch.hsr.ifs.testframework.event.ConsoleEventParser
import ch.hsr.ifs.testframework.model.ModellBuilder
import ch.hsr.ifs.testframework.ui.ConsoleLinkHandler
import ch.hsr.ifs.testframework.ui.ShowResultView
import org.eclipse.cdt.debug.core.CDebugUtils
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants
import org.eclipse.cdt.launch.LaunchUtils
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Platform
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2
import org.eclipse.debug.core.model.LaunchConfigurationDelegate
import org.eclipse.debug.ui.DebugUITools
import org.eclipse.ui.console.TextConsole
import java.util.concurrent.Executors
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess

/**
 * @since 3.0
 */
abstract class TestLauncherDelegate : LaunchConfigurationDelegate() {

	protected abstract fun getPreferredDelegateId(): String

	protected abstract fun getConsoleEventParser(): ConsoleEventParser

	override fun launch(config: ILaunchConfiguration, mode: String, launch: ILaunch, monitor: IProgressMonitor?) {
		try {
			if (mode == ILaunchManager.RUN_MODE || mode == ILaunchManager.DEBUG_MODE) {
				val project = CDebugUtils.verifyCProject(config)

				getPreferredDelegate(config, mode)?.launch(config, mode, launch, monitor) ?: return

				ShowResultView().run {
					schedule()
					join()
				}

				launch.processes.firstOrNull { it !is GDBProcess }?.let { process ->
					val console = DebugUITools.getConsole(process)
					if (console is TextConsole) {
						val programPath = CDebugUtils.verifyProgramPath(config)
						val sourcePath = sourcelookupPath(config, programPath)
						registerPatternMatchListener(launch, sourcePath, console)
					}
				}
			}
		} catch (e: CoreException) {
			TestFrameworkPlugin.log(e)
		}
	}

	override fun getLaunch(config: ILaunchConfiguration, mode: String) =
		getPreferredDelegate(config, mode)?.getLaunch(config, mode)

	override fun buildForLaunch(config: ILaunchConfiguration, mode: String, monitor: IProgressMonitor?) =
		getPreferredDelegate(config, mode)?.buildForLaunch(config, mode, monitor) ?: false

	override fun finalLaunchCheck(config: ILaunchConfiguration, mode: String, monitor: IProgressMonitor?) =
		getPreferredDelegate(config, mode)?.finalLaunchCheck(config, mode, monitor) ?: false

	override fun preLaunchCheck(config: ILaunchConfiguration, mode: String, monitor: IProgressMonitor?) =
		getPreferredDelegate(config, mode)?.preLaunchCheck(config, mode, monitor) ?: false

	private fun getPreferredDelegate(config: ILaunchConfiguration, mode: String): ILaunchConfigurationDelegate2? {
		val manager = DebugPlugin.getDefault().getLaunchManager()
		val modes = config.getModes() + mode
		val preferredId = getPreferredDelegateId()
		manager.getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP).let { cfg ->
			return cfg.getDelegates(modes).firstOrNull {
				preferredId == it.id
			}?.delegate as? ILaunchConfigurationDelegate2
		}
	}

	private fun registerPatternMatchListener(launch: ILaunch, exePath: IPath, textCons: TextConsole) {
		val handler = ConsoleLinkHandler(exePath, textCons)
		val modelHandler = ModellBuilder(exePath, launch)
		val listener = ConsolePatternListener(getConsoleEventParser())
		listener.addHandler(handler)
		listener.addHandler(modelHandler)
		textCons.addPatternMatchListener(listener)
	}

	private fun sourcelookupPath(config: ILaunchConfiguration?, exePath: IPath): IPath {
		try {
			if (config != null && config.getAttribute(USE_CUSTOM_SRC_PATH, false)) {
				val rootpath = Platform.getLocation().toOSString()
				val customSrcPath = config.getAttribute(CUSTOM_SRC_PATH, "")
				val fileSeparator = System.getProperty("file.separator")
				return Path(rootpath + customSrcPath + fileSeparator)
			} else {
				return exePath.removeLastSegments(1)
			}
		} catch (e: CoreException) {
			TestFrameworkPlugin.log(e.getStatus())
		}
		return exePath
	}

}
