package ch.hsr.ifs.mockator.tests.linker.gnuoption;

import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption.GnuOptionRefactoring;
import ch.hsr.ifs.mockator.tests.AbstractRefactoringTest;


public class GnuOptionRefactoringTest extends AbstractRefactoringTest {

   @Override
   protected Refactoring createRefactoring() {
      return new GnuOptionRefactoring(getActiveCElement(), selection, cproject);
   }
}
