/*******************************************************************************
 * Institute for Software
 * Contributors:
 *     Thomas Corbat - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.cute;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProvider;
import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
import org.eclipse.cdt.testsrunner.model.TestingException;
import org.eclipse.core.runtime.CoreException;

public class CUTETestsRunner implements ITestsRunnerProvider {

	private static final String CUTE_TEST_PATH_DELIMITER = "#"; //$NON-NLS-1$

	private StringBuilder assembleTestPath(String[] testPath) {
		StringBuilder path = new StringBuilder();
		path.append('"');
		for (int i = 0; i < testPath.length; i++) {
			if (i > 0) {
				path.append(CUTE_TEST_PATH_DELIMITER);
			}
			path.append(testPath[i]);
		}
		path.append('"');
		return path;
	}
	
	public void run(ITestModelUpdater modelUpdater, InputStream inputStream) throws TestingException {
		CUTEOutputHandler ouputHandler = new CUTEOutputHandler(modelUpdater);
		try {
			ouputHandler.run(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
			throw new TestingException("I/O Error: "+ e.getLocalizedMessage()); //$NON-NLS-1$
		} catch (CoreException e) {
			e.printStackTrace();
			throw new TestingException("I/O Error: "+ e.getLocalizedMessage()); //$NON-NLS-1$
		}
	}

	public String[] getAdditionalLaunchParameters(String[][] arg0)
			throws TestingException {
		ArrayList<String> command = new ArrayList<String>();
		
		if (arg0 != null) {
			for(String[] testPath : arg0)  {
				StringBuilder sb = assembleTestPath(testPath);
				command.add(sb.toString());
			}
		}
		
		return command.toArray(new String[command.size()]);
	}
}
