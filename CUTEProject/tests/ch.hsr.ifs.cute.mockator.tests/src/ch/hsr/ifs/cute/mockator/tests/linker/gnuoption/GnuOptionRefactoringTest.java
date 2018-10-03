package ch.hsr.ifs.cute.mockator.tests.linker.gnuoption;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.cute.mockator.linker.wrapfun.gnuoption.GnuOptionRefactoring;
import ch.hsr.ifs.cute.mockator.tests.AbstractRefactoringTest;


public class GnuOptionRefactoringTest extends AbstractRefactoringTest {

    @Override
    protected Refactoring createRefactoring() {
        return new GnuOptionRefactoring(getPrimaryCElementFromCurrentProject().get(), getSelectionOfPrimaryTestFile(), getCurrentCProject());
    }
}
