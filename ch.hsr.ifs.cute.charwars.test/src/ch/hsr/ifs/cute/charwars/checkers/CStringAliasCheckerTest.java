package ch.hsr.ifs.cute.charwars.checkers;

import ch.hsr.ifs.cdttesting.rts.junit4.RunFor;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;

@RunFor(rtsFile="/resources/Checkers/CStringAliasChecker.rts")
public class CStringAliasCheckerTest extends BaseCheckerTest {
	@Override
	protected String getProblemId() {
		return ProblemIDs.C_STRING_ALIAS_PROBLEM;
	}
}