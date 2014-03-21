package ch.hsr.ifs.cute.elevator.test.quickfix;
import org.eclipse.core.resources.IMarker;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanQuickfixTest;
import ch.hsr.ifs.cute.elevator.checker.InitializerChecker;
import ch.hsr.ifs.cute.elevator.quickfix.InitializerQuickFix;


public class InitializerQuickFixTest extends CDTTestingCodanQuickfixTest {

    @Override
    protected String getProblemId() {
        return InitializerChecker.PROBLEM_ID;
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
