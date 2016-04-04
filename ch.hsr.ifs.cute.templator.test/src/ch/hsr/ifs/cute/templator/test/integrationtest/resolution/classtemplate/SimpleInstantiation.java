package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.classtemplate;

import org.junit.Test;

import ch.hsr.ifs.cute.templator.test.ClassTemplateResolutionTest;

public class SimpleInstantiation extends ClassTemplateResolutionTest {

    @Test
    public void resolvesToFirstDefinition() {
        firstStatementResolvesToFirstDefinition();
    }

}
