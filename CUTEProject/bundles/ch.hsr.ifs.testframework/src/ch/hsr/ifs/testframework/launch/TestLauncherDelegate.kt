/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.launch

import java.io.IOException
import java.io.File
import java.util.concurrent.Executors

import org.eclipse.cdt.debug.core.CDebugUtils
import org.eclipse.cdt.launch.AbstractCLaunchDelegate
import org.eclipse.cdt.launch.internal.ui.LaunchMessages
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin
import org.eclipse.cdt.utils.pty.PTY
import org.eclipse.cdt.utils.spawner.ProcessFactory
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.Status
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.debug.ui.DebugUITools
import org.eclipse.ui.console.TextConsole

import ch.hsr.ifs.testframework.TestFrameworkPlugin
import ch.hsr.ifs.testframework.event.ConsoleEventParser
import ch.hsr.ifs.testframework.model.ModellBuilder
import ch.hsr.ifs.testframework.ui.ConsoleLinkHandler
import ch.hsr.ifs.testframework.ui.ShowResultView


/**
 * @since 3.0
 */
@SuppressWarnings("restriction")
abstract class TestLauncherDelegate : AbstractCLaunchDelegate() {

   protected val terminationRunner = Executors.newSingleThreadExecutor()

   protected abstract fun getConsoleEventParser(): ConsoleEventParser

   override protected abstract fun getPluginID(): String

   override fun launch(configuration: ILaunchConfiguration, mode: String, launch: ILaunch, monitor: IProgressMonitor?) =
         runLocalApplication(configuration, launch, monitor ?: NullProgressMonitor())

   private fun runLocalApplication(config: ILaunchConfiguration , launch: ILaunch, monitor: IProgressMonitor) {
      monitor.beginTask(LaunchMessages.LocalCDILaunchDelegate_0, 10)
      if (monitor.isCanceled) {
         return
      }

      monitor.worked(1)

      try {
         var exePath = CDebugUtils.verifyProgramPath(config)
         val cProject = CDebugUtils.verifyCProject(config)
         val project = cProject.project

         notifyBeforeLaunch(project)
         val wd = getWorkingDirectory(config) ?: File(System.getProperty("user.home", "."))

         val arguments = getProgramArgumentsArray(config)
         val command = listOf(exePath.toOSString(), *arguments)
         val commandArray = command.toTypedArray()
         val usePty = config.getAttribute("ch.hsr.ifs.testframework.launcher.useTerminal", true)
         monitor.worked(2)

         val process = exec(commandArray, this.getEnvironment(config), wd, usePty)
         monitor.worked(6)

         DebugPlugin.newProcess(launch, process, renderProcessLabel(commandArray[0]))
         val proc = launch.getProcesses()[0]
         val console = DebugUITools.getConsole(proc)
         if (console is TextConsole) {
            val job = ShowResultView()
            job.schedule()
            try {
               job.join()
            } catch (e: InterruptedException) {}
            exePath = sourcelookupPath(config, exePath)
            registerPatternMatchListener(launch, exePath, console)
         }

         notifyAfterLaunch(project)
         terminationRunner.execute{
            try {
               process.waitFor()
            } catch (e1: InterruptedException) {
               TestFrameworkPlugin.log(e1)
            }
            try {
               notifyTermination(project)
            } catch (e2: CoreException) {
               TestFrameworkPlugin.log(e2)
            }
         }
      } finally {
         monitor.done()
      }
   }

   protected fun registerPatternMatchListener(launch: ILaunch, exePath: IPath,textCons: TextConsole) {
      val handler = ConsoleLinkHandler(exePath, textCons)
      val modelHandler = ModellBuilder(exePath, launch)
      val listener = ConsolePatternListener(getConsoleEventParser())
      listener.addHandler(handler)
      listener.addHandler(modelHandler)
      textCons.addPatternMatchListener(listener)
   }

   protected fun notifyAfterLaunch(project: IProject) =
         getObservers().forEach{ it.notifyAfterLaunch(project) }

   protected fun notifyBeforeLaunch(project: IProject) =
         getObservers().forEach{ it.notifyBeforeLaunch(project) }

   protected fun notifyTermination(project: IProject) =
         getObservers().forEach{ it.notifyTermination(project) }

   private fun getObservers(): List<ILaunchObserver>  {
      val additions = mutableListOf<ILaunchObserver>()
      try {
         val extension = Platform.getExtensionRegistry().getExtensionPoint(TestFrameworkPlugin.PLUGIN_ID, "launchObserver")
         extension?.let{
            val extensions = extension.extensions
            extensions.forEach{
               val configElements = it.configurationElements
               additions.add(configElements[0].createExecutableExtension("class") as ILaunchObserver)
            }
         }
      } catch (e: Exception) {
      }
      return additions
   }

   override fun getEnvironment(config: ILaunchConfiguration): Array<String> {
      if(config.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, null as Map<String, String>?) == null) {
         return super.getEnvironment(config)
      }

      val array = DebugPlugin.getDefault().getLaunchManager().getEnvironment(config)
      return array ?: emptyArray()
   }

   public fun sourcelookupPath(config: ILaunchConfiguration?, exePath: IPath): IPath {
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
         TestFrameworkPlugin.getDefault().getLog().log(e.getStatus())
      }
      return exePath
   }

   protected fun exec(cmdLine: Array<String>, environ: Array<String>, workingDirectory: File?, usePty: Boolean): Process {
      var process: Process? = null
      try {
         if (workingDirectory == null) {
            process = ProcessFactory.getFactory().exec(cmdLine, environ)
         } else {
            if (usePty && PTY.isSupported()) {
               process = ProcessFactory.getFactory().exec(cmdLine, environ, workingDirectory, PTY())
            } else {
               process = ProcessFactory.getFactory().exec(cmdLine, environ, workingDirectory)
            }
         }
      } catch (e: IOException) {
         abort("IOException in exec()", e, 99)
      } catch (e: NoSuchMethodError) {
         // attempting launches on 1.2.* - no ability to set working
         // directory
         val status = Status(IStatus.ERROR, LaunchUIPlugin.getUniqueIdentifier(), 98, "Eclipse runtime does not support working directory.", e)
         val handler = DebugPlugin.getDefault().getStatusHandler(status)
         handler?.let{
            val result = handler.handleStatus(status, this)
            if(result is Boolean && result) {
               process = exec(cmdLine, environ, null, usePty)
            } 
         }
      }
      return process!!
   }

}
