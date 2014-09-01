package ch.hsr.ifs.cute.charwars.quickfixes.cstring.parameter;

import org.eclipse.ui.IMarkerResolution;

import ch.hsr.ifs.cdttesting.rts.junit4.RunFor;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.quickfixes.BaseQuickFixTest;

@RunFor(rtsFile="/resources/QuickFixes/CStringParameterQuickFix.rts")
public class CStringParameterQuickFixTest extends BaseQuickFixTest {
	@Override
	protected String getProblemId() {
		return ProblemIDs.C_STRING_PARAMETER_PROBLEM;
	}

	@Override
	protected IMarkerResolution getQuickFix() {
		return new CStringParameterQuickFix();
	}
}
