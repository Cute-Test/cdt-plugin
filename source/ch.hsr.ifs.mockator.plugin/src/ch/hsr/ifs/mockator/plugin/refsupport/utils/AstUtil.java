package ch.hsr.ifs.mockator.plugin.refsupport.utils;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;
import static ch.hsr.ifs.mockator.plugin.base.misc.CastHelper.unsecureCast;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTTypeMatcher;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPDeferredClassInstance;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;

@SuppressWarnings("restriction")
public abstract class AstUtil {

  public static boolean isSameType(IType lhs, IType rhs) {
    return new ASTTypeMatcher().isEquivalent(lhs, rhs);
  }

  public static boolean isStructType(ICPPASTCompositeTypeSpecifier klass) {
    return klass.getKey() == IASTCompositeTypeSpecifier.k_struct;
  }

  public static boolean isClassType(ICPPASTCompositeTypeSpecifier klass) {
    return klass.getKey() == ICPPASTCompositeTypeSpecifier.k_class;
  }

  public static boolean isClass(IASTNode node) {
    return node instanceof ICPPASTCompositeTypeSpecifier;
  }

  public static boolean isQualifiedName(IASTName name) {
    return name instanceof ICPPASTQualifiedName;
  }

  public static IASTName getName(IASTFunctionCallExpression callExpr) {
    if (callExpr instanceof IASTIdExpression) {
      IASTIdExpression idExpr = (IASTIdExpression) callExpr.getFunctionNameExpression();
      return idExpr.getName();
    }

    IASTExpression expression = callExpr.getFunctionNameExpression();

    if (expression instanceof ICPPASTFieldReference)
      return ((ICPPASTFieldReference) expression).getFieldName();
    else if (expression instanceof IASTIdExpression)
      return ((IASTIdExpression) expression).getName().getLastName();

    throw new MockatorException("Was not able to determine name of function call");
  }

  public static IASTDeclaration[] getAllDeclarations(IASTNode parent) {
    if ((parent instanceof IASTTranslationUnit))
      return ((IASTTranslationUnit) parent).getDeclarations();
    if ((parent instanceof ICPPASTCompositeTypeSpecifier))
      return ((ICPPASTCompositeTypeSpecifier) parent).getMembers();
    if ((parent instanceof ICPPASTNamespaceDefinition))
      return ((ICPPASTNamespaceDefinition) parent).getDeclarations();

    return new IASTDeclaration[0];
  }

  public static ICPPASTDeclSpecifier getDeclSpec(ICPPASTFunctionDeclarator funDecl) {
    ICPPASTFunctionDefinition funDef =
        AstUtil.getAncestorOfType(funDecl, ICPPASTFunctionDefinition.class);

    if (funDef != null)
      return (ICPPASTDeclSpecifier) funDef.getDeclSpecifier();

    IASTSimpleDeclaration simpleDecl =
        AstUtil.getAncestorOfType(funDecl, IASTSimpleDeclaration.class);
    return (ICPPASTDeclSpecifier) simpleDecl.getDeclSpecifier();
  }

  public static boolean hasConstPart(IType type) {
    if (!(type instanceof ITypeContainer))
      return false;

    if (type instanceof IQualifierType) {
      if (((IQualifierType) type).isConst())
        return true;
    }

    return hasConstPart(((ITypeContainer) type).getType());
  }

  public static boolean hasVolatilePart(IType type) {
    if (!(type instanceof ITypeContainer))
      return false;

    if (type instanceof IQualifierType) {
      if (((IQualifierType) type).isVolatile())
        return true;
    }
    return hasVolatilePart(((ITypeContainer) type).getType());
  }

  public static boolean isStatic(IASTDeclSpecifier specifier) {
    return specifier != null && specifier.getStorageClass() == IASTDeclSpecifier.sc_static;
  }

  public static boolean isConstructor(ICPPASTFunctionDefinition function) {
    return function.getDeclarator().getName().resolveBinding() instanceof ICPPConstructor;
  }

  public static boolean isDeclConstructor(IASTDeclaration declaration) {
    IASTDeclarator declaratorForNode = AstUtil.getDeclaratorForNode(declaration);

    if (!(declaratorForNode instanceof ICPPASTFunctionDeclarator))
      return false;

    for (IASTDeclSpecifier optDeclSpec : AstUtil.getDeclarationSpecifier(declaration))
      return optDeclSpec instanceof IASTSimpleDeclSpecifier
          && isUnspecified((IASTSimpleDeclSpecifier) optDeclSpec);

    return false;
  }

  public static boolean isUnspecified(IASTSimpleDeclSpecifier declSpec) {
    return declSpec.getType() == IASTSimpleDeclSpecifier.t_unspecified;
  }

