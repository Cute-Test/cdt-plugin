/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Emanuel Graf - initial API and implementation
 * Industrial Logic, Inc.: Mike Bria & John Tangney - enhancements to support additional C++ unit testing frameworks, such as GTest
 ******************************************************************************/
package ch.hsr.ifs.testframework.event

import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.Path
import org.eclipse.jface.text.IRegion


private const val CYGDRIVE = "/cygdrive/"

abstract class TestEventHandler {

   fun handle(event: TestEvent) =
      when(event) {
         is TestStartEvent -> handleTestStart(event.reg, event.testName)
         is TestSuccessEvent -> handleSuccess(event.reg, event.testName, event.msg)
         is TestFailureEvent -> handleFailure(event.reg, event.testName, event.fileName, event.lineNo, event.reason)
         is TestErrorEvent -> handleError(event.reg, event.testName, event.msg)
         is SuiteBeginEvent -> handleBeginning(event.reg, event.suiteName, event.suiteSize)
         is SuiteEndEvent -> handleEnding(event.reg, event.suitename)
         is SessionStartEvent -> handleSessionStart()
         is SessionEndEvent -> handleSessionEnd()
         else -> Unit
      }

   protected abstract fun handleBeginning(reg: IRegion, suitename: String, suitesize: String)
   protected abstract fun handleTestStart(reg: IRegion, testName: String)
   protected abstract fun handleError(reg: IRegion, testName: String, msg: String)
   protected abstract fun handleSuccess(reg: IRegion, name:  String, msg: String)
   protected abstract fun handleEnding(reg: IRegion, suitename: String)
   protected abstract fun handleFailure(reg: IRegion, testName: String, fileName: String, lineNo: String, reason: String)

   abstract fun handleSessionStart()
   abstract fun handleSessionEnd()

   protected fun getWorkspaceFile(fileName: String, rtPath: IPath) =
         if(!Path(fileName).isAbsolute()) {
            rtPath.append(fileName)
         } else if(fileName.startsWith(CYGDRIVE)) {
            val adaptedName = fileName.replace(CYGDRIVE, "")
            Path.fromOSString("${adaptedName[0]}:${adaptedName.substring(1)}")
         } else {
            Path(fileName)
         }

}
