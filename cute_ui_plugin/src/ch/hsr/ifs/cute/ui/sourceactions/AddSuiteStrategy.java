package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

public class AddSuiteStrategy extends AddStrategy {

	private final AddPushbackStatementStrategy decoratedStrategy;
	private final String newLine;

	public AddSuiteStrategy(AddPushbackStatementStrategy strategy) {
		decoratedStrategy = strategy;
		newLine = strategy.newLine;
	}

	@Override
	public MultiTextEdit getEdit() {
		final MultiTextEdit mEdit = new MultiTextEdit();
		final String pushbackContent = decoratedStrategy.createPushBackContent();
		final int length = decoratedStrategy.astTu.getFileLocation().getNodeLength();
		InsertEdit edit = new InsertEdit(length, "cute::suite make_suite(){" + newLine + "\tcute::suite s;" + newLine + "\ts.push_back(" + pushbackContent + ");" + newLine
				+ "\treturn s;" + newLine + "}");
		mEdit.addChild(edit);
		return mEdit;
	}

}
