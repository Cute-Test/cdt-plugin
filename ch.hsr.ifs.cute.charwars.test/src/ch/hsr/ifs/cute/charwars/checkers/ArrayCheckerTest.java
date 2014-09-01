package ch.hsr.ifs.cute.charwars.checkers;

import ch.hsr.ifs.cdttesting.rts.junit4.RunFor;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;

@RunFor(rtsFile="/resources/Checkers/ArrayChecker.rts")
public class ArrayCheckerTest extends BaseCheckerTest {
	@Override
	protected String getProblemId() {
		return ProblemIDs.ARRAY_PROBLEM;
	}
}