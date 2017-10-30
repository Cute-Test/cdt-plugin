package ch.hsr.ifs.mockator.plugin.preprocessor;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTLiteralNode;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterNameFunDecorator;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.CppIncludeResolver;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.IncludeGuardCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NamespaceApplier;


@SuppressWarnings("restriction")
class PreprocessorHeaderFileCreator extends PreprocessorFileCreator {

   private static final String FILE_MACRO        = "__FILE__";
   private static final String LINE_NUMBER_MACRO = "__LINE__";

   public PreprocessorHeaderFileCreator(ModificationCollector collector, ICProject cProject, CRefactoringContext context) {
      super(collector, cProject, context);
   }

   @Override
   protected void addContentToTu(IASTTranslationUnit newAst, ASTRewrite rewriter, ICPPASTFunctionDeclarator funDecl, IProgressMonitor pm)
         throws CoreException {
      IncludeGuardCreator guardCreator = getIncludeGuardCreator(newAst);
      addIncludeGuardsStart(newAst, rewriter, guardCreator);
      insertFunDeclInclude(funDecl, newAst, rewriter);
      insertFunDecl(funDecl, newAst, rewriter);
      insertTraceMacro(funDecl, newAst, rewriter);
      addIncludeGuardsEnd(newAst, rewriter, guardCreator);
   }

   private IncludeGuardCreator getIncludeGuardCreator(IASTTranslationUnit newAst) {
      IFile file = FileUtil.toIFile(newAst.getFilePath());
      return new IncludeGuardCreator(file, cProject);
   }

   private static void addIncludeGuardsEnd(IASTTranslationUnit header, ASTRewrite r, IncludeGuardCreator guardCreator) {
      r.insertBefore(header, null, guardCreator.createEndIf(), null);
   }

   private static void addIncludeGuardsStart(IASTTranslationUnit header, ASTRewrite r, IncludeGuardCreator guardCreator) {
      r.insertBefore(header, null, guardCreator.createIfNDef(), null);
      r.insertBefore(header, null, guardCreator.createDefine(), null);
   }

   private void insertFunDecl(ICPPASTFunctionDeclarator funDecl, IASTTranslationUnit newTu, ASTRewrite r) {
      ICPPASTDeclSpecifier newDeclSpec = getReturnValue(funDecl);
      ICPPASTFunctionDeclarator newFunDecl = createNewFunDecl(funDecl);
      IASTSimpleDeclaration simpleDecl = nodeFactory.newSimpleDeclaration(newDeclSpec);
      simpleDecl.addDeclarator(newFunDecl);
      NamespaceApplier applier = new NamespaceApplier(funDecl);
      IASTNode packedInNs = applier.packInSameNamespaces(simpleDecl);
      r.insertBefore(newTu, null, packedInNs, null);
   }

   private static void insertTraceMacro(ICPPASTFunctionDeclarator funDecl, IASTTranslationUnit header, ASTRewrite r) {
      ICPPASTFunctionDeclarator newFunDecl = funDecl.copy();
      adjustParamNamesIfNecessary(newFunDecl);
      ASTLiteralNode defineNode = createTraceMacro(newFunDecl);
      r.insertBefore(header, null, defineNode, null);
   }

   private static void adjustParamNamesIfNecessary(ICPPASTFunctionDeclarator newFunDecl) {
      ParameterNameFunDecorator funDecorator = new ParameterNameFunDecorator(newFunDecl);
      funDecorator.adjustParamNamesIfNecessary();
   }

   private static String getParamString(ICPPASTFunctionDeclarator funDecl, boolean shouldQuote) {
      StringBuilder params = new StringBuilder();

      for (ICPPASTParameterDeclaration param : funDecl.getParameters()) {
         if (AstUtil.isVoid(param)) {
            continue;
         }

         if (!params.toString().isEmpty()) {
            params.append(MockatorConstants.COMMA);
         }

         if (shouldQuote) {
            params.append("(");
         }

         params.append(param.getDeclarator().getName().toString());

         if (shouldQuote) {
            params.append(")");
         }
      }

      return params.toString();
   }

   private static ASTLiteralNode createTraceMacro(ICPPASTFunctionDeclarator funDecl) {
      StringBuilder define = new StringBuilder();
      addMacroName(funDecl, define, getParamString(funDecl, false));
      define.append(MockatorConstants.SPACE);
      addMacroValue(funDecl, define, getParamString(funDecl, true));
      return new ASTLiteralNode(define.toString());
   }

   private static void addMacroValue(ICPPASTFunctionDeclarator funDecl, StringBuilder define, String paramString) {
      define.append(MockatorConstants.MOCKED_TRACE_PREFIX + funDecl.getName().toString());
      define.append(MockatorConstants.L_PARENTHESIS);
      define.append(paramString);

      if (!paramString.isEmpty()) {
         define.append(MockatorConstants.COMMA + MockatorConstants.SPACE);
      }

      define.append(FILE_MACRO);
      define.append(MockatorConstants.COMMA);
      define.append(MockatorConstants.SPACE);
      define.append(LINE_NUMBER_MACRO);
      define.append(MockatorConstants.R_PARENTHESIS);
   }

   private static String addMacroName(ICPPASTFunctionDeclarator funDecl, StringBuilder define, String paramString) {
      define.append(MockatorConstants.DEFINE_DIRECTIVE);
      define.append(MockatorConstants.SPACE);
      define.append(funDecl.getName().toString());
      define.append(MockatorConstants.L_PARENTHESIS);
      define.append(paramString);
      define.append(MockatorConstants.R_PARENTHESIS);
      return paramString;
   }

   private void insertFunDeclInclude(ICPPASTFunctionDeclarator funDecl, IASTTranslationUnit tu, ASTRewrite rewriter) throws CoreException {
      CppIncludeResolver resolver = new CppIncludeResolver(tu, cProject, context.getIndex());
      AstIncludeNode includeForFunDecl = resolver.resolveIncludeNode(funDecl.getTranslationUnit().getFilePath());
      rewriter.insertBefore(tu, null, includeForFunDecl, null);
   }

   @Override
   protected IASTName getNewFunName(ICPPASTFunctionDeclarator funDecl) {
      String traceFunName = MockatorConstants.MOCKED_TRACE_PREFIX + funDecl.getName().toString();
      return nodeFactory.newName(traceFunName.toCharArray());
   }
}
