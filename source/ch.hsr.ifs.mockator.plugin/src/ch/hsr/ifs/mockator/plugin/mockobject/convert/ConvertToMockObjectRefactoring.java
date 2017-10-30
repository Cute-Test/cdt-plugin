package ch.hsr.ifs.mockator.plugin.mockobject.convert;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.incompleteclass.DefaultCtorProvider;
import ch.hsr.ifs.mockator.plugin.incompleteclass.MissingMemberFunction;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.mockobject.MockObject;
import ch.hsr.ifs.mockator.plugin.mockobject.MockObjectMemFunImplStrategy;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.MockCallRegistrationFinder;
import ch.hsr.ifs.mockator.plugin.mockobject.support.MockSupportAdder;
import ch.hsr.ifs.mockator.plugin.mockobject.support.context.MockSupportContext;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.ClassPublicVisibilityInserter;
import ch.hsr.ifs.mockator.plugin.testdouble.MemFunMockSupportAdder;
import ch.hsr.ifs.mockator.plugin.testdouble.entities.ExistingTestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.testdouble.support.TestDoubleKindAnalyzer;
import ch.hsr.ifs.mockator.plugin.testdouble.support.TestDoubleKindAnalyzer.TestDoubleKind;


@SuppressWarnings("restriction")
public class ConvertToMockObjectRefactoring extends MockatorRefactoring {

   private final CppStandard            cppStd;
   private final LinkedEditModeStrategy linkedEditStrategy;
   private MockObject                   newMockObject;

   public ConvertToMockObjectRefactoring(final CppStandard cppStd, final ICElement element, final ITextSelection selection, final ICProject cproject,
         final LinkedEditModeStrategy linkedEditStrategy) {
      super(element, selection, cproject);
      this.cppStd = cppStd;
      this.linkedEditStrategy = linkedEditStrategy;
   }

   @Override
   public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
      final RefactoringStatus status = super.checkInitialConditions(pm);
      final Optional<ICPPASTCompositeTypeSpecifier> klass = getClassInSelection(getAST(tu, pm));

      if (!klass.isPresent()) {
         status.addFatalError("Class could not be found in selection");
      } else if (!isFakeObject(klass.get())) {
         status.addFatalError("Chosen test double must be a fake object");
      } else {
         newMockObject = new MockObject(klass.get());
      }

      return status;
   }

   private static boolean isFakeObject(final ICPPASTCompositeTypeSpecifier klass) {
      return new TestDoubleKindAnalyzer(klass).getKindOfTestDouble() == TestDoubleKind.FakeObject;
   }

   @Override
   protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException, OperationCanceledException {
      final IASTTranslationUnit ast = getAST(tu, pm);
      final ASTRewrite rewriter = createRewriter(collector, ast);
      addMockSupportToFakeObject(ast, rewriter, pm);
   }

   private void addMockSupportToFakeObject(final IASTTranslationUnit ast, final ASTRewrite re, final IProgressMonitor pm) {
      final Optional<? extends MissingMemberFunction> defaultCtor = createDefaultCtorIfNecessary();
      final List<TestDoubleMemFun> expectations = getExpectations(defaultCtor);
      final MockSupportAdder mockSupport = new MockSupportAdder(buildContext(re, ast, expectations, pm));
      mockSupport.addMockSupport();
      insertDefaultCtorIfNecessary(re, defaultCtor);
      addCallVectorRegistrations(re);
   }

   private MockSupportContext buildContext(final ASTRewrite rewriter, final IASTTranslationUnit ast, final Collection<TestDoubleMemFun> withNewExpectations,
         final IProgressMonitor pm) {
      return new MockSupportContext.ContextBuilder(project, refactoringContext, newMockObject, rewriter, ast, cppStd, getPublicVisibilityInserter(
            rewriter), hasOnlyStaticMemFuns(), pm).withLinkedEditStrategy(linkedEditStrategy).withNewExpectations(withNewExpectations).build();
   }

   private boolean hasOnlyStaticMemFuns() {
      final List<MissingMemberFunction> noNewMemFuns = list();
      return newMockObject.hasOnlyStaticFunctions(noNewMemFuns);
   }

   private ClassPublicVisibilityInserter getPublicVisibilityInserter(final ASTRewrite rewriter) {
      return new ClassPublicVisibilityInserter(newMockObject.getKlass(), rewriter);
   }

   private List<TestDoubleMemFun> getExpectations(final Optional<? extends MissingMemberFunction> defaultCtor) {
      final List<TestDoubleMemFun> publicMemFuns = list();

      defaultCtor.ifPresent((defCtor) -> publicMemFuns.add(defCtor));

      publicMemFuns.addAll(newMockObject.getPublicMemFuns());
      return publicMemFuns;
   }

   private Optional<? extends MissingMemberFunction> createDefaultCtorIfNecessary() {
      final Collection<MissingMemberFunction> noNewFunctions = list();
      return getDefaultCtorProvider().createMissingDefaultCtor(noNewFunctions);
   }

   private void insertDefaultCtorIfNecessary(final ASTRewrite rewriter, final Optional<? extends MissingMemberFunction> defaultCtor) {
      defaultCtor.ifPresent((defCtor) -> {
         final MockObjectMemFunImplStrategy strategy = new MockObjectMemFunImplStrategy(cppStd, newMockObject);
         final ICPPASTFunctionDefinition fun = defCtor.createFunctionDefinition(strategy, cppStd);
         new ClassPublicVisibilityInserter(newMockObject.getKlass(), rewriter).insert(fun);
      });
   }

   private DefaultCtorProvider getDefaultCtorProvider() {
      return new MockObject(newMockObject.getKlass()).getDefaultCtorProvider(cppStd);
   }

   private void addCallVectorRegistrations(final ASTRewrite rewriter) {
      final MockCallRegistrationFinder callRegFinder = new MockCallRegistrationFinder(cppStd);

      for (final ExistingTestDoubleMemFun memFun : newMockObject.getPublicMemFuns()) {
         memFun.addMockSupport(getMockSupportFor(memFun, rewriter), callRegFinder);
      }
   }

   private MemFunMockSupportAdder getMockSupportFor(final ExistingTestDoubleMemFun fun, final ASTRewrite rewriter) {
      return newMockObject.getMockSupport(rewriter, cppStd, fun);
   }

   MockObject getNewMockObject() {
      return newMockObject;
   }

   @Override
   public String getDescription() {
      return I18N.ConvertToMockObjectRefactoringDesc;
   }
}
