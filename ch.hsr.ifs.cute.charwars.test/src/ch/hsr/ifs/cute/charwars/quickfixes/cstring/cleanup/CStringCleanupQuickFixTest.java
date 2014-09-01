package ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup;

import org.eclipse.ui.IMarkerResolution;

import ch.hsr.ifs.cdttesting.rts.junit4.RunFor;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.quickfixes.BaseQuickFixTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.cleanup.CStringCleanupQuickFix;

@RunFor(rtsFile="/resources/QuickFixes/CStringCleanupQuickFix.rts")
public class CStringCleanupQuickFixTest extends BaseQuickFixTest {
	@Override
	protected String getProblemId() {
		return ProblemIDs.C_STRING_CLEANUP_PROBLEM;
	}
	
	@Override
	protected IMarkerResolution getQuickFix() {
		return new CStringCleanupQuickFix();
	}
}
