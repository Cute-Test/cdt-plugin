package ch.hsr.ifs.mockator.plugin.preprocessor;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterNameFunDecorator;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
abstract class PreprocessorFileCreator {
  private static final String FILE_NAME = "fileName";
  private static final String LINE_NUMBER = "lineNumber";
  protected static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  protected ICProject cProject;
  protected CRefactoringContext context;
  private final ModificationCollector collector;

  public PreprocessorFileCreator(ModificationCollector collector, ICProject cProject,
      CRefactoringContext context) {
    this.collector = collector;
    this.cProject = cProject;
    this.context = context;
  }

  public void createFile(IPath pathForNewFile, ICPPASTFunctionDeclarator funDecl,
      IProgressMonitor pm) throws CoreException {
    IASTTranslationUnit newAst = createNewTu(pathForNewFile, pm);
    ASTRewrite rewriter = collector.rewriterForTranslationUnit(newAst);
    addContentToTu(newAst, rewriter, funDecl, pm);
  }

  protected abstract void addContentToTu(IASTTranslationUnit newAst, ASTRewrite rewriter,
      ICPPASTFunctionDeclarator funDecl, IProgressMonitor pm) throws CoreException;

  protected ICPPASTDeclSpecifier getReturnValue(ICPPASTFunctionDeclarator funDecl) {
    ICPPASTDeclSpecifier newDeclSpec = AstUtil.getDeclSpec(funDecl).copy();
    AstUtil.removeExternalStorageIfSet(newDeclSpec);
    return newDeclSpec;
  }

  protected ICPPASTFunctionDeclarator createNewFunDecl(ICPPASTFunctionDeclarator funDecl) {
    IASTName newFunName = getNewFunName(funDecl);
    ICPPASTFunctionDeclarator newFunDecl = nodeFactory.newFunctionDeclarator(newFunName);
    addParamsExceptVoid(funDecl, newFunDecl);
    adjustParamNamesIfNecessary(newFunDecl);
    addFileNameParam(newFunDecl);
    addLineNumberParam(newFunDecl);
    return newFunDecl;
  }

  protected abstract IASTName getNewFunName(ICPPASTFunctionDeclarator funDecl);

  private static void addLineNumberParam(ICPPASTFunctionDeclarator newFunDecl) {
    newFunDecl.addParameterDeclaration(getLineNumberParam());
  }

  private static void addFileNameParam(ICPPASTFunctionDeclarator newFunDecl) {
    newFunDecl.addParameterDeclaration(getFileNameParam());
  }

  private static void addParamsExceptVoid(ICPPASTFunctionDeclarator funDecl,
      ICPPASTFunctionDeclarator newFunDecl) {
    for (ICPPASTParameterDeclaration param : funDecl.getParameters()) {
      if (!AstUtil.isVoid(param)) {
        newFunDecl.addParameterDeclaration(param.copy());
      }
    }
  }

  private static ICPPASTParameterDeclaration getLineNumberParam() {
    ICPPASTSimpleDeclSpecifier intType = nodeFactory.newSimpleDeclSpecifier();
    intType.setType(IBasicType.Kind.eInt);
    ICPPASTDeclarator lineNumberDecl =
        nodeFactory.newDeclarator(nodeFactory.newName(LINE_NUMBER.toCharArray()));
    ICPPASTParameterDeclaration lineNumberParam =
        nodeFactory.newParameterDeclaration(intType, lineNumberDecl);
    return lineNumberParam;
  }

  private static ICPPASTParameterDeclaration getFileNameParam() {
    ICPPASTDeclarator fileNameDecl =
        nodeFactory.newDeclarator(nodeFactory.newName(FILE_NAME.toCharArray()));
    ICPPASTSimpleDeclSpecifier charType = nodeFactory.newSimpleDeclSpecifier();
    charType.setType(IBasicType.Kind.eChar);
    charType.setConst(true);
    fileNameDecl.addPointerOperator(nodeFactory.newPointer());
    return nodeFactory.newParameterDeclaration(charType, fileNameDecl);
  }

  private static void adjustParamNamesIfNecessary(ICPPASTFunctionDeclarator newFunDecl) {
    ParameterNameFunDecorator funDecorator = new ParameterNameFunDecorator(newFunDecl);
    funDecorator.adjustParamNamesIfNecessary();
  }

  protected IASTTranslationUnit createNewTu(IPath pathForNewFile, IProgressMonitor pm)
      throws CoreException {
    TranslationUnitCreator creator = new TranslationUnitCreator(cProject.getProject(), context);
    return creator.createAndGetNewTu(pathForNewFile, pm);
  }
}
