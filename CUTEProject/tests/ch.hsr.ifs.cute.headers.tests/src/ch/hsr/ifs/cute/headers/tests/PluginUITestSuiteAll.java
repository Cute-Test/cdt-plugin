/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.headers.tests;

import ch.hsr.ifs.cute.headers.tests.tests.CopyHeaders2_0Test;
import ch.hsr.ifs.cute.headers.tests.tests.CopyHeaders2_1Test;
import ch.hsr.ifs.cute.headers.tests.tests.CopyHeaders2_2Test;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 */
public class PluginUITestSuiteAll extends TestSuite {

   public PluginUITestSuiteAll() {
      super("All Header Tests");
      addTestSuite(CopyHeaders2_2Test.class);
      addTestSuite(CopyHeaders2_1Test.class);
      addTestSuite(CopyHeaders2_0Test.class);
   }

   public static Test suite() {
      return new PluginUITestSuiteAll();
   }
}
