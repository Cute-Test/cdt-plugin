/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik Rapperswil, University of
 * applied sciences and others All rights reserved.
 * 
 * Contributors: Institute for Software - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.mockator.tests.extractinterface;

import org.junit.Before;

public class ExtractInterfaceExternalProjectTest extends ExtractInterfaceRefactoringTest {

  @Override
  @Before
  public void setUp() throws Exception {
    addReferencedProject("SUTProject", "SUTProject.rts");
    super.setUp();
  }
}
