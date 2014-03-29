package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICProject;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.PublicMemFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

class CtorInjectionInfoCollector extends AbstractDepInjectInfoCollector {

  public CtorInjectionInfoCollector(IIndex index, ICProject cProject) {
    super(index, cProject);
  }

  @Override
  public Maybe<Pair<IASTName, IType>> collectDependencyInfos(IASTName problemArg) {
    IASTInitializer initializer =
        AstUtil.getAncestorOfType(problemArg, ICPPASTInitializerList.class);
    List<IASTInitializerClause> ctorArgs;

    if (initializer == null) {
      initializer = AstUtil.getAncestorOfType(problemArg, ICPPASTConstructorInitializer.class);

      if (initializer == null)
        return none();

      ctorArgs = list(((ICPPASTConstructorInitializer) initializer).getArguments());
    } else {
      ctorArgs = list(((ICPPASTInitializerList) initializer).getClauses());
    }

    for (ICPPASTCompositeTypeSpecifier optClass : findTargetClass(initializer)) {
      int args = getArgPosOfProblemType(problemArg, ctorArgs);
      for (ICPPASTFunctionDeclarator optCtor : findMatchingCtor(ctorArgs, args, optClass))
        return getTargetClassOfProblemType(optCtor, args);
    }
    return none();
  }

  private Maybe<ICPPASTCompositeTypeSpecifier> findTargetClass(IASTInitializer ctorInitializer) {
    if (isPartOf(ctorInitializer, ICPPASTNewExpression.class))
      return findTargetClassForNewExpression(ctorInitializer);
    else if (isPartOf(ctorInitializer, ICPPASTDeclarator.class))
      return findTargetClassForSimpleDecl(ctorInitializer);
    return none();
  }

  private Maybe<ICPPASTFunctionDeclarator> findMatchingCtor(List<IASTInitializerClause> ctorArgs,
      int argPosOfProblemType, ICPPASTCompositeTypeSpecifier klass) {
    for (ICPPASTFunctionDeclarator publicCtor : getPublicCtors(klass)) {
      if (areEquivalentExceptProblemType(ctorArgs, publicCtor, argPosOfProblemType))
        return maybe(publicCtor);
    }

    return none();
  }

  private static Collection<ICPPASTFunctionDeclarator> getPublicCtors(
      ICPPASTCompositeTypeSpecifier klass) {
    PublicMemFunFinder finder = new PublicMemFunFinder(klass, PublicMemFunFinder.ALL_TYPES);
    List<ICPPASTFunctionDeclarator> publicCtors = list();

    for (IASTDeclaration memFun : finder.getPublicMemFuns()) {
      if (!AstUtil.isDeclConstructor(memFun)) {
        continue;
      }

      ICPPASTFunctionDeclarator funDecl =
          AstUtil.getChildOfType(memFun, ICPPASTFunctionDeclarator.class);

      if (funDecl == null) {
        continue;
      }

      if (isCopyCtor(klass, funDecl)) {
        continue;
      }
      publicCtors.add(funDecl);
    }

    return publicCtors;
  }

  private static boolean isCopyCtor(ICPPASTCompositeTypeSpecifier klass,
      ICPPASTFunctionDeclarator funDecl) {
    ICPPConstructor ctor = (ICPPConstructor) funDecl.getName().resolveBinding();
    ICPPClassType targetClass = (ICPPClassType) klass.getName().resolveBinding();
    return AstUtil.isCopyCtor(ctor, targetClass);
  }

  private static boolean isPartOf(IASTNode node, Class<? extends IASTNode> klass) {
    return AstUtil.getAncestorOfType(node, klass) != null;
  }

  private Maybe<ICPPASTCompositeTypeSpecifier> findTargetClassForSimpleDecl(
      IASTInitializer ctorInitializer) {
    IASTSimpleDeclaration simpleDecl =
        AstUtil.getAncestorOfType(ctorInitializer, IASTSimpleDeclaration.class);
    IASTDeclSpecifier declSpecifier = simpleDecl.getDeclSpecifier();

    if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier))
      return none();

    IASTName name = ((ICPPASTNamedTypeSpecifier) declSpecifier).getName();
    return lookup.findClassDefinition(name.resolveBinding(), index);
  }

  private Maybe<ICPPASTCompositeTypeSpecifier> findTargetClassForNewExpression(
      IASTInitializer ctorInitializer) {
    ICPPASTNewExpression newExpr =
        AstUtil.getAncestorOfType(ctorInitializer, ICPPASTNewExpression.class);
    IASTTypeId typeId = newExpr.getTypeId();
    IASTDeclSpecifier declSpecifier = typeId.getDeclSpecifier();

    if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier))
      return none();

    IASTName name = ((ICPPASTNamedTypeSpecifier) declSpecifier).getName();
    return lookup.findClassDefinition(name.toString(), index);
  }
}
