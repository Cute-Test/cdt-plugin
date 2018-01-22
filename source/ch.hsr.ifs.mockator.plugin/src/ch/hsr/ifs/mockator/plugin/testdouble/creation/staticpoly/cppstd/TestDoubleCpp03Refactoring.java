package ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly.cppstd;

import java.util.Optional;

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

import ch.hsr.ifs.iltis.core.functional.OptHelper;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.wrappers.ModificationCollector;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.AbstractCreateTestDoubleRefactoring;
import ch.hsr.ifs.mockator.plugin.testdouble.movetons.TestDoubleInNsInserter;
import ch.hsr.ifs.mockator.plugin.testdouble.movetons.TestDoubleUsingNsHandler;


class TestDoubleCpp03Refactoring extends AbstractCreateTestDoubleRefactoring {

   public TestDoubleCpp03Refactoring(final ICElement cElement, final ITextSelection selection, final ICProject cProject) {
      super(cElement, selection, cProject);
   }

   @Override
   protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
            OperationCanceledException {
      final IASTTranslationUnit ast = getAST(tu(), pm);
      final ASTRewrite rewriter = createRewriter(collector, ast);
      final String newClassName = getSelectedName(ast).get().toString();
      final ICPPASTCompositeTypeSpecifier newTestDoubleClass = createNewTestDoubleClass(newClassName);

      getSelectedTestFunction(ast).ifPresent((testFun) -> {
         insertTestDoubleInNamespace(testFun, rewriter, newTestDoubleClass);
         insertUsingNamespaceStmt(testFun, rewriter, newTestDoubleClass);
      });
   }

   private static void insertUsingNamespaceStmt(final ICPPASTFunctionDefinition testFunction, final ASTRewrite rewriter,
            final ICPPASTCompositeTypeSpecifier testDouble) {
      final TestDoubleUsingNsHandler namespaceHandler = new TestDoubleUsingNsHandler(testDouble, rewriter);
      namespaceHandler.insertUsingNamespaceStmt(testFunction);
   }

   private static void insertTestDoubleInNamespace(final ICPPASTFunctionDefinition testFunction, final ASTRewrite rewriter,
            final ICPPASTCompositeTypeSpecifier testDouble) {
      final TestDoubleInNsInserter inserter = new TestDoubleInNsInserter(rewriter, CppStandard.Cpp03Std);
      inserter.insertTestDouble(nodeFactory.newSimpleDeclaration(testDouble), testDouble, testFunction);
   }

   private Optional<ICPPASTFunctionDefinition> getSelectedTestFunction(final IASTTranslationUnit ast) {
      return OptHelper.returnIfPresentElseEmpty(getSelectedName(ast), (funName) -> Optional.of((ICPPASTFunctionDefinition) ASTUtil.getAncestorOfType(
               funName, ICPPASTFunctionDefinition.class)));
   }
}
