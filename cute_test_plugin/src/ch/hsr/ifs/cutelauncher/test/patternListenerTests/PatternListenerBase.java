package ch.hsr.ifs.cutelauncher.test.patternListenerTests;

import ch.hsr.ifs.cutelauncher.test.internal.console.ConsoleTest;

public abstract class PatternListenerBase extends ConsoleTest {
	@Override
	protected String getInputFilePath() {
		return "patternListenerTests/" + getInputFileName();
	}

	protected abstract String getInputFileName();
}