/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.model;

import org.eclipse.debug.core.ILaunch;

private fun Boolean.toInt() = if (this) 1 else 0

class TestSession(val launch: ILaunch?) : ITestComposite {

   private val listeners = mutableListOf<ITestCompositeListener>()

   public val rootElements = mutableListOf<TestElement>()

   override fun addTestElement(element: TestElement) {
      rootElements.add(element);
      element.setParent(this);
      listeners.forEach{ it.newTestElement(this, element) }
   }

   override fun getElements() = rootElements


   override fun getError() = 
      rootElements.fold(0, {acc, te ->
         when(te) {
            is ITestComposite -> acc + te.getError()
            is TestCase -> acc + (te.getStatus() == TestStatus.error ).toInt()
            else -> acc
         }
      })

   override fun getFailure() =
      rootElements.fold(0, {acc, te ->
         when(te) {
            is ITestComposite -> acc + te.getFailure()
            is TestCase -> acc + (te.getStatus() == TestStatus.failure).toInt()
            else -> acc
         }
      })

   override fun getRun() =
      rootElements.fold(0, {acc, te ->
         when(te) {
            is ITestComposite -> acc + te.getRun()
            is TestCase -> acc + when(te.getStatus()){
               TestStatus.error, TestStatus.failure, TestStatus.success -> 1
               else -> 0
            }
            else -> acc
         }
      })

   override fun getSuccess() =
      rootElements.fold(0, {acc, te ->
         when(te) {
            is ITestComposite -> acc + te.getSuccess()
            is TestCase -> acc + (te.getStatus() == TestStatus.success).toInt()
            else -> acc
         }
      })

   override fun getTotalTests() =
      rootElements.fold(0, {acc, te ->
         when(te) {
            is ITestComposite -> acc + te.getTotalTests()
            is TestCase ->  acc + 1
            else -> acc
         }
      })

   override fun hasErrorOrFailure() = getFailure() + getError() > 0

   override fun addListener(listener: ITestCompositeListener) {
      if (!listeners.contains(listener)) {
         listeners.add(listener);
      }
   }

   override fun removeListener(listener: ITestCompositeListener) {
      listeners.remove(listener);
   }

   override fun getRerunName() = ""

}
