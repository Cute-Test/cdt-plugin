package ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly.cppstd;

import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.cpp.wrappers.ModificationCollector;

import ch.hsr.ifs.mockator.plugin.testdouble.creation.AbstractCreateTestDoubleRefactoring;


class TestDoubleCpp11Refactoring extends AbstractCreateTestDoubleRefactoring {

   public TestDoubleCpp11Refactoring(final ICElement cElement, final ITextSelection selection, final ICProject cProject) {
      super(cElement, selection, cProject);
   }

   @Override
   protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
         OperationCanceledException {
      final IASTTranslationUnit ast = getAST(tu(), pm);
      final ASTRewrite rewriter = createRewriter(collector, ast);
      insertBeforeCurrentStmt(createNewClassDefinition(ast), ast, rewriter);
   }

   private IASTDeclarationStatement createNewClassDefinition(final IASTTranslationUnit ast) {
      final String newClassName = getSelectedName(ast).get().toString();
      final ICPPASTCompositeTypeSpecifier testDouble = createNewTestDoubleClass(newClassName);
      return nodeFactory.newDeclarationStatement(nodeFactory.newSimpleDeclaration(testDouble));
   }
}
