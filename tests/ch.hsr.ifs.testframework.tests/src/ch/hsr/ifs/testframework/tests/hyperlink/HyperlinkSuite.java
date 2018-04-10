/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.tests.hyperlink;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Emanuel Graf IFS
 *
 */
public class HyperlinkSuite {

   public static Test suite() {
      TestSuite suite = new TestSuite("Hyperlink Suite");
      suite.addTestSuite(HyperlinkTest.class);
      return suite;
   }

}
