/*******************************************************************************
 * Institute for Software
 * Contributors:
 *     Thomas Corbat - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.testsrunner.internal.cute;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;

public class CUTEOutputHandler {
	private static final String LINE_QUALIFIER = "#"; //$NON-NLS-1$
	private static final int LINEPREFIXLENGTH = LINE_QUALIFIER.length();
	private static final String BEGINNING = "beginning"; //$NON-NLS-1$
	private static final String ENDING = "ending"; //$NON-NLS-1$
	private static final String STARTTEST = "starting"; //$NON-NLS-1$
	private static final String SUCCESS = "success"; //$NON-NLS-1$
	private static final String FAILURE = "failure"; //$NON-NLS-1$
	private static final String ERROR = "error"; //$NON-NLS-1$

	private static Pattern SUITEBEGINNINGLINE = Pattern.compile(LINE_QUALIFIER
			+ BEGINNING + " (.*) (\\d+)$"); //$NON-NLS-1$
	private static Pattern TESTSTARTLINE = Pattern.compile(LINE_QUALIFIER
			+ STARTTEST + " (.*)$"); //$NON-NLS-1$
	private static Pattern TESTFAILURELINE = Pattern.compile(LINE_QUALIFIER
			+ FAILURE + " (.*) (.*):(\\d+) (.*)$"); //$NON-NLS-1$
	private static Pattern TESTSUCESSLINE = Pattern.compile(LINE_QUALIFIER
			+ SUCCESS + " (.*) (.*)$"); //$NON-NLS-1$
	private static Pattern TESTERRORLINE = Pattern.compile(LINE_QUALIFIER
			+ ERROR + " (.*?) (.*)$"); //$NON-NLS-1$

	private ITestModelUpdater modelUpdater;

	public CUTEOutputHandler(ITestModelUpdater modelUpdater) {
		this.modelUpdater = modelUpdater;
	}

	public void run(InputStream inputStream) throws IOException, CoreException {
		
		InputStreamReader inputReader = new InputStreamReader(inputStream);
		BufferedReader bufferedInputReader = new BufferedReader(inputReader);
		String inputLine;
		while((inputLine = bufferedInputReader.readLine()) != null){
			 extractTestEventsFor(inputLine);
		}
	}

	

	public String getLineQualifier() {
		return escapeBrackets(LINE_QUALIFIER);
	}

	public final String getComprehensiveLinePattern() {
		return escapeBrackets(LINE_QUALIFIER
				+ "(" //$NON-NLS-1$
				+ createLogicOr(new String[] { LINE_QUALIFIER, BEGINNING, ENDING,
						SUCCESS, STARTTEST, FAILURE, ERROR, }))
				+ ")(.*)(\\n)"; //$NON-NLS-1$
	}

	protected void extractTestEventsFor(String line)
			throws CoreException {
		if (testStarting(line))
			testStart(line);
		else if (testSucceeded(line))
			testSuccess(line);
		else if (testFailed(line))
			testFailure(line);
		else if (suiteStarting(line))
			suiteStarted(line);
		else if (suiteEnding(line))
			suiteEnded(line);
		else if (testErrored(line))
			testError(line);
	}

	private boolean testStarting(String line) {
		return line.startsWith(STARTTEST, LINEPREFIXLENGTH);
	}

	private void testStart(String line) throws CoreException {
		Matcher m = matcherFor(TESTSTARTLINE, line);
		final String testName = m.group(1);
		modelUpdater.enterTestCase(testName);
	}

	private boolean testSucceeded(String line) {
		return line.startsWith(SUCCESS, LINEPREFIXLENGTH);
	}

	private void testSuccess(String line) throws CoreException {
		Matcher m = matcherFor(TESTSUCESSLINE, line);
		modelUpdater.setTestStatus(ITestItem.Status.Passed);
		final String file = ""; //$NON-NLS-1$
		final int lineNr = 0;
		final String successMessage = m.group(2);
		modelUpdater.addTestMessage(file, lineNr, ITestMessage.Level.Info, successMessage);
		modelUpdater.exitTestCase();
	}

	private boolean testFailed(String line) {
		return line.startsWith(FAILURE, LINEPREFIXLENGTH);
	}

	private void testFailure(String line) throws CoreException {
		Matcher m = matcherFor(TESTFAILURELINE, line);
		modelUpdater.setTestStatus(ITestItem.Status.Failed);
		final String file = m.group(2);
		final int lineNr = Integer.parseInt(m.group(3));
		final String failMessage = m.group(4);
		modelUpdater.addTestMessage(file, lineNr, ITestMessage.Level.Warning, failMessage);
		modelUpdater.exitTestCase();
	}

	private boolean suiteStarting(String line) {
		return line.startsWith(BEGINNING, LINEPREFIXLENGTH);
	}

	private void suiteStarted(String line) throws CoreException {
		Matcher m = matcherFor(SUITEBEGINNINGLINE, line);
		final String name = m.group(1);
		modelUpdater.enterTestSuite(name);
	}

	private boolean suiteEnding(String line) {
		return line.startsWith(ENDING, LINEPREFIXLENGTH);
	}

	private void suiteEnded(String line) throws CoreException {
		modelUpdater.exitTestSuite();
	}

	private boolean testErrored(String line) {
		return line.startsWith(ERROR, LINEPREFIXLENGTH);
	}

	private void testError(String line) throws CoreException
	{
		Matcher m = matcherFor(TESTERRORLINE, line);
		modelUpdater.setTestStatus(ITestItem.Status.Aborted);
		final String file = ""; //$NON-NLS-1$
		final int lineNr = 0;
		final String errorMessage = m.group(2);
		modelUpdater.addTestMessage(file, lineNr, ITestMessage.Level.Exception , errorMessage);
		modelUpdater.exitTestCase();
	}

	
	protected static String escapeBrackets(String string) {
		return string.replace("]", "\\]").replace("[", "\\[");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
	}
	
	protected static String createLogicOr(String[] parts) {

		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < parts.length; ++i) {
			if (i > 0){
				buffer.append("|"); //$NON-NLS-1$
			}
			buffer.append(parts[i]);
		}
		return buffer.toString();
	}
	
	protected Matcher matcherFor(Pattern pattern, String input)
			throws CoreException {
		Matcher m = pattern.matcher(input);
		if (!m.matches()) {
			throw new CoreException(new Status(Status.ERROR,
					Activator.PLUGIN_ID, 1, "Pattern does not match", //$NON-NLS-1$
					null));
		}
		return m;
	}
}
