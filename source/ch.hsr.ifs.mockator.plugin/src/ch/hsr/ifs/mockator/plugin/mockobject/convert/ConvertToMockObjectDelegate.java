package ch.hsr.ifs.mockator.plugin.mockobject.convert;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.mockobject.MockObject;
import ch.hsr.ifs.mockator.plugin.mockobject.linkedmode.MockObjectLinkedEditModeFactory;
import ch.hsr.ifs.mockator.plugin.project.nature.MockatorLibHandler;
import ch.hsr.ifs.mockator.plugin.project.properties.AssertionOrder;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeStarter;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorDelegate;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoringRunner;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.ExistingTestDoubleMemFun;


public class ConvertToMockObjectDelegate extends MockatorDelegate {

   @Override
   protected void execute() {
      copyMockatorLibIfNecessary();
      performRefactoring();
   }

   private void copyMockatorLibIfNecessary() {
      try {
         new MockatorLibHandler(cProject.getProject()).addLibToProject();
      }
      catch (final CoreException e) {
         throw new MockatorException(e);
      }
   }

   private void performRefactoring() {
      final ConvertToMockObjectRefactoring refactoring = getRefactoring();
      new MockatorRefactoringRunner(refactoring).runInNewJob((edit) -> startLinkedMode(edit, refactoring.getNewMockObject().getPublicMemFuns(),
            refactoring.getNewMockObject()));
   }

   private ConvertToMockObjectRefactoring getRefactoring() {
      return new ConvertToMockObjectRefactoring(getCppStd(), cElement, selection, cProject, getLinkedEditStrategy());
   }

   private void startLinkedMode(final ChangeEdit edit, final Collection<ExistingTestDoubleMemFun> memFuns, final MockObject mockObject) {
      final MockObjectLinkedEditModeFactory factory = new MockObjectLinkedEditModeFactory(edit, memFuns, getCppStd(), getAssertionOrder(), Optional
            .of(mockObject.getNameForExpectationVector()));
      new LinkedModeStarter().accept(factory.getLinkedModeInfoCreator(getLinkedEditStrategy()));
   }

   private AssertionOrder getAssertionOrder() {
      return AssertionOrder.fromProjectSettings(cProject.getProject());
   }

   private LinkedEditModeStrategy getLinkedEditStrategy() {
      return getAssertionOrder().getLinkedEditModeStrategy(cProject.getProject());
   }
}
