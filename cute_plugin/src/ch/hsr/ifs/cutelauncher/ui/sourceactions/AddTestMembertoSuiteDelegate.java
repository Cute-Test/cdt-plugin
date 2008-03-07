package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import org.eclipse.text.edits.TextEdit;

public class AddTestMembertoSuiteDelegate extends
		AbstractFunctionActionDelegate {
	public AddTestMembertoSuiteDelegate(){
		super("AddTestMember",new AddTestMembertoSuiteAction());
	}
	
	
	@Override
	int getCursorEndPosition(TextEdit[] edits, String newLine) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	int getExitPositionLength() {
		// TODO Auto-generated method stub
		return 0;
	}

}
