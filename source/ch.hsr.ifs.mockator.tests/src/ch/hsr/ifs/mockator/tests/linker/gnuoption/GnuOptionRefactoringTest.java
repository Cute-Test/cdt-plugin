package ch.hsr.ifs.mockator.tests.linker.gnuoption;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.GnuOptionRefactoring;
import ch.hsr.ifs.mockator.tests.MockatorRefactoringTest;

public class GnuOptionRefactoringTest extends MockatorRefactoringTest {

  @Override
  protected Refactoring createRefactoring() {
    return new GnuOptionRefactoring(getActiveCElement(), selection, cproject);
  }
}
