package ch.hsr.ifs.mockator.plugin.mockobject.qf;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
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
import ch.hsr.ifs.mockator.plugin.mockobject.MockObject;
import ch.hsr.ifs.mockator.plugin.mockobject.support.MockSupportAdder;
import ch.hsr.ifs.mockator.plugin.mockobject.support.context.MockSupportContext;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.ClassPublicVisibilityInserter;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.TestDouble;
import ch.hsr.ifs.mockator.plugin.testdouble.qf.AbstractTestDoubleRefactoring;

@SuppressWarnings("restriction")
public class MockObjectRefactoring extends AbstractTestDoubleRefactoring {
  private final Collection<MissingMemberFunction> missingMemFuns;
  private final LinkedEditModeStrategy linkedEdit;

  public MockObjectRefactoring(CppStandard cppStd, ICElement cElement, ITextSelection selection,
      ICProject cProject, LinkedEditModeStrategy linkedEdit) {
    super(cppStd, cElement, selection, cProject);
    this.linkedEdit = linkedEdit;
    missingMemFuns = list();
  }

  @Override
  protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
      throws CoreException, OperationCanceledException {
    IASTTranslationUnit ast = getAST(tu, pm);
    ASTRewrite rewriter = createRewriter(collector, ast);
    missingMemFuns.addAll(collectMissingMemFuns(pm));
    ClassPublicVisibilityInserter inserter = getPublicVisibilityInserter(rewriter);
    addMockSupport(ast, rewriter, inserter, pm);
    testDouble.addMissingMemFuns(missingMemFuns, inserter, cppStd);
  }

  private boolean hasOnlyStaticMemFuns() {
    return testDouble.hasOnlyStaticFunctions(missingMemFuns);
  }

  private void addMockSupport(IASTTranslationUnit ast, ASTRewrite r,
      ClassPublicVisibilityInserter ci, IProgressMonitor pm) {
    MockSupportAdder adder = new MockSupportAdder(buildContext(r, ast, ci, pm));
    adder.addMockSupport();
  }

  private MockSupportContext buildContext(ASTRewrite rewriter, IASTTranslationUnit ast,
      ClassPublicVisibilityInserter inserter, IProgressMonitor pm) {
    return new MockSupportContext.ContextBuilder(project, refactoringContext,
        (MockObject) testDouble, rewriter, ast, cppStd, inserter, hasOnlyStaticMemFuns(), pm)
        .withLinkedEditStrategy(linkedEdit).withNewExpectations(missingMemFuns).build();
  }

  Collection<MissingMemberFunction> getMemberFunctionsForLinkedEdit() {
    return missingMemFuns;
  }

  @Override
  public String getDescription() {
    return I18N.MockObjectRefactoringDesc;
  }

  @Override
  protected TestDouble createTestDouble(ICPPASTCompositeTypeSpecifier selectedClass) {
    return new MockObject(selectedClass);
  }
}
