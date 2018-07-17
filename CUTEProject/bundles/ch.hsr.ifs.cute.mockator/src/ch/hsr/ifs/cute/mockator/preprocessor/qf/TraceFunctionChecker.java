package ch.hsr.ifs.cute.mockator.preprocessor.qf;

import static ch.hsr.ifs.cute.mockator.MockatorConstants.MOCKED_TRACE_PREFIX;

import java.util.Optional;

import org.eclipse.cdt.codan.core.cxx.model.AbstractAstFunctionChecker;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ch.hsr.ifs.iltis.core.core.resources.FileUtil;

import ch.hsr.ifs.cute.mockator.base.misc.IdHelper.ProblemId;
import ch.hsr.ifs.cute.mockator.project.cdt.options.IncludeFileHandler;
import ch.hsr.ifs.cute.mockator.refsupport.tu.SiblingTranslationUnitFinder;
import ch.hsr.ifs.cute.mockator.refsupport.utils.BindingTypeVerifier;


public class TraceFunctionChecker extends AbstractAstFunctionChecker {

   @Override
   protected void processFunction(final IASTFunctionDefinition function) {
      if (!(function instanceof ICPPASTFunctionDefinition)) { return; }

      final ICPPASTFunctionDefinition funDef = (ICPPASTFunctionDefinition) function;

      if (!isFreeFunction(funDef) || !hasConstCharPointerAndIntAsLastTwoArguments(funDef)) { return; }

      if (startsWithTraceFunPrefix(funDef) || isIncludedIntoEveryTu(funDef)) {
         mark(funDef);
      }
   }

   private static boolean isFreeFunction(final ICPPASTFunctionDefinition function) {
      final IBinding binding = function.getDeclarator().getName().resolveBinding();
      return BindingTypeVerifier.isOfType(binding, ICPPFunction.class) && !BindingTypeVerifier.isOfType(binding, ICPPMethod.class);
   }

   private static boolean hasConstCharPointerAndIntAsLastTwoArguments(final ICPPASTFunctionDefinition function) {
      final ICPPASTFunctionDeclarator declarator = (ICPPASTFunctionDeclarator) function.getDeclarator();
      final ICPPASTParameterDeclaration[] parameters = declarator.getParameters();

      if (parameters.length < 2) { return false; }

      return hasConstCharPointerType(parameters[parameters.length - 2]) && hasIntType(parameters[parameters.length - 1]);
   }

   private static boolean hasIntType(final ICPPASTParameterDeclaration param) {
      final IASTDeclSpecifier declSpecifier = param.getDeclSpecifier();

      if (declSpecifier instanceof IASTSimpleDeclSpecifier) { return ((IASTSimpleDeclSpecifier) declSpecifier)
            .getType() == IASTSimpleDeclSpecifier.t_int; }

      return false;
   }

   private static boolean hasConstCharPointerType(final ICPPASTParameterDeclaration param) {
      final IASTDeclSpecifier declSpecifier = param.getDeclSpecifier();

      if (declSpecifier instanceof IASTSimpleDeclSpecifier) {
         final IASTSimpleDeclSpecifier simpleSpec = (IASTSimpleDeclSpecifier) declSpecifier;
         return simpleSpec.getType() == IASTSimpleDeclSpecifier.t_char && simpleSpec.isConst() && param.getDeclarator()
               .getPointerOperators().length == 1;
      }

      return false;
   }

   private boolean isIncludedIntoEveryTu(final ICPPASTFunctionDefinition function) {
      return getPathOfSiblingHeaderFile(function).map(sibling -> new IncludeFileHandler(getProject()).hasInclude(sibling)).orElse(false);
   }

   private Optional<? extends IResource> getPathOfSiblingHeaderFile(final ICPPASTFunctionDefinition function) {
      final ITranslationUnit tu = function.getTranslationUnit().getOriginatingTranslationUnit();

      if (tu == null) { return Optional.empty(); }

      if (tu.isHeaderUnit()) { return Optional.of(tu.getResource()); }

      try {
         final Optional<String> path = getSiblingFilePath(tu, getModelCache().getIndex());
         if (path.isPresent()) { return Optional.of(FileUtil.toIFile(path.get())); }
      } catch (final CoreException e) {
         // to ignore, we do not want to propagate errors in a checker
      }

      return Optional.empty();
   }

   private Optional<String> getSiblingFilePath(final ITranslationUnit tu, final IIndex index) throws CoreException {
      return new SiblingTranslationUnitFinder((IFile) tu.getResource(), getModelCache().getAST(), index).getSiblingTuPath();
   }

   private void mark(final ICPPASTFunctionDefinition function) {
      final IASTName funName = function.getDeclarator().getName();
      reportProblem(ProblemId.TRACE_FUNCTIONS.getId(), funName, funName.toString());
   }

   private static boolean startsWithTraceFunPrefix(final ICPPASTFunctionDefinition function) {
      return function.getDeclarator().getName().getLastName().toString().startsWith(MOCKED_TRACE_PREFIX);
   }
}
