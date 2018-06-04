/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.model

import java.net.URI
import java.net.URISyntaxException

import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IWorkspaceRoot
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.IPath
import org.eclipse.debug.core.ILaunch
import org.eclipse.jface.text.IRegion

import ch.hsr.ifs.testframework.TestFrameworkPlugin
import ch.hsr.ifs.testframework.event.TestEventHandler
import ch.hsr.ifs.testframework.model.Model

/**
 * @author egraf
 *
 */
class ModellBuilder @JvmOverloads constructor(private val exePath: IPath, private val launch: ILaunch? = null) : TestEventHandler() {
 
   private val model = TestFrameworkPlugin.getModel()
   private var lastTestCase: TestCase? = null
   private var currentTestCase: TestCase? = null

   override fun handleError(reg: IRegion, testName: String , msg: String) {
      when(currentTestCase) {
         null -> unexpectedTestCaseEnd()
         else -> {
            model.endCurrentTestCase(null, -1, msg, TestStatus.error, currentTestCase!!)
            endTestCase()
         }
      }
   }

   fun unexpectedTestCaseEnd() {
      lastTestCase?.let{model.endCurrentTestCase(null, -1, Messages.ModellBuilder_0, TestStatus.error, it)}
   }

   fun endTestCase() {
      lastTestCase = currentTestCase
      currentTestCase = null
   }

   override fun handleSuccess(reg: IRegion, name: String, msg: String) {
      when(currentTestCase) {
         null -> unexpectedTestCaseEnd()
         else -> {
            model.endCurrentTestCase(null, -1, msg, TestStatus.success, currentTestCase!!)
            endTestCase()
         }
      }
   }

   override fun handleEnding(reg: IRegion, suitename: String) {
      model.endSuite()
   }

   override fun handleBeginning(reg: IRegion , suitename: String, suitesize: String) {
      model.startSuite(TestSuite(suitename, Integer.parseInt(suitesize), TestStatus.running))
   }


   override fun handleFailure(reg:IRegion , testName: String, fileName: String, lineNo: String, reason: String) {
      when(currentTestCase) {
         null -> unexpectedTestCaseEnd()
         else -> {
            val filePath = getWorkspaceFile(fileName, exePath)
   
            val root = ResourcesPlugin.getWorkspace().getRoot()
            var file = root.getFileForLocation(filePath)
            if (file == null) {
               try {
                  val files = root.findFilesForLocationURI(URI("file:" + filePath.toPortableString()), IResource.FILE)
                  if (files.size > 0) {
                     file = files[0]
                  }
               } catch (e: URISyntaxException) {}
            }
            val lineNumber = Integer.parseInt(lineNo)
            model.endCurrentTestCase(file, lineNumber, reason, TestStatus.failure, currentTestCase!!)
            endTestCase()
         }
      }
   }

   override fun handleTestStart(reg: IRegion, testName: String) {
      currentTestCase = TestCase(testName)
      model.addTest(currentTestCase!!)
   }

   override fun handleSessionEnd() = model.endSession(currentTestCase)

   override fun handleSessionStart() = model.startSession(launch)

}
