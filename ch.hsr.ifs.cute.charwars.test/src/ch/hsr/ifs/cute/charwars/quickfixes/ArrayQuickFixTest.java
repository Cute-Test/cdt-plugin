package ch.hsr.ifs.cute.charwars.quickfixes;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IMarkerResolution;

import ch.hsr.ifs.cdttesting.rts.junit4.RunFor;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.quickfixes.array.ArrayQuickFix;
import ch.hsr.ifs.cute.charwars.test.Activator;

@RunFor(rtsFile="/resources/QuickFixes/ArrayQuickFix.rts")
public class ArrayQuickFixTest extends BaseQuickFixTest {
	@Override
	protected String getProblemId() {
		return ProblemIDs.ARRAY_PROBLEM;
	}

	@Override
	protected IMarkerResolution getQuickFix() {
		return new ArrayQuickFix();
	}

	@Override
	public void runTest() throws Throwable {
        if (getName().startsWith("-")) { //TODO: Fix tests with -
            String message = String.format("Test skipped because of name starting with '-'. Skipping test: %s%n", getName());
            Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 1, message, null);
            Activator.getDefault().getLog().log(status);
            return;
        }
        super.runTest();
	}
}
