/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.model;

import java.util.ArrayList;
import java.util.List;


/**
 * @author egraf
 *
 */
public class TestSuite extends TestElement implements ITestComposite, ITestElementListener {

   private String name = "";

   private int totalTests = 0;
   private int success    = 0;
   private int failure    = 0;
   private int error      = 0;

   private TestStatus status;

   private final ArrayList<TestElement>            cases     = new ArrayList<>();
   private final ArrayList<ITestCompositeListener> listeners = new ArrayList<>();

   public TestSuite(String name, int totalTests, TestStatus status) {
      super();
      this.name = name;
      this.totalTests = totalTests;
      this.status = status;
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public TestStatus getStatus() {
      return status;
   }

   protected void endTest(TestElement tCase) {
      switch (tCase.getStatus()) {
      case success:
         ++success;
         break;
      case failure:
         ++failure;
         break;
      case error:
         ++error;
         break;
      default:
         break;
      }
      notifyListeners(new NotifyEvent(NotifyEvent.EventType.testFinished, tCase));
   }

   private void setEndStatus() {
      if (cases.size() == 0) {
         status = TestStatus.success;
      } else {
         for (TestElement tCase : cases) {
            switch (status) {
            case running:
               status = tCase.getStatus();
               break;
            case success:
               if (tCase.getStatus() != TestStatus.success) {
                  status = tCase.getStatus();
               }
               break;
            case failure:
               if (tCase.getStatus() == TestStatus.error) {
                  status = tCase.getStatus();
               }
               break;
            default:
               // nothing
            }
         }
      }
   }

   @Override
   public int getError() {
      return error;
   }

   @Override
   public int getFailure() {
      return failure;
   }

   @Override
   public int getSuccess() {
      return success;
   }

   @Override
   public int getTotalTests() {
      return totalTests;
   }

   @Override
   public boolean hasErrorOrFailure() {
      return failure + error > 0;
   }

   @Override
   public int getRun() {
      return success + failure + error;
   }

   @Override
   public String toString() {
      return getName();
   }

   public void end(TestCase currentTestCase) {
      if (testsPerformed() != getTotalTests() && currentTestCase != null) {
         currentTestCase.endTest(null, 0, new TestResult("Test ended unexpectedly"), TestStatus.error);
      }
      setEndStatus();
      notifyListeners(new NotifyEvent(NotifyEvent.EventType.suiteFinished, this));
   }

   private int testsPerformed() {
      return error + failure + success;
   }

   @Override
   public void addTestElement(TestElement element) {
      cases.add(element);
      element.setParent(this);
      element.addTestElementListener(this);
      for (ITestCompositeListener lis : listeners) {
         lis.newTestElement(this, element);
      }
   }

   @Override
   public List<TestElement> getElements() {
      return cases;
   }

   @Override
   public void modelCanged(TestElement source, NotifyEvent event) {
      if (event.getType() == NotifyEvent.EventType.testFinished) {
         endTest(source);
      }

   }

   @Override
   public void addListener(ITestCompositeListener listener) {
      if (!listeners.contains(listener)) {
         listeners.add(listener);
      }
   }

   @Override
   public void removeListener(ITestCompositeListener listener) {
      listeners.remove(listener);
   }

}