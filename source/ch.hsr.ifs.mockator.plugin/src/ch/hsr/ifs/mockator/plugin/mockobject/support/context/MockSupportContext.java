package ch.hsr.ifs.mockator.plugin.mockobject.support.context;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.core.functional.functions.Builder;
import ch.hsr.ifs.iltis.cpp.wrappers.CRefactoringContext;

import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.mockobject.MockObject;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.ClassPublicVisibilityInserter;


public final class MockSupportContext {

   private final ICProject                              cProject;
   private final CRefactoringContext                    context;
   private final IASTTranslationUnit                    ast;
   private final CppStandard                            cppStd;
   private final boolean                                hasOnlyStaticMemFuns;
   private final ClassPublicVisibilityInserter          inserter;
   private final ASTRewrite                             rewriter;
   private final MockObject                             mockObject;
   private final IProgressMonitor                       pm;
   private final LinkedEditModeStrategy                 linkedEditStrategy;
   private final Collection<? extends TestDoubleMemFun> newForExpectations;

   public static class ContextBuilder implements Builder<MockSupportContext> {

      private final ICProject                        cProject;
      private final CRefactoringContext              context;
      private final IASTTranslationUnit              ast;
      private final CppStandard                      cppStandard;
      private final boolean                          hasOnlyStaticMemFuns;
      private final ClassPublicVisibilityInserter    inserter;
      private final ASTRewrite                       rewriter;
      private final MockObject                       mockObject;
      private final IProgressMonitor                 pm;
      private LinkedEditModeStrategy                 linkedEditStrategy;
      private Collection<? extends TestDoubleMemFun> newForExpectations;

      public ContextBuilder(final ICProject cProject, final CRefactoringContext context, final MockObject mockObject, final ASTRewrite rewriter,
            final IASTTranslationUnit ast, final CppStandard cppStandard, final ClassPublicVisibilityInserter inserter,
            final boolean hasOnlyStaticMemFuns, final IProgressMonitor pm) {
         this.cProject = cProject;
         this.context = context;
         this.mockObject = mockObject;
         this.rewriter = rewriter;
         this.ast = ast;
         this.cppStandard = cppStandard;
         this.inserter = inserter;
         this.hasOnlyStaticMemFuns = hasOnlyStaticMemFuns;
         this.pm = pm;
      }

      public ContextBuilder withNewExpectations(final Collection<? extends TestDoubleMemFun> newForExpectations) {
         this.newForExpectations = newForExpectations;
         return this;
      }

      public ContextBuilder withLinkedEditStrategy(final LinkedEditModeStrategy linkedEditStrategy) {
         this.linkedEditStrategy = linkedEditStrategy;
         return this;
      }

      @Override
      public MockSupportContext build() {
         return new MockSupportContext(this);
      }
   }

   private MockSupportContext(final ContextBuilder builder) {
      cProject = builder.cProject;
      context = builder.context;
      mockObject = builder.mockObject;
      rewriter = builder.rewriter;
      ast = builder.ast;
      cppStd = builder.cppStandard;
      inserter = builder.inserter;
      hasOnlyStaticMemFuns = builder.hasOnlyStaticMemFuns;
      pm = builder.pm;

      if (builder.newForExpectations == null) {
         newForExpectations = list();
      } else {
         newForExpectations = builder.newForExpectations;
      }

      if (builder.linkedEditStrategy == null) {
         linkedEditStrategy = LinkedEditModeStrategy.fromProjectSettings(cProject.getProject());
      } else {
         linkedEditStrategy = builder.linkedEditStrategy;
      }
   }

   public ICProject getProject() {
      return cProject;
   }

   public CRefactoringContext getCRefContext() {
      return context;
   }

   public IASTTranslationUnit getAst() {
      return ast;
   }

   public CppStandard getCppStandard() {
      return cppStd;
   }

   public boolean hasOnlyStaticMemFuns() {
      return hasOnlyStaticMemFuns;
   }

   public ClassPublicVisibilityInserter getInserter() {
      return inserter;
   }

   public ASTRewrite getRewriter() {
      return rewriter;
   }

   public MockObject getMockObject() {
      return mockObject;
   }

   public IProgressMonitor getProgressMonitor() {
      return pm;
   }

   public LinkedEditModeStrategy getLinkedEditStrategy() {
      return linkedEditStrategy;
   }

   public Collection<? extends TestDoubleMemFun> getNewForExpectations() {
      return newForExpectations;
   }

   public String getNameForExpectationsVector() {
      return getMockObject().getNameForExpectationVector();
   }

   public String getFqNameForAllCallsVector() {
      return getMockObject().getFqNameOfAllCallsVector();
   }

   public Collection<ICPPASTFunctionDefinition> getReferencingFunctions() {
      return mockObject.getReferencingTestFunctions(context, cProject, pm);
   }
}
