package ch.hsr.ifs.mockator.plugin.preprocessor.qf;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.MOCKED_TRACE_PREFIX;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

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

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.project.cdt.options.IncludeFileHandler;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.SiblingTranslationUnitFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.BindingTypeVerifier;

public class TraceFunctionChecker extends AbstractAstFunctionChecker {
  public static final String TRACE_FUNCTIONS_PROBLEM_ID =
      "ch.hsr.ifs.mockator.TraceFunctionProblem";

  @Override
  protected void processFunction(IASTFunctionDefinition function) {
    if (!(function instanceof ICPPASTFunctionDefinition))
      return;

    ICPPASTFunctionDefinition funDef = (ICPPASTFunctionDefinition) function;

    if (!isFreeFunction(funDef) || !hasConstCharPointerAndIntAsLastTwoArguments(funDef))
      return;

    if (startsWithTraceFunPrefix(funDef) || isIncludedIntoEveryTu(funDef)) {
      mark(funDef);
    }
  }

  private static boolean isFreeFunction(ICPPASTFunctionDefinition function) {
    IBinding binding = function.getDeclarator().getName().resolveBinding();
    return BindingTypeVerifier.isOfType(binding, ICPPFunction.class)
        && !BindingTypeVerifier.isOfType(binding, ICPPMethod.class);
  }

  private static boolean hasConstCharPointerAndIntAsLastTwoArguments(
      ICPPASTFunctionDefinition function) {
    ICPPASTFunctionDeclarator declarator = (ICPPASTFunctionDeclarator) function.getDeclarator();
    ICPPASTParameterDeclaration[] parameters = declarator.getParameters();

    if (parameters.length < 2)
      return false;

    return hasConstCharPointerType(parameters[parameters.length - 2])
        && hasIntType(parameters[parameters.length - 1]);
  }

  private static boolean hasIntType(ICPPASTParameterDeclaration param) {
    IASTDeclSpecifier declSpecifier = param.getDeclSpecifier();

    if (declSpecifier instanceof IASTSimpleDeclSpecifier)
      return ((IASTSimpleDeclSpecifier) declSpecifier).getType() == IASTSimpleDeclSpecifier.t_int;

    return false;
  }

  private static boolean hasConstCharPointerType(ICPPASTParameterDeclaration param) {
    IASTDeclSpecifier declSpecifier = param.getDeclSpecifier();

    if (declSpecifier instanceof IASTSimpleDeclSpecifier) {
      IASTSimpleDeclSpecifier simpleSpec = (IASTSimpleDeclSpecifier) declSpecifier;
      return simpleSpec.getType() == IASTSimpleDeclSpecifier.t_char && simpleSpec.isConst()
          && param.getDeclarator().getPointerOperators().length == 1;
    }

    return false;
  }

  private boolean isIncludedIntoEveryTu(ICPPASTFunctionDefinition function) {
    IncludeFileHandler includeHandler = new IncludeFileHandler(getProject());

    for (IResource optSibling : getPathOfSiblingHeaderFile(function))
      return includeHandler.hasInclude(optSibling);

    return false;
  }

  private Maybe<? extends IResource> getPathOfSiblingHeaderFile(ICPPASTFunctionDefinition function) {
    ITranslationUnit tu = function.getTranslationUnit().getOriginatingTranslationUnit();

    if (tu == null)
      return none();

    if (tu.isHeaderUnit())
      return maybe(tu.getResource());

    try {
      for (String path : getSiblingFilePath(tu, getModelCache().getIndex()))
        return maybe(FileUtil.toIFile(path));
    } catch (CoreException e) {
      // to ignore, we do not want to propagate errors in a checker
    }

    return none();
  }

  private Maybe<String> getSiblingFilePath(ITranslationUnit tu, IIndex index) throws CoreException {
    return new SiblingTranslationUnitFinder((IFile) tu.getResource(), getModelCache().getAST(),
        index).getSiblingTuPath();
  }

  private void mark(ICPPASTFunctionDefinition function) {
    IASTName funName = function.getDeclarator().getName();
    reportProblem(TRACE_FUNCTIONS_PROBLEM_ID, funName, funName.toString());
  }

  private static boolean startsWithTraceFunPrefix(ICPPASTFunctionDefinition function) {
    return function.getDeclarator().getName().getLastName().toString()
        .startsWith(MOCKED_TRACE_PREFIX);
  }
}
