package ch.hsr.ifs.cute.mockator.preprocessor;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoringContext;
import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

import ch.hsr.ifs.cute.mockator.refsupport.functions.params.ParameterNameFunDecorator;
import ch.hsr.ifs.cute.mockator.refsupport.tu.TranslationUnitCreator;


abstract class PreprocessorFileCreator {

   private static final String            FILE_NAME   = "fileName";
   private static final String            LINE_NUMBER = "lineNumber";
   protected static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   protected ICProject                    cProject;
   protected CRefactoringContext          context;
   private final ModificationCollector    collector;

   public PreprocessorFileCreator(final ModificationCollector collector, final ICProject cProject, final CRefactoringContext context) {
      this.collector = collector;
      this.cProject = cProject;
      this.context = context;
   }

   public void createFile(final IPath pathForNewFile, final ICPPASTFunctionDeclarator funDecl, final IProgressMonitor pm) throws CoreException {
      final IASTTranslationUnit newAst = createNewTu(pathForNewFile, pm);
      final ASTRewrite rewriter = collector.rewriterForTranslationUnit(newAst);
      addContentToTu(newAst, rewriter, funDecl, pm);
   }

   protected abstract void addContentToTu(IASTTranslationUnit newAst, ASTRewrite rewriter, ICPPASTFunctionDeclarator funDecl, IProgressMonitor pm)
         throws CoreException;

   protected ICPPASTDeclSpecifier getReturnValue(final ICPPASTFunctionDeclarator funDecl) {
      final ICPPASTDeclSpecifier newDeclSpec = ASTUtil.getDeclSpec(funDecl).copy();
      ASTUtil.removeExternalStorageIfSet(newDeclSpec);
      return newDeclSpec;
   }

   protected ICPPASTFunctionDeclarator createNewFunDecl(final ICPPASTFunctionDeclarator funDecl) {
      final IASTName newFunName = getNewFunName(funDecl);
      final ICPPASTFunctionDeclarator newFunDecl = nodeFactory.newFunctionDeclarator(newFunName);
      addParamsExceptVoid(funDecl, newFunDecl);
      adjustParamNamesIfNecessary(newFunDecl);
      addFileNameParam(newFunDecl);
      addLineNumberParam(newFunDecl);
      return newFunDecl;
   }

   protected abstract IASTName getNewFunName(ICPPASTFunctionDeclarator funDecl);

   private static void addLineNumberParam(final ICPPASTFunctionDeclarator newFunDecl) {
      newFunDecl.addParameterDeclaration(getLineNumberParam());
   }

   private static void addFileNameParam(final ICPPASTFunctionDeclarator newFunDecl) {
      newFunDecl.addParameterDeclaration(getFileNameParam());
   }

   private static void addParamsExceptVoid(final ICPPASTFunctionDeclarator funDecl, final ICPPASTFunctionDeclarator newFunDecl) {
      for (final ICPPASTParameterDeclaration param : funDecl.getParameters()) {
         if (!ASTUtil.isVoid(param)) {
            newFunDecl.addParameterDeclaration(param.copy());
         }
      }
   }

   private static ICPPASTParameterDeclaration getLineNumberParam() {
      final ICPPASTSimpleDeclSpecifier intType = nodeFactory.newSimpleDeclSpecifier();
      intType.setType(IBasicType.Kind.eInt);
      final ICPPASTDeclarator lineNumberDecl = nodeFactory.newDeclarator(nodeFactory.newName(LINE_NUMBER.toCharArray()));
      final ICPPASTParameterDeclaration lineNumberParam = nodeFactory.newParameterDeclaration(intType, lineNumberDecl);
      return lineNumberParam;
   }

   private static ICPPASTParameterDeclaration getFileNameParam() {
      final ICPPASTDeclarator fileNameDecl = nodeFactory.newDeclarator(nodeFactory.newName(FILE_NAME.toCharArray()));
      final ICPPASTSimpleDeclSpecifier charType = nodeFactory.newSimpleDeclSpecifier();
      charType.setType(IBasicType.Kind.eChar);
      charType.setConst(true);
      fileNameDecl.addPointerOperator(nodeFactory.newPointer());
      return nodeFactory.newParameterDeclaration(charType, fileNameDecl);
   }

   private static void adjustParamNamesIfNecessary(final ICPPASTFunctionDeclarator newFunDecl) {
      final ParameterNameFunDecorator funDecorator = new ParameterNameFunDecorator(newFunDecl);
      funDecorator.adjustParamNamesIfNecessary();
   }

   protected IASTTranslationUnit createNewTu(final IPath pathForNewFile, final IProgressMonitor pm) throws CoreException {
      final TranslationUnitCreator creator = new TranslationUnitCreator(cProject.getProject(), context);
      return creator.createAndGetNewTu(pathForNewFile, pm);
   }
}
