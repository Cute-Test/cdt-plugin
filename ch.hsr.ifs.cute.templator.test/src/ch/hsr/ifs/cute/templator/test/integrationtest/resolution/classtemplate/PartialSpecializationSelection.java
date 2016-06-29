package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.classtemplate;

import org.junit.Test;

import ch.hsr.ifs.cute.templator.test.ClassTemplateResolutionTest;

public class PartialSpecializationSelection extends ClassTemplateResolutionTest {

	@Test
	public void resolvesToFirstDefinition() {
		firstStatementResolvesToDefinition(definitions.get(1));
	}
}
