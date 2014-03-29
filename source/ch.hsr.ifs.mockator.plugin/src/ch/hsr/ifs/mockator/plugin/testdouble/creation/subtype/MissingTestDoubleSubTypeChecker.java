package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;

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

import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.CppIncludeResolver;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.BindingTypeVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.QualifiedNameCreator;
import ch.hsr.ifs.mockator.plugin.testdouble.support.TestFunctionChecker;

public class MissingTestDoubleSubTypeChecker extends TestFunctionChecker {
  public static final String MISSING_TEST_DOUBLE_SUBTYPE_PROBLEM_ID =
      "ch.hsr.ifs.mockator.MissingTestDoubleSubTypeProblem";

  @Override
  protected void processTestFunction(IASTFunctionDefinition function) {
    final InjectionInfoCollectorFactory factory =
        new InjectionInfoCollectorFactory(getIndex(), getCProject());
    function.accept(new ASTVisitor() {
      {
        shouldVisitNames = true;
      }

      @Override
      public int visit(IASTName name) {
        if (isUnknownArgumentType(name)) {
          DepInjectInfoCollector infoCollector = factory.getInfoCollectorStrategy(name);

          for (Pair<IASTName, IType> optResult : infoCollector.collectDependencyInfos(name)) {
            markMissingInjectedTestDouble(name, optResult);
          }
        }
        return PROCESS_SKIP;
      }
    });
  }

  private static boolean isUnknownArgumentType(IASTName name) {
    IBinding binding = name.resolveBinding();

    if (!isProblemBinding(binding))
      return false;

    IASTNode parent = name.getParent();

    if (!(parent instanceof IASTIdExpression || parent instanceof ICPPASTNamedTypeSpecifier))
      return false;

    return isPartOfCtorCall(name) || isPartOfFunCall(name);
  }

  private static boolean isPartOfCtorCall(IASTNode node) {
    return AstUtil.getAncestorOfType(node, ICPPASTConstructorInitializer.class) != null
        || AstUtil.getAncestorOfType(node, ICPPASTInitializerList.class) != null;
  }

  private static boolean isPartOfFunCall(IASTNode node) {
    return AstUtil.getAncestorOfType(node, ICPPASTFunctionCallExpression.class) != null;
  }

  private void markMissingInjectedTestDouble(IASTName name, Pair<IASTName, IType> optResult) {
    CreateTestDoubleSubTypeCodanArgs codanArgs = getCodanArgs(name, optResult);
    reportProblem(MissingTestDoubleSubTypeChecker.MISSING_TEST_DOUBLE_SUBTYPE_PROBLEM_ID, name,
        codanArgs.toArray());
  }

  private CreateTestDoubleSubTypeCodanArgs getCodanArgs(IASTName name,
      Pair<IASTName, IType> targetNameAndType) {
    IASTTranslationUnit ast = _1(targetNameAndType).getTranslationUnit();
    String parentClassName = getQualifiedNameFor(_1(targetNameAndType));
    String passByStrategy = ArgumentPassByStrategy.getStrategy(_2(targetNameAndType)).toString();
    return new CreateTestDoubleSubTypeCodanArgs(name.toString(), parentClassName, getInclude(ast),
        passByStrategy);
  }

  private String getInclude(IASTTranslationUnit targetTypeAst) {
    IASTTranslationUnit thisAst = getAst();
    if (isInSameTu(targetTypeAst, thisAst))
      return "";
    CppIncludeResolver resolver = new CppIncludeResolver(thisAst, getCProject(), getIndex());
    return resolver.resolveIncludePath(targetTypeAst.getFilePath());
  }

  private static boolean isInSameTu(IASTTranslationUnit targetTypeAst, IASTTranslationUnit thisAst) {
    return thisAst.getFileLocation().getFileName()
        .equals(targetTypeAst.getFileLocation().getFileName());
  }

  private static String getQualifiedNameFor(IASTName targetClassName) {
    QualifiedNameCreator creator = new QualifiedNameCreator(targetClassName);
    return creator.createQualifiedName().toString();
  }

  private static boolean isProblemBinding(IBinding binding) {
    return BindingTypeVerifier.isOfType(binding, IProblemBinding.class);
  }
}
