package ch.hsr.ifs.mockator.plugin.fakeobject;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.cpp.wrappers.ModificationCollector;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.ClassPublicVisibilityInserter;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.TestDouble;
import ch.hsr.ifs.mockator.plugin.testdouble.qf.AbstractTestDoubleRefactoring;


public class FakeObjectRefactoring extends AbstractTestDoubleRefactoring {

   public FakeObjectRefactoring(final CppStandard cppStd, final ICElement cElement, final ITextSelection selection, final ICProject cProject) {
      super(cppStd, cElement, selection, cProject);
   }

   @Override
   protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
            OperationCanceledException {
      final Collection<? extends MissingMemberFunction> missingMemFuns = collectMissingMemFuns(pm);
      final ASTRewrite rewriter = createRewriter(collector, getAST(tu(), pm));
      final ClassPublicVisibilityInserter inserter = getPublicVisibilityInserter(rewriter);
      testDouble.addMissingMemFuns(missingMemFuns, inserter, cppStd);
   }

   @Override
   public String getDescription() {
      return I18N.FakeObjectRefactoringDesc;
   }

   @Override
   protected TestDouble createTestDouble(final ICPPASTCompositeTypeSpecifier selectedClass) {
      return new FakeObject(selectedClass);
   }
}
