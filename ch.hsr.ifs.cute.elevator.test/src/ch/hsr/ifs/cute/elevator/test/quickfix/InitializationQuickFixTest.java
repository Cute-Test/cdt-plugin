package ch.hsr.ifs.cute.elevator.test.quickfix;
import org.eclipse.core.resources.IMarker;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanQuickfixTest;
import ch.hsr.ifs.cute.elevator.checker.InitializationChecker;
import ch.hsr.ifs.cute.elevator.quickfix.InitializerQuickFix;


public class InitializationQuickFixTest extends CDTTestingCodanQuickfixTest {

    @Override
    protected String getProblemId() {
        return InitializationChecker.PROBLEM_ID;
    }
       
    @Override
    @Test
    public void runTest() throws Throwable {
        openActiveFileInEditor();
        for (IMarker marker : findMarkers()) {
            runQuickFix(marker, new InitializerQuickFix());
        }
        assertEquals(getExpectedSource(), getCurrentSource());
    }
}
