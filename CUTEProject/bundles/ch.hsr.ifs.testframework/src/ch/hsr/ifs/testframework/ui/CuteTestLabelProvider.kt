/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui

import org.eclipse.jface.viewers.LabelProvider
import org.eclipse.swt.graphics.Image
import ch.hsr.ifs.testframework.TestFrameworkPlugin
import ch.hsr.ifs.testframework.model.TestCase
import ch.hsr.ifs.testframework.model.TestElement
import ch.hsr.ifs.testframework.model.TestSuite
import ch.hsr.ifs.testframework.model.TestStatus

/**
 * @author egraf
 *
 */
class CuteTestLabelProvider : LabelProvider() {
   private val suiteRun = TestFrameworkPlugin.getImageDescriptor("obj16/tsuiterun.gif").createImage()
   private val suiteOk = TestFrameworkPlugin.getImageDescriptor("obj16/tsuiteok.gif").createImage()
   private val suiteFail = TestFrameworkPlugin.getImageDescriptor("obj16/tsuitefail.gif").createImage()
   private val suiteError = TestFrameworkPlugin.getImageDescriptor("obj16/tsuiteerror.gif").createImage()
   private val testRun = TestFrameworkPlugin.getImageDescriptor("obj16/testrun.gif").createImage()
   private val testOk = TestFrameworkPlugin.getImageDescriptor("obj16/testok.gif").createImage()
   private val testFail = TestFrameworkPlugin.getImageDescriptor("obj16/testfail.gif").createImage()
   private val testError = TestFrameworkPlugin.getImageDescriptor("obj16/testerr.gif").createImage()

   override fun getImage(element: Any): Image? {
      if (element is TestSuite) {
         val suite = element as TestElement?
         return getSuiteImage(suite)
      } else if (element is TestCase) {
         val tCase = element as TestCase?
         return getTestCaseImage(tCase)
      } else {
         throw IllegalArgumentException(element.toString())
      }
   }

   /**
    * @since 3.0
    */
   protected fun getTestCaseImage(tCase: TestCase?): Image? {
      when (tCase!!.getStatus()) {
         TestStatus.running -> return testRun
         TestStatus.success -> return testOk
         TestStatus.failure -> return testFail
         TestStatus.error -> return testError
         else -> throw IllegalArgumentException(tCase.toString())
      }
   }

   /**
    * @since 3.0
    */
   protected fun getSuiteImage(suite: TestElement?): Image? {
      when (suite!!.getStatus()) {
         TestStatus.running -> return suiteRun
         TestStatus.success -> return suiteOk
         TestStatus.failure -> return suiteFail
         TestStatus.error -> return suiteError
         else -> throw IllegalArgumentException(suite.toString())
      }
   }

   override fun getText(element: Any?): String? {
      return element?.toString()
   }

   override fun dispose() {
// TODO Images disposen
   }
}