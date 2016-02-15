package ch.hsr.ifs.constificator.test.checker.localvariables;

import ch.hsr.ifs.constificator.constants.Markers;
import ch.hsr.ifs.constificator.test.checker.CheckerTest;

public abstract class TestBase extends CheckerTest {

	@Override
	protected String getProblemId() {
		return Markers.LocalVariables_MissingQualification;
	}

}
