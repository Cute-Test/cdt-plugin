package ch.hsr.ifs.cute.charwars.quickfixes;

import org.eclipse.ui.IMarkerResolution;

import ch.hsr.ifs.cdttesting.rts.junit4.RunFor;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.quickfixes.array.ArrayQuickFix;

@RunFor(rtsFile="/resources/QuickFixes/ArrayQuickFix.rts")
public class ArrayQuickFixTest extends BaseQuickFixTest {
	@Override
	protected String getProblemId() {
		return ProblemIDs.ARRAY_PROBLEM;
	}

	@Override
	protected IMarkerResolution getQuickFix() {
		return new ArrayQuickFix();
	}
}