  public static List<ICPPASTFunctionDefinition> getFunctionDefinitions(
      Collection<IASTDeclaration> publicMemFuns) {
    List<ICPPASTFunctionDefinition> result = list();

    for (IASTDeclaration fun : publicMemFuns) {
      ICPPASTFunctionDefinition candidate =
          AstUtil.getChildOfType(fun, ICPPASTFunctionDefinition.class);

      if (candidate != null) {
        result.add(candidate);
      }
    }
    return result;
  }

  public static <T> T getAncestorOfType(IASTNode node, Class<? extends IASTNode> T) {
    IASTNode currentNode = node;

    while (currentNode != null) {
      if (T.isInstance(currentNode))
        return unsecureCast(currentNode);

      currentNode = currentNode.getParent();
    }
    return null;
  }

  public static <T> T getChildOfType(IASTNode node, Class<? extends IASTNode> T) {
    if (node == null)
      return null;

    if (T.isInstance(node))
      return unsecureCast(node);

    for (IASTNode child : node.getChildren()) {
      T currentNode = getChildOfType(child, T);

      if (currentNode != null)
        return currentNode;
    }

    return null;
  }

  public static boolean hasPointerOrRefType(IASTDeclarator declarator) {
    if (declarator == null)
      return false;

    if (declarator instanceof IASTArrayDeclarator) {
      IASTArrayDeclarator arrayDecl = (IASTArrayDeclarator) declarator;
      return arrayDecl.getPointerOperators().length > 0;
    }

    if (declarator.getPointerOperators().length > 0)
      return true;

    IBinding declBinding = declarator.getName().resolveBinding();

    if (declBinding instanceof IVariable) {
      IType type = ((IVariable) declBinding).getType();
      return hasPointerOrRefType(type);
    } else if (declBinding instanceof ICPPMethod) {
      IType type = ((ICPPMethod) declBinding).getType().getReturnType();
      return hasPointerOrRefType(type);
    }

    return false;
  }

  public static boolean hasPointerOrRefType(IType type) {
    return type instanceof ICPPReferenceType || type instanceof IPointerType;
  }

  public static IType unwindPointerOrRefType(IType type) {
    if (type instanceof IPointerType) {
      type = ((IPointerType) type).getType();
    }

    if (type instanceof ICPPReferenceType) {
      type = ((ICPPReferenceType) type).getType();
    }

    return type;
  }

  public static IASTCompoundStatement toCompoundStatement(IASTStatement stmt) {
    if (stmt instanceof IASTCompoundStatement)
      return (IASTCompoundStatement) stmt;

    IASTCompoundStatement compound = new CPPASTCompoundStatement();
    compound.addStatement(stmt);
    return compound;
  }

  public static IType asNonConst(IType type) {
    while (type instanceof IQualifierType && ((IQualifierType) type).isConst()) {
      type = ((IQualifierType) type).getType();
    }
    return type;
  }

  public static IASTDeclarator getDeclaratorForNode(IASTNode node) {
    IASTDeclarator declarator = null;

    if (node instanceof IASTSimpleDeclaration) {
      IASTSimpleDeclaration decl = (IASTSimpleDeclaration) node;

      if (decl.getDeclarators().length > 0) {
        declarator = decl.getDeclarators()[0];
      }
    } else if (node instanceof IASTParameterDeclaration) {
      IASTParameterDeclaration decl = (IASTParameterDeclaration) node;
      declarator = decl.getDeclarator();
    } else if (node instanceof ICPPASTFunctionDefinition) {
      ICPPASTFunctionDefinition fun = (ICPPASTFunctionDefinition) node;
      declarator = fun.getDeclarator();
    }

    return declarator;
  }

  public static Maybe<IASTDeclSpecifier> getDeclarationSpecifier(IASTNode node) {
    if (isPartOf(node, ICPPASTParameterDeclaration.class)) {
      ICPPASTParameterDeclaration paramDecl =
          AstUtil.getAncestorOfType(node, ICPPASTParameterDeclaration.class);
      return maybe(paramDecl.getDeclSpecifier());
    } else if (isPartOf(node, ICPPASTFunctionDefinition.class)) {
      ICPPASTFunctionDefinition funDef =
          AstUtil.getAncestorOfType(node, ICPPASTFunctionDefinition.class);
      return maybe(funDef.getDeclSpecifier());
    } else if (isPartOf(node, IASTSimpleDeclaration.class)) {
      IASTSimpleDeclaration simpleDecl =
          AstUtil.getAncestorOfType(node, IASTSimpleDeclaration.class);
      return maybe(simpleDecl.getDeclSpecifier());
    }
    return none();
  }

  public static boolean isCopyCtor(ICPPConstructor ctor, ICPPClassType classType) {
    IType[] paramTypes = ctor.getType().getParameterTypes();

    if (paramTypes.length == 0)
      return false;

    if (!isFstCopyCtorParam(paramTypes[0], classType))
      return false;

    ICPPParameter[] params = ctor.getParameters();

    for (int i = 1; i < params.length; i++) {
      if (!params[i].hasDefaultValue())
        return false;
    }

    return true;
  }

