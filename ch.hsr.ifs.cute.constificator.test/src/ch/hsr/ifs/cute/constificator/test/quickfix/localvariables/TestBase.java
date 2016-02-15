package ch.hsr.ifs.cute.constificator.test.quickfix.localvariables;

import org.eclipse.ui.IMarkerResolution;

import ch.hsr.ifs.cute.constificator.constants.Markers;
import ch.hsr.ifs.cute.constificator.quickfixes.LocalVariablesQuickFix;
import ch.hsr.ifs.cute.constificator.test.quickfix.QuickFixTest;

public abstract class TestBase extends QuickFixTest {

	@Override
	protected IMarkerResolution getQuickFix() {
		return new LocalVariablesQuickFix();
	}

	@Override
	protected String getProblemId() {
		return Markers.LocalVariables_MissingQualification;
	}

}
