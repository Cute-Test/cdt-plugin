package ch.hsr.ifs.cute.elevator.test.quickfix;
import org.eclipse.core.resources.IMarker;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingCodanQuickfixTest;
import ch.hsr.ifs.cute.elevator.checker.InitializationChecker;
import ch.hsr.ifs.cute.elevator.quickfix.NullMacroQuickFix;


public class ReplaceNullMacroQuickFixTest extends CDTTestingCodanQuickfixTest {

    @Override
    protected String getProblemId() {
        return InitializationChecker.NULL_MACRO;
    }
       
    @Override
    @Test
    public void runTest() throws Throwable {
        openActiveFileInEditor();
        for (IMarker marker : findMarkers()) {
            runQuickFix(marker, new NullMacroQuickFix());
        }
        assertEquals(getExpectedSource(), getCurrentSource());
    }
}
