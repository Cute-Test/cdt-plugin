package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.text.edits.TextEdit;
public class AddTestFunctortoSuiteDelegate extends
		AbstractFunctionActionDelegate {
	public AddTestFunctortoSuiteDelegate(){
		super("AddTestFunctortoSuite",new AddTestFunctortoSuiteAction()); //$NON-NLS-1$
	}
	
	@Override
	int getCursorEndPosition(TextEdit[] edits, String newLine) {
		return 0;
	}

	@Override
	int getExitPositionLength() {
		return 0;
	}

}
