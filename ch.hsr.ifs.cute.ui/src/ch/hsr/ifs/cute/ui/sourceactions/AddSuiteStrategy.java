package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;

public class AddSuiteStrategy implements IAddStrategy {

	private final AddPushbackStatementStrategy decoratedStrategy;
	private final String nl;
	private final int insertOffset;

	public AddSuiteStrategy(AddPushbackStatementStrategy strategy) {
		this(strategy, -1);
	}

	public AddSuiteStrategy(AddPushbackStatementStrategy strategy, int insertOffset) {
		decoratedStrategy = strategy;
		nl = strategy.newLine;
		this.insertOffset = insertOffset;
	}

	public MultiTextEdit getEdit() {
		final MultiTextEdit mEdit = new MultiTextEdit();
		final String pushbackContent = decoratedStrategy.createPushBackContent();
		final int insertionPoint = insertOffset >= 0 ? insertOffset : decoratedStrategy.astTu.getFileLocation().getNodeLength();

		String code = "cute::suite make_suite(){" + nl + "\tcute::suite s;" + nl + "\ts.push_back(" + pushbackContent + ");" + nl + "\treturn s;" + nl + "}";
		InsertEdit edit = new InsertEdit(insertionPoint, code);
		mEdit.addChild(edit);
		return mEdit;
	}

}
