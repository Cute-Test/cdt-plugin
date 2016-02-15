package ch.hsr.ifs.constificator.test.quickfix.localvariables;

import org.eclipse.ui.IMarkerResolution;

import ch.hsr.ifs.constificator.constants.Markers;
import ch.hsr.ifs.constificator.quickfixes.LocalVariablesQuickFix;
import ch.hsr.ifs.constificator.test.quickfix.QuickFixTest;

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
