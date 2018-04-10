/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.model;

import org.eclipse.core.resources.IFile;


/**
 * @author Emanuel Graf
 *
 */
public class TestCase extends TestElement {

   private TestStatus status;

   private final String name;

   private IFile file;

   private int lineNumber = -1;

   private TestResult result;

   public TestCase(String name) {
      super();
      this.name = name;
      status = TestStatus.running;
   }

   public IFile getFile() {
      return file;
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public TestStatus getStatus() {
      return status;
   }

   public int getLineNumber() {
      return lineNumber;
   }

   public String getMessage() {
      if (result == null) {
         return "";
      } else {
         return result.getMsg();
      }
   }

   @Override
   public String toString() {
      return getName();
   }

   public void endTest(IFile file, int lineNumber, TestResult result, TestStatus status) {
      this.file = file;
      this.lineNumber = lineNumber;
      this.result = result;
      this.status = status;
      notifyListeners(new NotifyEvent(NotifyEvent.EventType.testFinished, this));
   }

   public TestResult getResult() {
      return result;
   }

}
