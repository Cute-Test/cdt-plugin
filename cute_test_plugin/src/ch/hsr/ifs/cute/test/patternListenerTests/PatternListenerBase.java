package ch.hsr.ifs.cute.test.patternListenerTests;

import ch.hsr.ifs.cute.test.internal.console.ConsoleTest;

public abstract class PatternListenerBase extends ConsoleTest {
	@Override
	protected String getInputFilePath() {
		return "patternListenerTests/" + getInputFileName(); //$NON-NLS-1$
	}

	protected abstract String getInputFileName();
}