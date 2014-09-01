package ch.hsr.ifs.cute.charwars.quickfixes;

import org.eclipse.ui.IMarkerResolution;

import ch.hsr.ifs.cdttesting.rts.junit4.RunFor;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.quickfixes.pointerparameter.PointerParameterQuickFix;

@RunFor(rtsFile="/resources/QuickFixes/PointerParameterQuickFix.rts")
public class PointerParameterQuickFixTest extends BaseQuickFixTest {
	@Override
	protected String getProblemId() {
		return ProblemIDs.POINTER_PARAMETER_PROBLEM;
	}

	@Override
	protected IMarkerResolution getQuickFix() {
		return new PointerParameterQuickFix();
	}
}
