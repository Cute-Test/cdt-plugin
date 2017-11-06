package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.AbstractCreateTestDoubleRefactoring;


@SuppressWarnings("restriction")
public class CreateTestDoubleSubTypeRefactoring extends AbstractCreateTestDoubleRefactoring {

   private static ICPPNodeFactory                 nodeFactory = CPPNodeFactory.getDefault();
   private final CreateTestDoubleSubTypeCodanArgs ca;

   public CreateTestDoubleSubTypeRefactoring(final ICElement cElement, final ITextSelection sel, final CreateTestDoubleSubTypeCodanArgs ca) {
      super(cElement, sel, null);
      this.ca = ca;
   }

   @Override
   protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
         OperationCanceledException {
      final IASTTranslationUnit ast = getAST(tu, pm);
      final ASTRewrite rewriter = createRewriter(collector, ast);
      addIncludeIfNecessary(ast, rewriter);
      replaceKindOfPassingArgIfNecessary(ast, rewriter);
      insertBeforeCurrentStmt(createNewTestDoubleClass(), ast, rewriter);
   }

   private void addIncludeIfNecessary(final IASTTranslationUnit ast, final ASTRewrite rewriter) {
      final String targetIncludePath = ca.getTargetIncludePath();
      final AstIncludeNode includeNode = new AstIncludeNode(targetIncludePath);
      includeNode.insertInTu(ast, rewriter);
   }

   private void replaceKindOfPassingArgIfNecessary(final IASTTranslationUnit ast, final ASTRewrite rewriter) {
      getSelectedName(ast).ifPresent((problemArgName) -> ca.getPassByStrategy().adaptArguments(problemArgName, ca.getNameOfMissingInstance(),
            rewriter));
   }

   private IASTDeclarationStatement createNewTestDoubleClass() {
      final String className = StringUtil.capitalize(ca.getNameOfMissingInstance());
      final ICPPASTCompositeTypeSpecifier newClass = createNewTestDoubleClass(className);
      addPublicInheritance(newClass);
      final IASTSimpleDeclaration newSimpleDeclaration = nodeFactory.newSimpleDeclaration(newClass);
      addClassInstance(newSimpleDeclaration);
      return nodeFactory.newDeclarationStatement(newSimpleDeclaration);
   }

   private void addClassInstance(final IASTSimpleDeclaration newSimpleDeclaration) {
      final IASTName classInstanceName = nodeFactory.newName(ca.getNameOfMissingInstance().toCharArray());
      newSimpleDeclaration.addDeclarator(nodeFactory.newDeclarator(classInstanceName));
   }

   private void addPublicInheritance(final ICPPASTCompositeTypeSpecifier newClass) {
      final boolean nonVirtual = false;
      final int noVisibility = 0; // we always create the new test doubles as 'struct' and use default
      // (public) visibility
      final IASTName parentClassName = nodeFactory.newName(ca.getParentClassName().toCharArray());
      final ICPPASTBaseSpecifier baseSpecifier = nodeFactory.newBaseSpecifier(parentClassName, noVisibility, nonVirtual);
      newClass.addBaseSpecifier(baseSpecifier);
   }
}
