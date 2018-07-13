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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.core.core.resources.FileUtil;
import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.util.constants.CommonCPPConstants;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoringContext;
import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterNameFunDecorator;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.CppIncludeResolver;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.IncludeGuardCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NamespaceApplier;


@SuppressWarnings("restriction")
class PreprocessorHeaderFileCreator extends PreprocessorFileCreator {

   private static final String FILE_MACRO        = "__FILE__";
   private static final String LINE_NUMBER_MACRO = "__LINE__";

   public PreprocessorHeaderFileCreator(final ModificationCollector collector, final ICProject cProject, final CRefactoringContext context) {
      super(collector, cProject, context);
   }

   @Override
   protected void addContentToTu(final IASTTranslationUnit newAst, final ASTRewrite rewriter, final ICPPASTFunctionDeclarator funDecl,
         final IProgressMonitor pm) throws CoreException {
      final IncludeGuardCreator guardCreator = getIncludeGuardCreator(newAst);
      addIncludeGuardsStart(newAst, rewriter, guardCreator);
      insertFunDeclInclude(funDecl, newAst, rewriter);
      insertFunDecl(funDecl, newAst, rewriter);
      insertTraceMacro(funDecl, newAst, rewriter);
      addIncludeGuardsEnd(newAst, rewriter, guardCreator);
   }

   private IncludeGuardCreator getIncludeGuardCreator(final IASTTranslationUnit newAst) {
      final IFile file = FileUtil.toIFile(newAst.getFilePath());
      return new IncludeGuardCreator(file, cProject);
   }

   private static void addIncludeGuardsEnd(final IASTTranslationUnit header, final ASTRewrite r, final IncludeGuardCreator guardCreator) {
      r.insertBefore(header, null, guardCreator.createEndIf(), null);
   }

   private static void addIncludeGuardsStart(final IASTTranslationUnit header, final ASTRewrite r, final IncludeGuardCreator guardCreator) {
      r.insertBefore(header, null, guardCreator.createIfNDef(), null);
      r.insertBefore(header, null, guardCreator.createDefine(), null);
   }

   private void insertFunDecl(final ICPPASTFunctionDeclarator funDecl, final IASTTranslationUnit newTu, final ASTRewrite r) {
      final ICPPASTDeclSpecifier newDeclSpec = getReturnValue(funDecl);
      final ICPPASTFunctionDeclarator newFunDecl = createNewFunDecl(funDecl);
      final IASTSimpleDeclaration simpleDecl = nodeFactory.newSimpleDeclaration(newDeclSpec);
      simpleDecl.addDeclarator(newFunDecl);
      final NamespaceApplier applier = new NamespaceApplier(funDecl);
      final IASTNode packedInNs = applier.packInSameNamespaces(simpleDecl);
      r.insertBefore(newTu, null, packedInNs, null);
   }

   private static void insertTraceMacro(final ICPPASTFunctionDeclarator funDecl, final IASTTranslationUnit header, final ASTRewrite r) {
      final ICPPASTFunctionDeclarator newFunDecl = funDecl.copy();
      adjustParamNamesIfNecessary(newFunDecl);
      final ASTLiteralNode defineNode = createTraceMacro(newFunDecl);
      r.insertBefore(header, null, defineNode, null);
   }

   private static void adjustParamNamesIfNecessary(final ICPPASTFunctionDeclarator newFunDecl) {
      final ParameterNameFunDecorator funDecorator = new ParameterNameFunDecorator(newFunDecl);
      funDecorator.adjustParamNamesIfNecessary();
   }

   private static String getParamString(final ICPPASTFunctionDeclarator funDecl, final boolean shouldQuote) {
      final StringBuilder params = new StringBuilder();

      for (final ICPPASTParameterDeclaration param : funDecl.getParameters()) {
         if (ASTUtil.isVoid(param)) {
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

   private static ASTLiteralNode createTraceMacro(final ICPPASTFunctionDeclarator funDecl) {
      final StringBuilder define = new StringBuilder();
      addMacroName(funDecl, define, getParamString(funDecl, false));
      define.append(MockatorConstants.SPACE);
      addMacroValue(funDecl, define, getParamString(funDecl, true));
      return new ASTLiteralNode(define.toString());
   }

   private static void addMacroValue(final ICPPASTFunctionDeclarator funDecl, final StringBuilder define, final String paramString) {
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

   private static String addMacroName(final ICPPASTFunctionDeclarator funDecl, final StringBuilder define, final String paramString) {
      define.append(CommonCPPConstants.DEFINE_DIRECTIVE);
      define.append(MockatorConstants.SPACE);
      define.append(funDecl.getName().toString());
      define.append(MockatorConstants.L_PARENTHESIS);
      define.append(paramString);
      define.append(MockatorConstants.R_PARENTHESIS);
      return paramString;
   }

   private void insertFunDeclInclude(final ICPPASTFunctionDeclarator funDecl, final IASTTranslationUnit tu, final ASTRewrite rewriter)
         throws CoreException {
      final CppIncludeResolver resolver = new CppIncludeResolver(tu, cProject, context.getIndex());
      final AstIncludeNode includeForFunDecl = resolver.resolveIncludeNode(funDecl.getTranslationUnit().getFilePath());
      rewriter.insertBefore(tu, null, includeForFunDecl, null);
   }

   @Override
   protected IASTName getNewFunName(final ICPPASTFunctionDeclarator funDecl) {
      final String traceFunName = MockatorConstants.MOCKED_TRACE_PREFIX + funDecl.getName().toString();
      return nodeFactory.newName(traceFunName.toCharArray());
   }
}