  private static boolean isFstCopyCtorParam(IType paramType, ICPPClassType classType) {
    if (!(paramType instanceof ICPPReferenceType))
      return false;

    IType candidate = ((ICPPReferenceType) paramType).getType();

    if ((candidate instanceof IQualifierType)) {
      candidate = ((IQualifierType) candidate).getType();
    }

    if ((candidate instanceof ICPPDeferredClassInstance)) {
      candidate = ((ICPPDeferredClassInstance) candidate).getClassTemplate();
    }

    if (candidate == null)
      return false;

    return candidate.isSameType(classType);
  }

  public static boolean isDefaultCtor(ICPPConstructor ctor) {
    ICPPParameter[] params = ctor.getParameters();
    return params.length == 0 || ((params.length == 1) && (isVoid(params[0])))
        || haveAllDefaultValue(params);
  }

  private static boolean isVoid(ICPPParameter param) {
    if ((param.getType() instanceof IBasicType))
      return ((IBasicType) param.getType()).getKind().equals(IBasicType.Kind.eVoid);

    return false;
  }

  private static boolean haveAllDefaultValue(ICPPParameter[] parameters) {
    for (int i = 0; i < parameters.length; i++) {
      if (!parameters[i].hasDefaultValue())
        return false;
    }

    return true;
  }

  public static String getQfNameF(ICPPASTCompositeTypeSpecifier klass) {
    IBinding klassBinding = klass.getName().resolveBinding();
    Assert.instanceOf(klassBinding, ICPPClassType.class, "Class expected");
    return getQfName(((ICPPClassType) klassBinding));
  }

  public static String getQfName(ICPPBinding binding) {
    try {
      return getQfName(binding.getQualifiedName());
    } catch (DOMException e) {
      throw new MockatorException(e);
    }
  }

  public static String getQfName(String[] names) {
    return StringUtil.join(list(names), "::");
  }

  public static boolean isVoid(ICPPASTParameterDeclaration param) {
    return param.getDeclarator().getPointerOperators().length == 0
        && isVoid(param.getDeclSpecifier());
  }

  public static boolean isVoid(IASTDeclSpecifier specifier) {
    return specifier instanceof IASTSimpleDeclSpecifier
        && ((IASTSimpleDeclSpecifier) specifier).getType() == IASTSimpleDeclSpecifier.t_void;
  }

  public static void removeExternalStorageIfSet(IASTDeclSpecifier newDeclSpec) {
    if (newDeclSpec.getStorageClass() == IASTDeclSpecifier.sc_extern) {
      newDeclSpec.setStorageClass(IASTDeclSpecifier.sc_unspecified);
    }
  }

  public static boolean isPushBack(IASTName name) {
    IASTFunctionCallExpression funcCall = getAncestorOfType(name, IASTFunctionCallExpression.class);

    if (funcCall != null && funcCall.getFunctionNameExpression() instanceof IASTFieldReference) {
      IASTFieldReference idExp = (IASTFieldReference) funcCall.getFunctionNameExpression();
      return idExp.getFieldName().toString().equals(MockatorConstants.PUSH_BACK);
    }

    return false;
  }

  public static boolean isPartOf(IASTNode node, Class<? extends IASTNode> klass) {
    return getAncestorOfType(node, klass) != null;
  }

  public static IType windDownToRealType(IType type, boolean stopAtTypeDef) {
    if (type instanceof ITypeContainer) {
      if (stopAtTypeDef && type instanceof ITypedef)
        return type;

      type = ((ITypeContainer) type).getType();
      return windDownToRealType(type, stopAtTypeDef);
    }

    return type;
  }

  public static IType getType(IASTInitializerClause clause) {
    if (clause instanceof IASTInitializerList) {
      IASTInitializerClause[] clauses = ((IASTInitializerList) clause).getClauses();
      if (clauses.length > 0)
        return getType(clauses[0]);

      final int noQualifiers = 0;
      return new CPPBasicType(Kind.eInt, noQualifiers);
    }

    if (clause instanceof IASTIdExpression) {
      IASTName name = ((IASTIdExpression) clause).getName();
      IBinding binding = name.resolveBinding();

      if (binding instanceof IVariable)
        return ((IVariable) binding).getType();
      if (binding instanceof ICPPConstructor)
        return ((ICPPConstructor) binding).getClassOwner();
      if (binding instanceof IFunction)
        return ((IFunction) binding).getType().getReturnType();
      if (binding instanceof ITypedef)
        return ((ITypedef) binding).getType();
      if (binding instanceof ICPPClassType)
        return ((ICPPClassType) binding);
    }

    if (clause instanceof IASTExpression)
      return ((IASTExpression) clause).getExpressionType();

    return new CPPBasicType(Kind.eInt, 0);
  }
}
