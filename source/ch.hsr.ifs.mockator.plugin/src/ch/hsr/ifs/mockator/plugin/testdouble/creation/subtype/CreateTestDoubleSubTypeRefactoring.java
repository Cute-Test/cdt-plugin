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
  private static ICPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private final CreateTestDoubleSubTypeCodanArgs ca;

  public CreateTestDoubleSubTypeRefactoring(ICElement cElement, ITextSelection sel,
      CreateTestDoubleSubTypeCodanArgs ca) {
    super(cElement, sel, null);
    this.ca = ca;
  }

  @Override
  protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
      throws CoreException, OperationCanceledException {
    IASTTranslationUnit ast = getAST(tu, pm);
    ASTRewrite rewriter = createRewriter(collector, ast);
    addIncludeIfNecessary(ast, rewriter);
    replaceKindOfPassingArgIfNecessary(ast, rewriter);
    insertBeforeCurrentStmt(createNewTestDoubleClass(), ast, rewriter);
  }

  private void addIncludeIfNecessary(IASTTranslationUnit ast, ASTRewrite rewriter) {
    String targetIncludePath = ca.getTargetIncludePath();
    AstIncludeNode includeNode = new AstIncludeNode(targetIncludePath);
    includeNode.insertInTu(ast, rewriter);
  }

  private void replaceKindOfPassingArgIfNecessary(IASTTranslationUnit ast, ASTRewrite rewriter) {
    for (IASTName optProblemArgName : getSelectedName(ast)) {
      ca.getPassByStrategy().adaptArguments(optProblemArgName, ca.getNameOfMissingInstance(),
          rewriter);
    }
  }

  private IASTDeclarationStatement createNewTestDoubleClass() {
    String className = StringUtil.capitalize(ca.getNameOfMissingInstance());
    ICPPASTCompositeTypeSpecifier newClass = createNewTestDoubleClass(className);
    addPublicInheritance(newClass);
    IASTSimpleDeclaration newSimpleDeclaration = nodeFactory.newSimpleDeclaration(newClass);
    addClassInstance(newSimpleDeclaration);
    return nodeFactory.newDeclarationStatement(newSimpleDeclaration);
  }

  private void addClassInstance(IASTSimpleDeclaration newSimpleDeclaration) {
    IASTName classInstanceName = nodeFactory.newName(ca.getNameOfMissingInstance().toCharArray());
    newSimpleDeclaration.addDeclarator(nodeFactory.newDeclarator(classInstanceName));
  }

  private void addPublicInheritance(ICPPASTCompositeTypeSpecifier newClass) {
    boolean nonVirtual = false;
    int noVisibility = 0; // we always create the new test doubles as 'struct' and use default
                          // (public) visibility
    IASTName parentClassName = nodeFactory.newName(ca.getParentClassName().toCharArray());
    ICPPASTBaseSpecifier baseSpecifier =
        nodeFactory.newBaseSpecifier(parentClassName, noVisibility, nonVirtual);
    newClass.addBaseSpecifier(baseSpecifier);
  }
}
