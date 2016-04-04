package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.function;

import org.junit.Test;

import ch.hsr.ifs.cute.templator.plugin.asttools.data.AbstractResolvedNameInfo;
import ch.hsr.ifs.cute.templator.plugin.asttools.templatearguments.TemplateArgumentMap;
import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.cute.templator.test.FunctionTemplateResolutionTest;

public class GeneralWithTwoLevelTest extends FunctionTemplateResolutionTest {
	@Test
	public void testNoAutomaticResolvedSubcalls() throws TemplatorException {
		int numberOfResolvedSubcallsBefore = firstStatementInMain.getSubNames().size();
		assertEquals(0, numberOfResolvedSubcallsBefore);

		firstStatementInMain.searchSubNames(loadingProgress);

		int numberOfResolvedSubcallsAfter = firstStatementInMain.getSubNames().size();
		assertEquals(1, numberOfResolvedSubcallsAfter);
	}

	@Test
	public void testArgumentMapFromSubcallIsEmptyAndNotNull() throws TemplatorException {
		firstStatementInMain.searchSubNames(loadingProgress);

		AbstractResolvedNameInfo subcall = firstStatementInMain.getSubNames().get(0).getInfo();
		TemplateArgumentMap mapFromSubcall = subcall.getTemplateArgumentMap();

		assertEquals(new TemplateArgumentMap(), mapFromSubcall);
	}

}
