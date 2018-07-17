/*******************************************************************************
 * Copyright (c) 2018, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/

package ch.hsr.ifs.testframework.tests.consoleparser;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.junit.Test;

import ch.hsr.ifs.testframework.event.TestEvent;
import ch.hsr.ifs.testframework.event.TestFailureEvent;
import ch.hsr.ifs.testframework.tests.mock.TestCuteConsoleEventParser;
import junit.framework.TestCase;


public class ConsoleParserFailureTest extends TestCase {

   public ConsoleParserFailureTest(String name) {
      super(name);
   }

   @Test

   public void testFailureContentWithColon() {

      TestCuteConsoleEventParser parser = new TestCuteConsoleEventParser();
      String failureInput = "#failure poly::testComposite ../src/Test.cpp:80 testComposite: \"{ circle:42rectangle:4,circle:4 }\" == out.str() expected:  { circle:42rectangle:4,circle:4 }  but was:  { circle:42rectangle:4,2circle:4 }  ";
      IRegion dummyRegion = new Region(0, 0);
      List<TestEvent> eventsFrom = parser.eventsFrom(dummyRegion, failureInput);
      assertThat(eventsFrom.size(), is(1));

      TestEvent firstEvent = eventsFrom.get(0);
      assertThat(firstEvent, is(instanceOf(TestFailureEvent.class)));

      TestFailureEvent failureEvent = (TestFailureEvent) firstEvent;
      assertThat(failureEvent.getTestName(), is(equalTo("poly::testComposite")));
      assertThat(failureEvent.getFileName(), is(equalTo("../src/Test.cpp")));
      assertThat(failureEvent.getLineNo(), is(equalTo("80")));
      assertThat(failureEvent.getReason(), is(equalTo("testComposite: \"{ circle:42rectangle:4,circle:4 }\" == out.str() expected:  { circle:42rectangle:4,circle:4 }  but was:  { circle:42rectangle:4,2circle:4 }  ")));

      assertThat(failureEvent.getReg(), is(dummyRegion));
   }

}
