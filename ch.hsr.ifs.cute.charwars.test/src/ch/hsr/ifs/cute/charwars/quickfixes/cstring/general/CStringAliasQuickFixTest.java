package ch.hsr.ifs.cute.charwars.quickfixes.cstring.general;

import ch.hsr.ifs.cdttesting.rts.junit4.RunFor;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;

@RunFor(rtsFile="/resources/QuickFixes/CStringAliasQuickFix.rts")
public class CStringAliasQuickFixTest extends BaseCStringQuickFixTest {
	@Override
	protected String getProblemId() {
		return ProblemIDs.C_STRING_ALIAS_PROBLEM;
	}
}
