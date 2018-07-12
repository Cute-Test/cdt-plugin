/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.model


/**
 * @author egraf
 *
 */
public class TestSuite(private val name: String, private var totalTests: Int, private var status: TestStatus) : TestElement(), ITestComposite, ITestElementListener {

   private var success = 0
   private var failure = 0
   private var error = 0

   private val cases = mutableListOf<TestElement>()
   private val listeners = mutableListOf<ITestCompositeListener>()

   override fun getName() = name

   override fun getStatus() = status

   protected fun endTest(tCase: TestElement) {
      @Suppress("NON_EXHAUSTIVE_WHEN")
      when(tCase.getStatus()) {
         TestStatus.success -> success++
         TestStatus.failure -> failure++
         TestStatus.error -> error++
      }
      notifyListeners(NotifyEvent(NotifyEvent.EventType.testFinished, tCase))
   }

   private fun setEndStatus() {
      if (cases.size == 0) {
         status = TestStatus.success
      } else {
         cases.forEach{
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when(status) {
               TestStatus.running -> status = it.getStatus()
               TestStatus.success ->
                  if (it.getStatus() != TestStatus.success) {
                     status = it.getStatus()
                  }
               TestStatus.failure -> 
                  if (it.getStatus() == TestStatus.error) {
                     status = it.getStatus()
                  }
            }
         }
      }
   }

   override fun getError() = error

   override fun getFailure() = failure

   override fun getSuccess() = success

   override fun getTotalTests() = totalTests

   override fun hasErrorOrFailure() = failure + error > 0

   override fun getRun() = success + failure + error

   override fun toString() = getName()

   fun end(currentTestCase: TestCase?) {
      if (testsPerformed() != getTotalTests() && currentTestCase != null) {
         currentTestCase.endTest(null, 0, TestResult("Test ended unexpectedly"), TestStatus.error)
      }
      setEndStatus()
      notifyListeners(NotifyEvent(NotifyEvent.EventType.suiteFinished, this))
   }

   private fun testsPerformed() = error + failure + success

   override fun addTestElement(element: TestElement) {
      cases.add(element)
      element.setParent(this)
      element.addTestElementListener(this)
      listeners.forEach{ it.newTestElement(this, element) }
   }

   override fun getElements() = cases

   override fun modelCanged(source: TestElement, event:  NotifyEvent) {
      if (event.type == NotifyEvent.EventType.testFinished) {
         endTest(source)
      }
   }

   override fun addListener(listener: ITestCompositeListener) {
      if (!listeners.contains(listener)) {
         listeners.add(listener)
      }
   }

   override fun removeListener(listener: ITestCompositeListener) {
      listeners.remove(listener)
   }

}
