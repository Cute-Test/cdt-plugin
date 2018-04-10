/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.tests.modelbuilder;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.testframework.TestFrameworkPlugin;
import ch.hsr.ifs.testframework.launch.ConsolePatternListener;
import ch.hsr.ifs.testframework.model.ModellBuilder;
import ch.hsr.ifs.testframework.model.TestCase;
import ch.hsr.ifs.testframework.model.TestElement;
import ch.hsr.ifs.testframework.model.TestFailure;
import ch.hsr.ifs.testframework.model.TestResult;
import ch.hsr.ifs.testframework.model.TestSession;
import ch.hsr.ifs.testframework.model.TestSuite;
import ch.hsr.ifs.testframework.tests.ConsoleTest;
import junit.framework.Test;


/**
 * @author Emanuel Graf
 *
 */
public class ModelBuilderTest extends ConsoleTest {

   private static final String SEPARATOR = ", ";
   private final String        inputFile;

   public ModelBuilderTest(String inputFile) {
      super();
      this.inputFile = inputFile;
   }

   public static Test suite(String inputFile) {
      String testName = inputFile.split("\\.")[0];
      junit.framework.TestSuite suite = new junit.framework.TestSuite(testName);
      suite.addTest(new ModelBuilderTest(inputFile));
      return suite;
   }

   @Override
   protected void addTestEventHandler(ConsolePatternListener lis) {
      lis.addHandler(new ModellBuilder(new Path("")));
   }

   protected String getExpected() throws CoreException, IOException {
      return firstConsoleLine();
   }

   @Override
   public String getName() {
      return inputFile;
   }

   @Override
   protected void runTest() throws Throwable {
      emulateTestRun();
      TestSession session = TestFrameworkPlugin.getModel().getSession();
      assertEquals(getExpected(), getSessionString(session));
   }

   private String getSessionString(TestSession session) {
      StringBuffer sb = new StringBuffer();
      sb.append("Session{");
      List<TestElement> rootElements = session.getRootElements();
      writeElements(sb, rootElements);
      sb.append('}');
      return sb.toString();
   }

   private void writeTestCase(TestCase tcase, StringBuffer sb) {
      sb.append("Testcase(");
      sb.append(tcase.getName());
      sb.append(SEPARATOR);
      sb.append(tcase.getStatus().toString());
      sb.append(SEPARATOR);
      sb.append(tcase.getFile());
      sb.append(SEPARATOR);
      sb.append(tcase.getLineNumber());
      sb.append(SEPARATOR);
      writeTestResult(tcase.getResult(), sb);
      sb.append(')');
   }

   private void writeTestResult(TestResult result, StringBuffer sb) {
      sb.append("Result(");
      sb.append(result.getMsg());
      if (result instanceof TestFailure) {
         TestFailure failure = (TestFailure) result;
         sb.append(SEPARATOR);
         sb.append(failure.getExpected());
         sb.append(SEPARATOR);
         sb.append(failure.getWas());
      }
      sb.append(')');

   }

   private void writeSuite(TestSuite suite, StringBuffer sb) {
      sb.append("Suite(");
      sb.append(suite.getName());
      sb.append(SEPARATOR);
      sb.append(suite.getStatus().toString());
      sb.append(SEPARATOR);
      sb.append(suite.getTotalTests());
      sb.append(SEPARATOR);
      sb.append(suite.getRun());
      sb.append(SEPARATOR);
      sb.append(suite.getSuccess());
      sb.append(SEPARATOR);
      sb.append(suite.getFailure());
      sb.append(SEPARATOR);
      sb.append(suite.getError());
      sb.append("){");
      List<TestElement> elements = suite.getElements();
      writeElements(sb, elements);
      sb.append('}');

   }

   private void writeElements(StringBuffer sb, List<TestElement> elements) {
      for (TestElement element : elements) {
         if (element instanceof TestSuite) {
            TestSuite suite1 = (TestSuite) element;
            writeSuite(suite1, sb);
         } else if (element instanceof TestCase) {
            TestCase tcase = (TestCase) element;
            writeTestCase(tcase, sb);
         }
      }
   }

   @Override
   public String getInputFilePath() {
      return "modelBuilderTests/" + inputFile;
   }

}
