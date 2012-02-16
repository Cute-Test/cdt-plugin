package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

public class AddSuiteStrategy implements IAddStrategy {

	private final AddPushbackStatementStrategy decoratedStrategy;
	private final String newLine;
	private final int insertOffset;

	public AddSuiteStrategy(AddPushbackStatementStrategy strategy) {
		this(strategy, -1);
	}

	public AddSuiteStrategy(AddPushbackStatementStrategy strategy, int insertOffset) {
		decoratedStrategy = strategy;
		newLine = strategy.newLine;
		this.insertOffset = insertOffset;
	}

	public MultiTextEdit getEdit() {
		final MultiTextEdit mEdit = new MultiTextEdit();
		final String pushbackContent = decoratedStrategy.createPushBackContent();
		final int insertionPoint = insertOffset >= 0 ? insertOffset : decoratedStrategy.astTu.getFileLocation().getNodeLength();

		InsertEdit edit = new InsertEdit(insertionPoint, "cute::suite make_suite(){" + newLine + "\tcute::suite s;" + newLine + "\ts.push_back(" + pushbackContent + ");" + newLine
				+ "\treturn s;" + newLine + "}");
		mEdit.addChild(edit);
		return mEdit;
	}

}
