package ch.hsr.ifs.mockator.plugin.testdouble.movetons;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.CUTE_NS;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.CUTE_SUITE;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;

// Parts taken from CUTE
class CuteSuiteFinder extends ASTVisitor {
  private static final String FQ_CUTE_SUITE = AstUtil.getQfName(array(CUTE_NS, CUTE_SUITE));
  private final NodeContainer<IASTName> relatedSuiteName;
  private final ICPPASTFunctionDefinition testFunction;

  {
    shouldVisitNames = true;
  }

  public CuteSuiteFinder(ICPPASTFunctionDefinition testFunction) {
    this.testFunction = testFunction;
    relatedSuiteName = new NodeContainer<IASTName>();
  }

  public Maybe<String> getCuteSuiteName() {
    for (IASTName optName : relatedSuiteName.getNode())
      return maybe(optName.toString());
    return none();
  }

  @Override
  public int visit(IASTName name) {
    if (!isCuiteSuiteName(name))
      return PROCESS_CONTINUE;

    IBinding suiteBinding = name.resolveBinding();

    for (IASTName refName : name.getTranslationUnit().getReferences(suiteBinding)) {
      if (AstUtil.isPushBack(refName) && matchesTestFunction(refName)) {
        relatedSuiteName.setNode(name);
        return PROCESS_ABORT;
      }
    }

    return PROCESS_CONTINUE;
  }

  private static boolean isCuiteSuiteName(IASTName name) {
    IASTSimpleDeclaration simpleDecl = AstUtil.getAncestorOfType(name, IASTSimpleDeclaration.class);

    if (simpleDecl == null)
      return false;

    IASTDeclSpecifier declSpecifier = simpleDecl.getDeclSpecifier();

    if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier))
      return false;

    ICPPASTNamedTypeSpecifier namedSpec = (ICPPASTNamedTypeSpecifier) declSpecifier;
    IASTName typeName = namedSpec.getName();

    if (typeName.toString().equals(FQ_CUTE_SUITE))
      return true;

    IBinding binding = typeName.resolveBinding();

    if (!(binding instanceof ITypedef))
      return false;

    ITypedef typeDef = (ITypedef) binding;
    return typeDef.getName().equals(MockatorConstants.CUTE_SUITE)
        && typeDef.getOwner().getName().equals(MockatorConstants.CUTE_NS);
  }

  private boolean matchesTestFunction(IASTName referencingName) {
    for (String optRegisteredFunName : getRegisteredFunctionName(referencingName))
      return optRegisteredFunName.equals(testFunction.getDeclarator().getName().toString());

    return false;
  }

  private static Maybe<String> getRegisteredFunctionName(IASTName name) {
    IASTFunctionCallExpression funcCall =
        AstUtil.getAncestorOfType(name, IASTFunctionCallExpression.class);
    IASTInitializerClause[] arguments = funcCall.getArguments();

    if (isFunctionPushBack(arguments))
      return getFunctionName(arguments);
    if (isSimpleMemberFunctionPushBack(arguments))
      return getSimpleMemFunName(arguments);
    if (isFunctorPushBack(arguments))
      return getFunctorName(arguments);

    return none();
  }

  private static Maybe<String> getFunctorName(IASTInitializerClause[] arguments) {
    if (isFunctorPushBack(arguments)) {
      ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
      IASTIdExpression idExp = (IASTIdExpression) funcCall.getFunctionNameExpression();
      return maybe(idExp.getName().toString());
    }
    return none();
  }

  private static boolean isFunctorPushBack(IASTInitializerClause[] arguments) {
    if (!(arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression))
      return false;

    ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
    return funcCall.getArguments().length == 0;
  }

  private static Maybe<String> getSimpleMemFunName(IASTInitializerClause[] arguments) {
    if (!isSimpleMemberFunctionPushBack(arguments))
      return none();

    ICPPASTFunctionCallExpression funCall = (ICPPASTFunctionCallExpression) arguments[0];

    if (hasFunCallTwoArgsWithUnaryExpr(funCall)) {
      IASTUnaryExpression unExp = (IASTUnaryExpression) funCall.getArguments()[0];

      if (unExp.getOperand() instanceof IASTIdExpression) {
        IASTIdExpression idExp = (IASTIdExpression) unExp.getOperand();
        return maybe(idExp.getName().toString());
      }
    }

    return none();
  }

  private static boolean hasFunCallTwoArgsWithUnaryExpr(ICPPASTFunctionCallExpression funCall) {
    return (funCall.getArguments().length == 2 && funCall.getArguments()[0] instanceof IASTUnaryExpression);
  }

  private static Maybe<String> getFunctionName(IASTInitializerClause[] arguments) {
    if (!isFunctionPushBack(arguments))
      return none();

    ICPPASTFunctionCallExpression funCall = (ICPPASTFunctionCallExpression) arguments[0];

    if (hasFunCallTwoArgsWithUnaryExpr(funCall)) {
      IASTUnaryExpression unExp = (IASTUnaryExpression) funCall.getArguments()[0];

      if (unExp.getOperand() instanceof IASTUnaryExpression
          && ((IASTUnaryExpression) unExp.getOperand()).getOperand() instanceof IASTIdExpression) {
        IASTIdExpression idExp =
            (IASTIdExpression) ((IASTUnaryExpression) unExp.getOperand()).getOperand();
        return maybe(idExp.getName().toString());
      }
    }
    return none();
  }

  private static boolean isFunctionPushBack(IASTInitializerClause[] arguments) {
    return isFunctionPushBackWithName(arguments,
        AstUtil.getQfName(array(MockatorConstants.CUTE_NS, "test")));
  }

  private static boolean isSimpleMemberFunctionPushBack(IASTInitializerClause[] arguments) {
    return isFunctionPushBackWithName(arguments,
        AstUtil.getQfName(array(MockatorConstants.CUTE_NS, "makeSimpleMemberFunctionTest")));
  }

  private static boolean isFunctionPushBackWithName(IASTInitializerClause[] arguments,
      String funName) {
    if (arguments.length == 1 && arguments[0] instanceof ICPPASTFunctionCallExpression) {
      ICPPASTFunctionCallExpression funcCall = (ICPPASTFunctionCallExpression) arguments[0];
      return isSameFunctionName(funcCall, funName);
    }

    return false;
  }

  private static boolean isSameFunctionName(ICPPASTFunctionCallExpression funCall, String funName) {
    IASTExpression funNameExpr = funCall.getFunctionNameExpression();
    if (!(funNameExpr instanceof IASTIdExpression))
      return false;
    return ((IASTIdExpression) funNameExpr).getName().toString().startsWith(funName);
  }
}
