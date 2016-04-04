package ch.hsr.ifs.cute.templator.test.integrationtest.resolution.function.sameorder;

import org.junit.Test;

import ch.hsr.ifs.cute.templator.plugin.asttools.data.AbstractResolvedNameInfo;
import ch.hsr.ifs.cute.templator.plugin.asttools.templatearguments.TemplateArgumentMap;
import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.cute.templator.test.FunctionTemplateResolutionTest;

public class ResolvesToExactlyMatchingNonTemplateWithoutTemplateIdTest extends FunctionTemplateResolutionTest {

	@Test
	public void testOuterArgumentMapIsBool() {
		testOuterArgumentMap(BOOL);
	}

	@Test
	public void testNumberOfInnerSubcallsIsOne() throws TemplatorException {
		// the function name itself
		firstStatementInMain.searchSubNames(loadingProgress);
		AbstractResolvedNameInfo innerCall = getOnlyFunctionCallSubstatements(firstStatementInMain).get(0).getInfo();
		innerCall.searchSubNames(loadingProgress);
		assertEquals(1, getOnlyFunctionCallSubstatements(innerCall).size());
	}

	@Test
	public void testInnerArgumentMapIsEmpty() throws TemplatorException {
		firstStatementInMain.searchSubNames(loadingProgress);
		assertEquals(new TemplateArgumentMap(),
				getOnlyFunctionCallSubstatements(firstStatementInMain).get(0).getInfo().getTemplateArgumentMap());
	}

	@Test
	public void testSubcallResolvedToNormalFunctionAndNotFunctionTemplate() throws TemplatorException {
		testFirstInnerCallResolvesToFirstDefinition();
	}
}