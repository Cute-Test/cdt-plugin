package ch.hsr.ifs.cute.templator.test.asttests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    //@formatter:off
    ExtractTemplateInstanceNameTest.class,
    FindFirstAncestorByTypeTest.class,
    GetFunctionBindingTest.class,
    GetFunctionDefinitionFromBindingTest.class,
    GetNodeAtRegionTest.class,
    IsFunctionTemplateInstanceTest.class,
    ResolveTargetBindingTest.class,
    SourceFileContentTest.class
    //@formatter:on	
})
public class ASTRelatedTestSuite {
}