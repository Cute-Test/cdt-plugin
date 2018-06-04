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


/**
 * @author Emanuel Graf
 *
 */
class TestCase(private val name: String): TestElement() {

   private lateinit var status: TestStatus

   private var file: IFile? = null

   private var lineNumber = -1

   private lateinit var result: TestResult

   fun getFile() = file

   override fun getName() = name

	override fun getStatus() = status
	
	fun getLineNumber() = lineNumber

   fun getMessage() = 
         if(this::result.isInitialized) {
            result.msg
         } else {
            ""
         }

   override fun toString() = getName()

   fun endTest(file: IFile?, lineNumber: Int, result: TestResult, status: TestStatus) {
      this.file = file
      this.lineNumber = lineNumber
      this.result = result
      this.status = status
      notifyListeners(NotifyEvent(NotifyEvent.EventType.testFinished, this))
   }

   fun getResult() = result

}
