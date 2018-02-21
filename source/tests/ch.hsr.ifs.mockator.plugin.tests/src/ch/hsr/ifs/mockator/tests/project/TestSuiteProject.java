package ch.hsr.ifs.mockator.tests.project;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.hsr.ifs.mockator.tests.project.cdt.CompilerFlagHandlerTest;
import ch.hsr.ifs.mockator.tests.project.cdt.DiscoveryOptionsHandlerTest;
import ch.hsr.ifs.mockator.tests.project.cdt.IncludeFileHandlerTest;
import ch.hsr.ifs.mockator.tests.project.cdt.IncludePathHandlerTest;
import ch.hsr.ifs.mockator.tests.project.cdt.LinkerLibraryHandlerTest;
import ch.hsr.ifs.mockator.tests.project.cdt.LinkerOptionHandlerTest;
import ch.hsr.ifs.mockator.tests.project.cdt.MacroOptionHandlerTest;
import ch.hsr.ifs.mockator.tests.project.cdt.ProjRelPathGeneratorTest;
import ch.hsr.ifs.mockator.tests.project.cdt.SourceFolderHandlerTest;
import ch.hsr.ifs.mockator.tests.project.nature.NatureHandlerTest;


@RunWith(Suite.class)
//@formatter:off
@SuiteClasses({

   /*Project*/
   IncludeFileHandlerTest.class,
   MacroOptionHandlerTest.class,
   IncludePathHandlerTest.class,
   LinkerLibraryHandlerTest.class,
   SourceFolderHandlerTest.class,
   LinkerOptionHandlerTest.class,
   CompilerFlagHandlerTest.class,
   ProjRelPathGeneratorTest.class,
   DiscoveryOptionsHandlerTest.class,
   NatureHandlerTest.class,

})
public class TestSuiteProject {
}
