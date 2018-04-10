/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.tests.patternlistener;

import java.io.IOException;

import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.testframework.launch.ConsolePatternListener;
import ch.hsr.ifs.testframework.tests.PatternListenerBase;
import ch.hsr.ifs.testframework.tests.mock.DummyTestEventHandler;


/**
 * @author Emanuel Graf
 *
 */
public class PatternListenerTestSuccessTest extends PatternListenerBase {

   private static final String TEST_NAME_EXP = "xUnitTest";
   private static final String MSG_EXP       = "OK";

   private String testNameStart;
   private String testNameEnd;
   private String msgEnd;

   final class TestSuccessHandler extends DummyTestEventHandler {

      @Override
      protected void handleSuccess(IRegion reg, String name, String msg) {
         testNameEnd = name;
         msgEnd = msg;
      }

      @Override
      protected void handleTestStart(IRegion reg, String testname) {
         testNameStart = testname;
      }

   }

   public void testListenerEvents() throws IOException, InterruptedException {
      emulateTestRun();
      assertEquals("Teststart name", TEST_NAME_EXP, testNameStart);
      assertEquals("Testend name", TEST_NAME_EXP, testNameEnd);
      assertEquals("Message", MSG_EXP, msgEnd);
   }

   @Override
   protected void addTestEventHandler(ConsolePatternListener lis) {
      lis.addHandler(new TestSuccessHandler());
   }

   @Override
   protected String getInputFileName() {
      return "successTest.txt";
   }
}
