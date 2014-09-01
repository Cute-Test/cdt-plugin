package ch.hsr.ifs.cute.charwars.quickfixes.cstring.general;

import org.eclipse.ui.IMarkerResolution;

import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.quickfixes.BaseQuickFixTest;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.general.CStringQuickFix;

public abstract class BaseCStringQuickFixTest extends BaseQuickFixTest {
	@Override
	protected String getProblemId() {
		return ProblemIDs.C_STRING_PROBLEM;
	}
	
	@Override
	protected IMarkerResolution getQuickFix() {
		return new CStringQuickFix();
	}
}