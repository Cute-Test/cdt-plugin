package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

import ch.hsr.ifs.iltis.core.functional.OptHelper;

import ch.hsr.ifs.mockator.plugin.base.data.Pair;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.PublicMemFunFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


class CtorInjectionInfoCollector extends AbstractDepInjectInfoCollector {

   public CtorInjectionInfoCollector(final IIndex index, final ICProject cProject) {
      super(index, cProject);
   }

   @Override
   public Optional<Pair<IASTName, IType>> collectDependencyInfos(final IASTName problemArg) {
      IASTInitializer initializer = AstUtil.getAncestorOfType(problemArg, ICPPASTInitializerList.class);
      List<IASTInitializerClause> ctorArgs;

      if (initializer == null) {
         initializer = AstUtil.getAncestorOfType(problemArg, ICPPASTConstructorInitializer.class);

         if (initializer == null) { return Optional.empty(); }

         ctorArgs = list(((ICPPASTConstructorInitializer) initializer).getArguments());
      } else {
         ctorArgs = list(((ICPPASTInitializerList) initializer).getClauses());
      }

      return OptHelper.returnIfPresentElseEmpty(findTargetClass(initializer), (clazz) -> {
         final int args = getArgPosOfProblemType(problemArg, ctorArgs);
         return OptHelper.returnIfPresentElseEmpty(findMatchingCtor(ctorArgs, args, clazz), (ctor) -> getTargetClassOfProblemType(ctor, args));
      });
   }

   private Optional<ICPPASTCompositeTypeSpecifier> findTargetClass(final IASTInitializer ctorInitializer) {
      if (isPartOf(ctorInitializer, ICPPASTNewExpression.class)) {
         return findTargetClassForNewExpression(ctorInitializer);
      } else if (isPartOf(ctorInitializer, ICPPASTDeclarator.class)) { return findTargetClassForSimpleDecl(ctorInitializer); }
      return Optional.empty();
   }

   private Optional<ICPPASTFunctionDeclarator> findMatchingCtor(final List<IASTInitializerClause> ctorArgs, final int argPosOfProblemType,
         final ICPPASTCompositeTypeSpecifier klass) {
      for (final ICPPASTFunctionDeclarator publicCtor : getPublicCtors(klass)) {
         if (areEquivalentExceptProblemType(ctorArgs, publicCtor, argPosOfProblemType)) { return Optional.of(publicCtor); }
      }

      return Optional.empty();
   }

   private static Collection<ICPPASTFunctionDeclarator> getPublicCtors(final ICPPASTCompositeTypeSpecifier klass) {
      final PublicMemFunFinder finder = new PublicMemFunFinder(klass, PublicMemFunFinder.ALL_TYPES);
      final List<ICPPASTFunctionDeclarator> publicCtors = list();

      for (final IASTDeclaration memFun : finder.getPublicMemFuns()) {
         if (!AstUtil.isDeclConstructor(memFun)) {
            continue;
         }

         final ICPPASTFunctionDeclarator funDecl = AstUtil.getChildOfType(memFun, ICPPASTFunctionDeclarator.class);

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

   private static boolean isCopyCtor(final ICPPASTCompositeTypeSpecifier klass, final ICPPASTFunctionDeclarator funDecl) {
      final ICPPConstructor ctor = (ICPPConstructor) funDecl.getName().resolveBinding();
      final ICPPClassType targetClass = (ICPPClassType) klass.getName().resolveBinding();
      return AstUtil.isCopyCtor(ctor, targetClass);
   }

   private static boolean isPartOf(final IASTNode node, final Class<? extends IASTNode> klass) {
      return AstUtil.getAncestorOfType(node, klass) != null;
   }

   private Optional<ICPPASTCompositeTypeSpecifier> findTargetClassForSimpleDecl(final IASTInitializer ctorInitializer) {
      final IASTSimpleDeclaration simpleDecl = AstUtil.getAncestorOfType(ctorInitializer, IASTSimpleDeclaration.class);
      final IASTDeclSpecifier declSpecifier = simpleDecl.getDeclSpecifier();

      if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier)) { return Optional.empty(); }

      final IASTName name = ((ICPPASTNamedTypeSpecifier) declSpecifier).getName();
      return lookup.findClassDefinition(name.resolveBinding(), index);
   }

   private Optional<ICPPASTCompositeTypeSpecifier> findTargetClassForNewExpression(final IASTInitializer ctorInitializer) {
      final ICPPASTNewExpression newExpr = AstUtil.getAncestorOfType(ctorInitializer, ICPPASTNewExpression.class);
      final IASTTypeId typeId = newExpr.getTypeId();
      final IASTDeclSpecifier declSpecifier = typeId.getDeclSpecifier();

      if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier)) { return Optional.empty(); }

      final IASTName name = ((ICPPASTNamedTypeSpecifier) declSpecifier).getName();
      return lookup.findClassDefinition(name.toString(), index);
   }
}
