package ch.hsr.ifs.mockator.plugin.mockobject.togglefun;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.ArrayList;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.wrappers.ModificationCollector;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.mockobject.MockObject;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.reconcile.ExpectationsHandler;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.MockCallRegistrationFinder;
import ch.hsr.ifs.mockator.plugin.mockobject.support.MockSupportAdder;
import ch.hsr.ifs.mockator.plugin.mockobject.support.context.MockSupportContext;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.ClassPublicVisibilityInserter;
import ch.hsr.ifs.mockator.plugin.testdouble.MemFunMockSupportAdder;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.ExistingTestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.testdouble.support.MemFunSignature;


public class ToggleTracingFunCallRefactoring extends MockatorRefactoring {

   private final CppStandard            cppStd;
   private final LinkedEditModeStrategy linkedEdit;
   private ExistingTestDoubleMemFun     testDoubleMemFun;
   private MockObject                   mockObject;

   public ToggleTracingFunCallRefactoring(final CppStandard cppStd, final ICElement element, final ITextSelection selection, final ICProject cProject,
                                          final LinkedEditModeStrategy linkedEdit) {
      super(element, selection, cProject);
      this.cppStd = cppStd;
      this.linkedEdit = linkedEdit;
   }

   @Override
   public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) throws CoreException {
      final RefactoringStatus status = super.checkInitialConditions(pm);
      final Optional<IASTName> selectedName = getSelectedName(getAST(tu(), pm));

      if (!selectedName.isPresent()) {
         status.addFatalError("Not a valid name selected");
         return status;
      }

      final ICPPASTFunctionDefinition function = ASTUtil.getAncestorOfType(selectedName.get(), ICPPASTFunctionDefinition.class);
      assureIsMemberFunction(status, function);
      return status;
   }

   private void assureIsMemberFunction(final RefactoringStatus status, final ICPPASTFunctionDefinition function) {
      if (function == null) {
         status.addFatalError("Not a valid function selected");
      }

      testDoubleMemFun = new ExistingTestDoubleMemFun(function);
      final ICPPASTCompositeTypeSpecifier clazz = testDoubleMemFun.getContainingClass();

      if (clazz == null) {
         status.addFatalError("No test double member function selected");
      } else {
         mockObject = new MockObject(clazz);
      }
   }

   @Override
   protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
            OperationCanceledException {
      final IASTTranslationUnit ast = getAST(tu(), pm);
      final ASTRewrite rewriter = createRewriter(collector, ast);
      toggleTraceSupport(buildContext(rewriter, ast, pm));
   }

   private void toggleTraceSupport(final MockSupportContext context) {
      final Optional<? extends MemFunSignature> tracedCall = getRegisteredCallInSelectedFun();

      if (!tracedCall.isPresent()) {
         addMockSupport(context);
      } else {
         removeTraceSupport(context.getRewriter(), (ExistingMemFunCallRegistration) tracedCall.get());
         removeExpectation(context);
      }
   }

   private void addMockSupport(final MockSupportContext context) {
      new MockSupportAdder(context).addMockSupport();
      addCallVectorRegistrations(context.getRewriter());
   }

   private void removeExpectation(final MockSupportContext context) {
      final ExpectationsHandler handler = new ExpectationsHandler(context);
      handler.removeExpectation(new ExistingMemFunCallRegistration(testDoubleMemFun), context.getProgressMonitor());
   }

   private Optional<? extends MemFunSignature> getRegisteredCallInSelectedFun() {
      return testDoubleMemFun.getRegisteredCall(new MockCallRegistrationFinder(cppStd));
   }

   private MockSupportContext buildContext(final ASTRewrite rewriter, final IASTTranslationUnit ast, final IProgressMonitor pm) {
      return new MockSupportContext.ContextBuilder(getProject(), refactoringContext(), mockObject, rewriter, ast, cppStd, getPublicVisibilityInserter(
               rewriter), hasMockObjectOnlyStaticMemFuns(), pm).withLinkedEditStrategy(linkedEdit).withNewExpectations(list(testDoubleMemFun))
                        .build();
   }

   private boolean hasMockObjectOnlyStaticMemFuns() {
      return mockObject.hasOnlyStaticFunctions(new ArrayList<>());
   }

   private ClassPublicVisibilityInserter getPublicVisibilityInserter(final ASTRewrite rewriter) {
      return new ClassPublicVisibilityInserter(mockObject.getKlass(), rewriter);
   }

   private void addCallVectorRegistrations(final ASTRewrite rewriter) {
      testDoubleMemFun.addMockSupport(getMockSupportAdder(rewriter), new MockCallRegistrationFinder(cppStd));
   }

   private MemFunMockSupportAdder getMockSupportAdder(final ASTRewrite rewriter) {
      return mockObject.getMockSupport(rewriter, cppStd, testDoubleMemFun);
   }

   private static void removeTraceSupport(final ASTRewrite rewriter, final ExistingMemFunCallRegistration existingMemFunCallRegistration) {
      final IASTStatement registrationStmt = existingMemFunCallRegistration.getRegistrationStmt();
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
