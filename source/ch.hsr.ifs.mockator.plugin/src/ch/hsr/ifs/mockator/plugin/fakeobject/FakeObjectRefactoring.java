package ch.hsr.ifs.mockator.plugin.fakeobject;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.ClassPublicVisibilityInserter;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.TestDouble;
import ch.hsr.ifs.mockator.plugin.testdouble.qf.AbstractTestDoubleRefactoring;


@SuppressWarnings("restriction")
public class FakeObjectRefactoring extends AbstractTestDoubleRefactoring {

   public FakeObjectRefactoring(CppStandard cppStd, ICElement cElement, ITextSelection selection, ICProject cProject) {
      super(cppStd, cElement, selection, cProject);
   }

   @Override
   protected void collectModifications(IProgressMonitor pm, ModificationCollector collector) throws CoreException, OperationCanceledException {
      Collection<? extends MissingMemberFunction> missingMemFuns = collectMissingMemFuns(pm);
      ASTRewrite rewriter = createRewriter(collector, getAST(tu, pm));
      ClassPublicVisibilityInserter inserter = getPublicVisibilityInserter(rewriter);
      testDouble.addMissingMemFuns(missingMemFuns, inserter, cppStd);
   }

   @Override
   public String getDescription() {
      return I18N.FakeObjectRefactoringDesc;
   }

   @Override
   protected TestDouble createTestDouble(ICPPASTCompositeTypeSpecifier selectedClass) {
      return new FakeObject(selectedClass);
   }
}
