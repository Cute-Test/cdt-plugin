package ch.hsr.ifs.testframework.test.mock;

import org.eclipse.jface.text.IRegion;

import ch.hsr.ifs.testframework.event.TestEventHandler;

public class DummyTestEventHandler extends TestEventHandler {

	@Override
	protected void handleBeginning(IRegion reg, String suitename, String suitesize) {
	}

	@Override
	protected void handleTestStart(IRegion reg, String testName) {
	}

	@Override
	protected void handleError(IRegion reg, String testName, String msg) {
	}

	@Override
	protected void handleSuccess(IRegion reg, String name, String msg) {
	}

	@Override
	protected void handleEnding(IRegion reg, String suitename) {
	}

	@Override
	protected void handleFailure(IRegion reg, String testName, String fileName, String lineNo, String reason) {
	}

	@Override
	public void handleSessionStart() {
	}

	@Override
	public void handleSessionEnd() {
	}

}
