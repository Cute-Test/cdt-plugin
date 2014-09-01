package ch.hsr.ifs.cute.charwars.checkers;

import ch.hsr.ifs.cdttesting.rts.junit4.RunFor;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;

@RunFor(rtsFile="/resources/Checkers/CStringCleanupChecker.rts")
public class CStringCleanupCheckerTest extends BaseCheckerTest {
	@Override
	protected String getProblemId() {
		return ProblemIDs.C_STRING_CLEANUP_PROBLEM;
	}
}