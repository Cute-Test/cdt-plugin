package ch.hsr.ifs.mockator.plugin.preprocessor;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Set;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionDelegateCallCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.QualifiedNameCreator;

@SuppressWarnings("restriction")
class PreprocessorSourceFileCreator extends PreprocessorFileCreator {
  private final IPath newHeaderFilePath;

  public PreprocessorSourceFileCreator(IPath newHeaderFilePath, ModificationCollector collector,
      ICProject cProject, CRefactoringContext context) {
    super(collector, cProject, context);
    this.newHeaderFilePath = newHeaderFilePath;
  }

  @Override
  protected void addContentToTu(IASTTranslationUnit newAst, ASTRewrite rewriter,
      ICPPASTFunctionDeclarator funDecl, IProgressMonitor pm) throws CoreException {
    addHeaderInclude(newAst, rewriter);
    addUndef(newAst, rewriter, null, funDecl.getName().toString());
    insertFunctionDefinition(funDecl, newAst, rewriter);
  }

  private static void addUndef(IASTTranslationUnit tu, ASTRewrite rewriter,
      IASTNode insertionPoint, String funName) {
    new UndefMacroAdder(tu, rewriter, insertionPoint).addUndefMacro(funName);
  }

  private void insertFunctionDefinition(ICPPASTFunctionDeclarator funDecl,
      IASTTranslationUnit source, ASTRewrite rewriter) {
    ICPPASTDeclSpecifier newDeclSpec = getReturnValue(funDecl);
    ICPPASTFunctionDeclarator newFunDecl = createNewFunDecl(funDecl);
    IASTCompoundStatement funBody = createFunctionBody(funDecl, newFunDecl);
    ICPPASTFunctionDefinition funDef =
        nodeFactory.newFunctionDefinition(newDeclSpec, newFunDecl, funBody);
    rewriter.insertBefore(source, null, funDef, null);
  }

  private static IASTCompoundStatement createFunctionBody(ICPPASTFunctionDeclarator funDecl,
      ICPPASTFunctionDeclarator newFunDecl) {
    int numOfParams = newFunDecl.getParameters().length;
    Set<Integer> lastTwoParamsToBeIgnored = orderPreservingSet(numOfParams - 2, numOfParams - 1);
    FunctionDelegateCallCreator creator =
        new FunctionDelegateCallCreator(newFunDecl, lastTwoParamsToBeIgnored);
    IASTStatement delegateToOriginal =
        creator.createDelegate(funDecl.getName(), AstUtil.getDeclSpec(funDecl));
    return AstUtil.toCompoundStatement(delegateToOriginal);
  }

  private void addHeaderInclude(IASTTranslationUnit source, ASTRewrite rewriter) {
    AstIncludeNode astIncludeNode =
        new AstIncludeNode(FileUtil.getFilePart(newHeaderFilePath.toString()));
    rewriter.insertBefore(source, null, astIncludeNode, null);
  }

  @Override
  protected IASTName getNewFunName(ICPPASTFunctionDeclarator funDecl) {
    QualifiedNameCreator resolver = new QualifiedNameCreator(funDecl.getName());
    ICPPASTQualifiedName qualifiedName = resolver.createQualifiedName();
    String traceFunName = MockatorConstants.MOCKED_TRACE_PREFIX + funDecl.getName().toString();
    IASTName newName = nodeFactory.newName(traceFunName.toCharArray());
    qualifiedName.addName(newName);
    return qualifiedName;
  }
}
