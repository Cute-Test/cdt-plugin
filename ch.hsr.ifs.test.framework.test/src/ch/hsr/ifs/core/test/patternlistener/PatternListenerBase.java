package ch.hsr.ifs.core.test.patternlistener;

import ch.hsr.ifs.core.test.ConsoleTest;

public abstract class PatternListenerBase extends ConsoleTest {
	@Override
	protected String getInputFilePath() {
		return "patternListenerTests/" + getInputFileName(); //$NON-NLS-1$
	}

	protected abstract String getInputFileName();
}