package ch.hsr.ifs.cute.constificator.test.quickfix.functionparameters;

import org.eclipse.ui.IMarkerResolution;

import ch.hsr.ifs.cute.constificator.constants.Markers;
import ch.hsr.ifs.cute.constificator.quickfixes.FunctionParametersQuickFix;
import ch.hsr.ifs.cute.constificator.test.quickfix.QuickFixTest;

public abstract class TestBase extends QuickFixTest{

	@Override
	protected IMarkerResolution getQuickFix() {
		return (new FunctionParametersQuickFix()).setTesting(true);
	}

	@Override
	protected String getProblemId() {
		return Markers.FunctionParameters_MissingQualification;
	}

}
