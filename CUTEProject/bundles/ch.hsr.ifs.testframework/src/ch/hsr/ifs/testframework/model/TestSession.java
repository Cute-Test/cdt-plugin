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

import org.eclipse.debug.core.ILaunch;


public class TestSession implements ITestComposite {

   private final List<TestElement>            rootElements = new ArrayList<>();
   private final List<ITestCompositeListener> listeners    = new ArrayList<>();;

   private final ILaunch launch;

   public TestSession(ILaunch launch) {
      super();
      this.launch = launch;
   }

   public List<TestElement> getRootElements() {
      return rootElements;
   }

   public ILaunch getLaunch() {
      return launch;
   }

   @Override
   public void addTestElement(TestElement element) {
      rootElements.add(element);
      element.setParent(this);
      for (ITestCompositeListener lis : listeners) {
         lis.newTestElement(this, element);
      }
   }

   @Override
   public List<TestElement> getElements() {
      return rootElements;
   }

   @Override
   public int getError() {
      int tot = 0;
      for (TestElement tElement : rootElements) {
         if (tElement instanceof ITestComposite) {
            ITestComposite testComp = (ITestComposite) tElement;
            tot += testComp.getError();
         } else if (tElement instanceof TestCase) {
            TestCase tCase = (TestCase) tElement;
            if (tCase.getStatus() == TestStatus.error) {
               ++tot;
            }
         }
      }
      return tot;
   }

   @Override
   public int getFailure() {
      int tot = 0;
      for (TestElement tElement : rootElements) {
         if (tElement instanceof ITestComposite) {
            ITestComposite testComp = (ITestComposite) tElement;
            tot += testComp.getFailure();
         } else if (tElement instanceof TestCase) {
            TestCase tCase = (TestCase) tElement;
            if (tCase.getStatus() == TestStatus.failure) {
               ++tot;
            }
         }
      }
      return tot;
   }

   @Override
   public int getRun() {
      int tot = 0;
      for (TestElement tElement : rootElements) {
         if (tElement instanceof ITestComposite) {
            ITestComposite testComp = (ITestComposite) tElement;
            tot += testComp.getRun();
         } else if (tElement instanceof TestCase) {
            TestCase tCase = (TestCase) tElement;
            if (tCase.getStatus() == TestStatus.error || tCase.getStatus() == TestStatus.failure || tCase.getStatus() == TestStatus.success) {
               ++tot;
            }
         }
      }
      return tot;
   }

   @Override
   public int getSuccess() {
      int tot = 0;
      for (TestElement tElement : rootElements) {
         if (tElement instanceof ITestComposite) {
            ITestComposite testComp = (ITestComposite) tElement;
            tot += testComp.getSuccess();
         } else if (tElement instanceof TestCase) {
            TestCase tCase = (TestCase) tElement;
            if (tCase.getStatus() == TestStatus.success) {
               ++tot;
            }
         }
      }
      return tot;
   }

   @Override
   public int getTotalTests() {
      int tot = 0;
      for (TestElement tElement : rootElements) {
         if (tElement instanceof ITestComposite) {
            ITestComposite testComp = (ITestComposite) tElement;
            tot += testComp.getTotalTests();
         } else if (tElement instanceof TestCase) {
            ++tot;
         }
      }
      return tot;
   }

   @Override
   public boolean hasErrorOrFailure() {
      return getFailure() + getError() > 0;
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

   @Override
   public String getRerunName() {
      // errr? what? primitive obsession? (lfelber)
      return ""; // empty means all tests
   }

}
