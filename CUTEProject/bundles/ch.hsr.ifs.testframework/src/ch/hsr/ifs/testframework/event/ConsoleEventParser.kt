/*******************************************************************************
 * Copyright (c) 2008, Industrial Logic, Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Industrial Logic, Inc.: Mike Bria & John Tangney - initial implementation (based on ideas originating from the work of Emanuel Graf)
 ******************************************************************************/
package ch.hsr.ifs.testframework.event

import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.jface.text.IRegion

import ch.hsr.ifs.testframework.TestFrameworkPlugin


fun regExUnion(fragments: Array<String>): String {
   val buffer = StringBuffer()
   for(i in 0 until fragments.size) {
      if(i > 0) {
         buffer.append("|")
      }
      buffer.append(fragments[i])
   }
return buffer.toString()
}

fun escapeForRegex(string: String) = string.replace("]", "\\]").replace("[", "\\[")

abstract class ConsoleEventParser {

   protected lateinit var testEvents: MutableList<TestEvent>

   abstract fun getComprehensiveLinePattern(): String

   abstract fun getLineQualifier(): String

   protected abstract fun extractTestEventsFor(reg: IRegion, line: String)

   companion object {
   }

   fun eventsFrom(reg: IRegion, line: String): List<TestEvent> {
      freshTestEventCollection()
      try {
         extractTestEventsFor(reg, line)
      } catch (e: CoreException) {
         TestFrameworkPlugin.log(e.getStatus())
         throwLineParsingException(reg, line, e)
      } catch (e: Exception) {
         throwLineParsingException(reg, line, e)
      }
      return testEvents
   }

   protected fun freshTestEventCollection() {
      testEvents = ArrayList()
   }

   protected fun throwLineParsingException(reg: IRegion, line: String, e: Exception): Nothing =
      throw RuntimeException("Failure parsing console event {<line=$line>, <Reg=$reg>} into TestEvent. Check log for more information", e)

   protected fun matcherFor(pattern: Pattern, line: String): Matcher {
      val m = pattern.matcher(line)
      if (!m.matches()) {
         throw CoreException(Status(IStatus.ERROR, TestFrameworkPlugin.PLUGIN_ID, 1, "Pattern don't match", null))
      }
      return m
   }

}
