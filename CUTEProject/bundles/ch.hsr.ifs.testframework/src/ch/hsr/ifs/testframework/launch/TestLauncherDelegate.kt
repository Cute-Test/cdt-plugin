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
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Platform
import org.eclipse.debug.core.DebugEvent
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate2
import org.eclipse.debug.core.model.IProcess
import org.eclipse.debug.core.model.LaunchConfigurationDelegate
import org.eclipse.debug.internal.ui.views.console.ProcessConsole
import org.eclipse.ui.console.ConsolePlugin
import org.eclipse.ui.console.IConsole
import org.eclipse.ui.console.IConsoleListener
import org.eclipse.ui.console.TextConsole

/**
 * @since 3.0
 */
abstract class TestLauncherDelegate : LaunchConfigurationDelegate() {

	private inner class ProcessConsoleListener(
		private val fLaunch: ILaunch,
		private val fSourcePath: IPath
	) : IConsoleListener {

		override fun consolesAdded(consoles: Array<out IConsole>) {
			consoles.filter { it is ProcessConsole }
				.map { it as ProcessConsole }
				.filter { it.process !is GDBProcess }
				.filter { fLaunch.processes.contains(it.process) }
				.forEach {
					ShowResultView().apply { schedule() }
					registerPatternMatchListener(fLaunch, fSourcePath, it)
				}
		}

		override fun consolesRemoved(consoles: Array<out IConsole>?) = Unit

	}

	private val fLaunchObservers: List<ILaunchObserver> by lazy {
		val registry = Platform.getExtensionRegistry()
		registry.getExtensionPoint(TestFrameworkPlugin.PLUGIN_ID, "launchObserver")?.run {
			try {
				extensions.map {
					it.configurationElements[0].createExecutableExtension("class") as? ILaunchObserver
				}.filterNotNull().toList()
			} catch (e: Throwable) {
				TestFrameworkPlugin.log(e)
				emptyList<ILaunchObserver>()
			}
		} ?: emptyList<ILaunchObserver>()
	}

	protected abstract fun getPreferredDelegateId(): String

	protected abstract fun getConsoleEventParser(): ConsoleEventParser

	override fun launch(config: ILaunchConfiguration, mode: String, launch: ILaunch, monitor: IProgressMonitor?) {
		try {
			if (mode == ILaunchManager.RUN_MODE || mode == ILaunchManager.DEBUG_MODE) {
				val project = CDebugUtils.verifyCProject(config)
				val programPath = CDebugUtils.verifyProgramPath(config)
				val sourcePath = sourcelookupPath(config, programPath)
				val consoleListener = ProcessConsoleListener(launch, sourcePath)

				ConsolePlugin.getDefault().consoleManager.addConsoleListener(consoleListener)
				DebugPlugin.getDefault().addDebugEventListener { events ->
					events.filter { it.kind == DebugEvent.TERMINATE }
						.filter { launch.processes.contains(it.source) }
						.filter { it.source !is GDBProcess }
						.forEach {
							fLaunchObservers.forEach { it.notifyTermination(project.project) }
							ConsolePlugin.getDefault().consoleManager.removeConsoleListener(consoleListener)
						}
				}

				fLaunchObservers.forEach { it.notifyBeforeLaunch(project.project) }
				getPreferredDelegate(config, mode)?.launch(config, mode, launch, monitor) ?: return
				fLaunchObservers.forEach { it.notifyAfterLaunch(project.project) }
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
