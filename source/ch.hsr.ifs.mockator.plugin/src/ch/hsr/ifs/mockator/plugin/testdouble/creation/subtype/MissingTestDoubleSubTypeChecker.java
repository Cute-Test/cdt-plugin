package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;

import ch.hsr.ifs.mockator.plugin.base.data.Pair;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.CppIncludeResolver;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.BindingTypeVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.QualifiedNameCreator;
import ch.hsr.ifs.mockator.plugin.testdouble.support.TestFunctionChecker;

public class MissingTestDoubleSubTypeChecker extends TestFunctionChecker {

  public static final String MISSING_TEST_DOUBLE_SUBTYPE_PROBLEM_ID = "ch.hsr.ifs.mockator.MissingTestDoubleSubTypeProblem";

  @Override
  protected void processTestFunction(final IASTFunctionDefinition function) {
    final InjectionInfoCollectorFactory factory = new InjectionInfoCollectorFactory(getIndex(), getCProject());
    function.accept(new ASTVisitor() {

      {
        shouldVisitNames = true;
      }

      @Override
      public int visit(final IASTName name) {
        if (isUnknownArgumentType(name)) {
          final DepInjectInfoCollector infoCollector = factory.getInfoCollectorStrategy(name);
          infoCollector.collectDependencyInfos(name).ifPresent((result) -> markMissingInjectedTestDouble(name, result));
        }
        return PROCESS_SKIP;
      }
    });
  }

  private static boolean isUnknownArgumentType(final IASTName name) {
    final IBinding binding = name.resolveBinding();

    if (!isProblemBinding(binding)) {
      return false;
    }

    final IASTNode parent = name.getParent();

    if (!(parent instanceof IASTIdExpression || parent instanceof ICPPASTNamedTypeSpecifier)) {
      return false;
    }

    return isPartOfCtorCall(name) || isPartOfFunCall(name);
  }

  private static boolean isPartOfCtorCall(final IASTNode node) {
    return AstUtil.getAncestorOfType(node, ICPPASTConstructorInitializer.class) != null
        || AstUtil.getAncestorOfType(node, ICPPASTInitializerList.class) != null;
  }

  private static boolean isPartOfFunCall(final IASTNode node) {
    return AstUtil.getAncestorOfType(node, ICPPASTFunctionCallExpression.class) != null;
  }

  private void markMissingInjectedTestDouble(final IASTName name, final Pair<IASTName, IType> optResult) {
    final CreateTestDoubleSubTypeCodanArgs codanArgs = getCodanArgs(name, optResult);
    reportProblem(MissingTestDoubleSubTypeChecker.MISSING_TEST_DOUBLE_SUBTYPE_PROBLEM_ID, name, codanArgs.toArray());
  }

  private CreateTestDoubleSubTypeCodanArgs getCodanArgs(final IASTName name, final Pair<IASTName, IType> targetNameAndType) {
    final IASTTranslationUnit ast = targetNameAndType.first().getTranslationUnit();
    final String parentClassName = getQualifiedNameFor(targetNameAndType.first());
    final String passByStrategy = ArgumentPassByStrategy.getStrategy(targetNameAndType.second()).toString();
    return new CreateTestDoubleSubTypeCodanArgs(name.toString(), parentClassName, getInclude(ast), passByStrategy);
  }

  private String getInclude(final IASTTranslationUnit targetTypeAst) {
    final IASTTranslationUnit thisAst = getAst();
    if (isInSameTu(targetTypeAst, thisAst)) {
      return "";
    }
    final CppIncludeResolver resolver = new CppIncludeResolver(thisAst, getCProject(), getIndex());
    return resolver.resolveIncludePath(targetTypeAst.getFilePath());
  }

  private static boolean isInSameTu(final IASTTranslationUnit targetTypeAst, final IASTTranslationUnit thisAst) {
    return thisAst.getFileLocation().getFileName().equals(targetTypeAst.getFileLocation().getFileName());
  }

  private static String getQualifiedNameFor(final IASTName targetClassName) {
    final QualifiedNameCreator creator = new QualifiedNameCreator(targetClassName);
    return creator.createQualifiedName().toString();
  }

  private static boolean isProblemBinding(final IBinding binding) {
    return BindingTypeVerifier.isOfType(binding, IProblemBinding.class);
  }
}
