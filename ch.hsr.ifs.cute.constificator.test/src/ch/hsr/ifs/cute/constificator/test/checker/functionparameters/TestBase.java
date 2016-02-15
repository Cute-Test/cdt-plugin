package ch.hsr.ifs.cute.constificator.test.checker.functionparameters;

import ch.hsr.ifs.cute.constificator.constants.Markers;
import ch.hsr.ifs.cute.constificator.test.checker.CheckerTest;

public abstract class TestBase extends CheckerTest {

	@Override
	protected String getProblemId() {
		return Markers.FunctionParameters_MissingQualification;
	}

}
