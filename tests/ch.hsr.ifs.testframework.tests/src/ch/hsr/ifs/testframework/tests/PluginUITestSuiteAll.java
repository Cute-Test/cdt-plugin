/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.tests;

import ch.hsr.ifs.testframework.tests.hyperlink.HyperlinkSuite;
import ch.hsr.ifs.testframework.tests.modelbuilder.ModelBuilderSuite;
import ch.hsr.ifs.testframework.tests.patternlistener.PatternListenerSuite;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Emanuel Graf IFS
 *
 */
public class PluginUITestSuiteAll extends TestSuite {

   public PluginUITestSuiteAll() {
      super("Testframework All Core Tests");
      addTest(PatternListenerSuite.suite());
      addTest(ModelBuilderSuite.suite());
      addTest(HyperlinkSuite.suite());
   }

   public static Test suite() {
      return new PluginUITestSuiteAll();
   }

}
