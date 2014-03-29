package ch.hsr.ifs.mockator.plugin.mockobject.togglefun;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.mockobject.MockObject;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.reconcile.ExpectationsHandler;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.MockCallRegistrationFinder;
import ch.hsr.ifs.mockator.plugin.mockobject.support.MockSupportAdder;
import ch.hsr.ifs.mockator.plugin.mockobject.support.context.MockSupportContext;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.ClassPublicVisibilityInserter;
import ch.hsr.ifs.mockator.plugin.testdouble.MemFunMockSupportAdder;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.ExistingTestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.testdouble.support.MemFunSignature;

@SuppressWarnings("restriction")
public class ToggleTracingFunCallRefactoring extends MockatorRefactoring {
  private final CppStandard cppStd;
  private final LinkedEditModeStrategy linkedEdit;
  private ExistingTestDoubleMemFun testDoubleMemFun;
  private MockObject mockObject;

  public ToggleTracingFunCallRefactoring(CppStandard cppStd, ICElement element,
      ITextSelection selection, ICProject cProject, LinkedEditModeStrategy linkedEdit) {
    super(element, selection, cProject);
    this.cppStd = cppStd;
    this.linkedEdit = linkedEdit;
  }

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
    RefactoringStatus status = super.checkInitialConditions(pm);
    Maybe<IASTName> selectedName = getSelectedName(getAST(tu, pm));

    if (selectedName.isNone()) {
      status.addFatalError("Not a valid name selected");
      return status;
    }

    ICPPASTFunctionDefinition function =
        AstUtil.getAncestorOfType(selectedName.get(), ICPPASTFunctionDefinition.class);
    assureIsMemberFunction(status, function);
    return status;
  }

  private void assureIsMemberFunction(RefactoringStatus status, ICPPASTFunctionDefinition function) {
    if (function == null) {
      status.addFatalError("Not a valid function selected");
    }

    testDoubleMemFun = new ExistingTestDoubleMemFun(function);
    ICPPASTCompositeTypeSpecifier klass = testDoubleMemFun.getContainingClass();

    if (klass == null) {
      status.addFatalError("No test double member function selected");
    } else {
      mockObject = new MockObject(klass);
    }
  }

  @Override
  protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
      throws CoreException, OperationCanceledException {
    IASTTranslationUnit ast = getAST(tu, pm);
    ASTRewrite rewriter = createRewriter(collector, ast);
    toggleTraceSupport(buildContext(rewriter, ast, pm));
  }

  private void toggleTraceSupport(MockSupportContext context) {
    Maybe<? extends MemFunSignature> tracedCall = getRegisteredCallInSelectedFun();

    if (tracedCall.isNone()) {
      addMockSupport(context);
    } else {
      removeTraceSupport(context.getRewriter(), (ExistingMemFunCallRegistration) tracedCall.get());
      removeExpectation(context);
    }
  }

  private void addMockSupport(MockSupportContext context) {
    new MockSupportAdder(context).addMockSupport();
    addCallVectorRegistrations(context.getRewriter());
  }

  private void removeExpectation(MockSupportContext context) {
    ExpectationsHandler handler = new ExpectationsHandler(context);
    handler.removeExpectation(new ExistingMemFunCallRegistration(testDoubleMemFun),
        context.getProgressMonitor());
  }

  private Maybe<? extends MemFunSignature> getRegisteredCallInSelectedFun() {
    return testDoubleMemFun.getRegisteredCall(new MockCallRegistrationFinder(cppStd));
  }

  private MockSupportContext buildContext(ASTRewrite rewriter, IASTTranslationUnit ast,
      IProgressMonitor pm) {
    return new MockSupportContext.ContextBuilder(project, refactoringContext, mockObject, rewriter,
        ast, cppStd, getPublicVisibilityInserter(rewriter), hasMockObjectOnlyStaticMemFuns(), pm)
        .withLinkedEditStrategy(linkedEdit).withNewExpectations(list(testDoubleMemFun)).build();
  }

  private boolean hasMockObjectOnlyStaticMemFuns() {
    return mockObject.hasOnlyStaticFunctions(CollectionHelper.<MissingMemberFunction>list());
  }

  private ClassPublicVisibilityInserter getPublicVisibilityInserter(ASTRewrite rewriter) {
    return new ClassPublicVisibilityInserter(mockObject.getKlass(), rewriter);
  }

  private void addCallVectorRegistrations(ASTRewrite rewriter) {
    testDoubleMemFun.addMockSupport(getMockSupportAdder(rewriter), new MockCallRegistrationFinder(
        cppStd));
  }

  private MemFunMockSupportAdder getMockSupportAdder(ASTRewrite rewriter) {
    return mockObject.getMockSupport(rewriter, cppStd, testDoubleMemFun);
  }

  private static void removeTraceSupport(ASTRewrite rewriter,
      ExistingMemFunCallRegistration existingMemFunCallRegistration) {
    IASTStatement registrationStmt = existingMemFunCallRegistration.getRegistrationStmt();
    rewriter.remove(registrationStmt, null);
  }

  @Override
  public String getDescription() {
    return I18N.ToggleTracingFunctionRefactoringDesc;
  }

  ExistingTestDoubleMemFun getToggledFunction() {
    return testDoubleMemFun;
  }
}
