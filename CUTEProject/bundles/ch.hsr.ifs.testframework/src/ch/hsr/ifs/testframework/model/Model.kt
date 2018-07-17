/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.model

import org.eclipse.core.resources.IFile
import org.eclipse.debug.core.ILaunch

import ch.hsr.ifs.testframework.model.TestStatus.failure


class Model {

   private val sessionListeners = mutableListOf<ISessionListener>()

   private var currentParent: ITestComposite? = null

   private lateinit var session: TestSession

   fun startSession(launch: ILaunch?) {
      session = TestSession(launch)
      currentParent = session
      notifyListenerSessionStart(session)
   }

   fun startSuite(suite: TestSuite) {
      currentParent?.addTestElement(suite)
      currentParent = suite
   }

   fun addTest(test: TestCase) {
      currentParent?.addTestElement(test)
   }

   fun endCurrentTestCase(file: IFile?, lineNumber: Int, msg: String, status: TestStatus, tCase: TestCase) {
      var result: TestResult
      when (status) {
         failure -> result = TestFailure(msg)
         else -> result = TestResult(msg)
      }
      tCase.endTest(file, lineNumber, result, status)
   }

   fun endSuite() = 
      (currentParent as? TestSuite)?.apply {
         end(null)
         currentParent = getParent()
      }

   fun endSession(currentTestCase: TestCase?) {
      (currentParent as? TestSuite)?.apply {
         end(currentTestCase)
      }
      notifyListenerSessionEnd(session)
   }

   fun addListener(lis: ISessionListener) {
      if (!sessionListeners.contains(lis)) {
         sessionListeners.add(lis)
      }
   }

   fun removeListener(lis: ISessionListener) {
      sessionListeners.remove(lis)
   }

   fun notifyListenerSessionStart(session: TestSession) {
	   sessionListeners.forEach{ it.sessionStarted(session) }
   }

   fun notifyListenerSessionEnd(session: TestSession) {
	   sessionListeners.forEach{ it.sessionFinished(session) }
   }

   fun getSession() = session

}
