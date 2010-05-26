package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.text.edits.TextEdit;

public class AddTestMembertoSuiteDelegate extends
		AbstractFunctionActionDelegate {
	public AddTestMembertoSuiteDelegate(){
		super("AddTestMember",new AddTestMemberToSuiteAction()); //$NON-NLS-1$
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
