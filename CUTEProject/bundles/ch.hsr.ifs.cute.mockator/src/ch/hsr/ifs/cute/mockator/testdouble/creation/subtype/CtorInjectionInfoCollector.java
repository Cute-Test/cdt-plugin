package ch.hsr.ifs.cute.mockator.testdouble.creation.subtype;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.list;

import java.util.ArrayList;
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

import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.refsupport.finder.PublicMemFunFinder;


class CtorInjectionInfoCollector extends AbstractDepInjectInfoCollector {

   public CtorInjectionInfoCollector(final IIndex index, final ICProject cProject) {
      super(index, cProject);
   }

   @Override
   public Optional<DependencyInfo> collectDependencyInfos(final IASTName problemArg) {
      IASTInitializer initializer = CPPVisitor.findAncestorWithType(problemArg, ICPPASTInitializerList.class).orElse(null);
      List<IASTInitializerClause> ctorArgs;

      if (initializer == null) {
         initializer = CPPVisitor.findAncestorWithType(problemArg, ICPPASTConstructorInitializer.class).orElse(null);

         if (initializer == null) { return Optional.empty(); }

         ctorArgs = list(((ICPPASTConstructorInitializer) initializer).getArguments());
      } else {
         ctorArgs = list(((ICPPASTInitializerList) initializer).getClauses());
      }
      return findTargetClass(initializer).flatMap(clazz -> {
         final int args = getArgPosOfProblemType(problemArg, ctorArgs);
         return findMatchingCtor(ctorArgs, args, clazz).flatMap(ctor -> getTargetClassOfProblemType(ctor, args));
      });
   }

   private Optional<ICPPASTCompositeTypeSpecifier> findTargetClass(final IASTInitializer ctorInitializer) {
      if (isPartOf(ctorInitializer, ICPPASTNewExpression.class)) {
         return findTargetClassForNewExpression(ctorInitializer);
      } else if (isPartOf(ctorInitializer, ICPPASTDeclarator.class)) { return findTargetClassForSimpleDecl(ctorInitializer); }
      return Optional.empty();
   }

   private Optional<ICPPASTFunctionDeclarator> findMatchingCtor(final List<IASTInitializerClause> ctorArgs, final int argPosOfProblemType,
         final ICPPASTCompositeTypeSpecifier clazz) {
      for (final ICPPASTFunctionDeclarator publicCtor : getPublicCtors(clazz)) {
         if (areEquivalentExceptProblemType(ctorArgs, publicCtor, argPosOfProblemType)) { return Optional.of(publicCtor); }
      }

      return Optional.empty();
   }

   private static Collection<ICPPASTFunctionDeclarator> getPublicCtors(final ICPPASTCompositeTypeSpecifier clazz) {
      final PublicMemFunFinder finder = new PublicMemFunFinder(clazz, PublicMemFunFinder.ALL_TYPES);
      final List<ICPPASTFunctionDeclarator> publicCtors = new ArrayList<>();

      for (final IASTDeclaration memFun : finder.getPublicMemFuns()) {
         if (!ASTUtil.isDeclConstructor(memFun)) {
            continue;
         }

         final ICPPASTFunctionDeclarator funDecl = CPPVisitor.findChildWithType(memFun, ICPPASTFunctionDeclarator.class).orElse(null);

         if (funDecl == null) {
            continue;
         }

         if (isCopyCtor(clazz, funDecl)) {
            continue;
         }
         publicCtors.add(funDecl);
      }

      return publicCtors;
   }

   private static boolean isCopyCtor(final ICPPASTCompositeTypeSpecifier clazz, final ICPPASTFunctionDeclarator funDecl) {
      final ICPPConstructor ctor = (ICPPConstructor) funDecl.getName().resolveBinding();
      final ICPPClassType targetClass = (ICPPClassType) clazz.getName().resolveBinding();
      return ASTUtil.isCopyCtor(ctor, targetClass);
   }

   private static boolean isPartOf(final IASTNode node, final Class<? extends IASTNode> clazz) {
      return CPPVisitor.findAncestorWithType(node, clazz).orElse(null) != null;
   }

   private Optional<ICPPASTCompositeTypeSpecifier> findTargetClassForSimpleDecl(final IASTInitializer ctorInitializer) {
      final IASTSimpleDeclaration simpleDecl = CPPVisitor.findAncestorWithType(ctorInitializer, IASTSimpleDeclaration.class).orElse(null);
      final IASTDeclSpecifier declSpecifier = simpleDecl.getDeclSpecifier();

      if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier)) { return Optional.empty(); }

      final IASTName name = ((ICPPASTNamedTypeSpecifier) declSpecifier).getName();
      return lookup.findClassDefinition(name.resolveBinding(), index);
   }

   private Optional<ICPPASTCompositeTypeSpecifier> findTargetClassForNewExpression(final IASTInitializer ctorInitializer) {
      final ICPPASTNewExpression newExpr = CPPVisitor.findAncestorWithType(ctorInitializer, ICPPASTNewExpression.class).orElse(null);
      final IASTTypeId typeId = newExpr.getTypeId();
      final IASTDeclSpecifier declSpecifier = typeId.getDeclSpecifier();

      if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier)) { return Optional.empty(); }

      final IASTName name = ((ICPPASTNamedTypeSpecifier) declSpecifier).getName();
      return lookup.findClassDefinition(name.toString(), index);
   }
}
