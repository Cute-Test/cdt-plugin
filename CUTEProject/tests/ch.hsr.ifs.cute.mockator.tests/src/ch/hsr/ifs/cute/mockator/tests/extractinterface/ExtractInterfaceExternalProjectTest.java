/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik Rapperswil, University of
 * applied sciences and others All rights reserved.
 *
 * Contributors: Institute for Software - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cute.mockator.tests.extractinterface;

public class ExtractInterfaceExternalProjectTest extends ExtractInterfaceRefactoringTest {

   @Override
   protected void initReferencedProjects() throws Exception {
      stageReferencedProjectForBothProjects("SUTProject", "SUTProject.rts");
      super.initReferencedProjects();
   }

}
