package ch.hsr.ifs.cute.charwars.quickfixes;

import org.eclipse.ui.IMarkerResolution;

import ch.hsr.ifs.cdttesting.rts.junit4.RunFor;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.quickfixes.cstr.CStrQuickFix;

@RunFor(rtsFile="/resources/QuickFixes/CStrQuickFix.rts")
public class CStrQuickFixTest extends BaseQuickFixTest {
	@Override
	protected String getProblemId() {
		return ProblemIDs.C_STR_PROBLEM;
	}

	@Override
	protected IMarkerResolution getQuickFix() {
		return new CStrQuickFix();
	}
}
