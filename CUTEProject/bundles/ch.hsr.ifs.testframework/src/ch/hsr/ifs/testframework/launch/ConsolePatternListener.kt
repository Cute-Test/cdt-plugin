/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik
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
package ch.hsr.ifs.testframework.launch

import java.util.regex.Pattern

import org.eclipse.jface.text.BadLocationException
import org.eclipse.jface.text.IRegion
import org.eclipse.ui.console.IPatternMatchListener
import org.eclipse.ui.console.PatternMatchEvent
import org.eclipse.ui.console.TextConsole

import ch.hsr.ifs.testframework.event.ConsoleEventParser
import ch.hsr.ifs.testframework.event.TestEvent
import ch.hsr.ifs.testframework.event.TestEventHandler


/**
 * @since 3.0
 */
class ConsolePatternListener(private val eventParser: ConsoleEventParser) : IPatternMatchListener {

   private var console: TextConsole? = null
   private val handlers = mutableListOf<TestEventHandler>() 

   override fun getCompilerFlags() = Pattern.UNIX_LINES

   override fun getLineQualifier() = eventParser.getLineQualifier()

   override fun getPattern() = eventParser.getComprehensiveLinePattern()

   override fun connect(console: TextConsole) {
      this.console = console
	   handlers.forEach(TestEventHandler::handleSessionStart)
   }

   override fun disconnect() {
	   handlers.forEach(TestEventHandler::handleSessionEnd)
      console = null
   }

   fun addHandler(handler: TestEventHandler) {
      handlers.add(handler)
   }

   fun removeHandler(handler: TestEventHandler) {
      handlers.remove(handler)
   }

   override fun matchFound(event: PatternMatchEvent) {
      console?.let{
         try {
            val doc = it.document
            val reg = doc.getLineInformation(doc.getLineOfOffset(event.offset))
            val line = doc.get(reg.offset, reg.length)
            processTestEventsFrom(reg, line)
         } catch (e: BadLocationException) {
            throw RuntimeException(e)
         }
      }
   }

   private fun processTestEventsFrom(reg: IRegion, line: String) =
         eventParser.eventsFrom(reg, line).forEach{ evt -> handlers.forEach{ it.handle(evt) }}
}
