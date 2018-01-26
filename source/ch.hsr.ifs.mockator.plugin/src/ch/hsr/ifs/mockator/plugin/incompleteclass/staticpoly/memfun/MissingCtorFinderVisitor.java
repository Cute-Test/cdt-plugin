package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun;

import static ch.hsr.ifs.iltis.core.collections.CollectionHelper.head;
import static ch.hsr.ifs.iltis.core.collections.CollectionHelper.orderPreservingSet;
import static ch.hsr.ifs.iltis.core.collections.CollectionHelper.unorderedMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.CtorArgumentsCopier;


class MissingCtorFinderVisitor extends MissingMemFunVisitor {

   private static final ICPPNodeFactory                          nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final Collection<Constructor>                         missingCtors;
   private final Map<String, ICPPASTConstructorChainInitializer> initialisers;

   {
      shouldVisitDeclarations = true;
      shouldVisitExpressions = true;
      shouldVisitInitializers = true;
   }

   public MissingCtorFinderVisitor(final ICPPASTCompositeTypeSpecifier testDouble, final ICPPASTTemplateParameter templateParam,
                                   final ICPPASTTemplateDeclaration sut) {
      super(testDouble, templateParam, sut);
      missingCtors = orderPreservingSet();
      initialisers = unorderedMap();
   }

   @Override
   public Collection<? extends StaticPolyMissingMemFun> getMissingMemberFunctions() {
      addDefaultCtorIfNecessary();

      if (isSolelyDefaultCtor()) {
         return new ArrayList<>();
      }

      return missingCtors;
   }

   private void addDefaultCtorIfNecessary() {
      if (hasDefaultInitTemplateParamMember()) {
         missingCtors.add(createCtorWith(nodeFactory.newInitializerList()));
      }
   }

   private boolean hasDefaultInitTemplateParamMember() {
      final ICPPASTCompositeTypeSpecifier sutClass = ASTUtil.getChildOfType(sut, ICPPASTCompositeTypeSpecifier.class);

      if (sutClass == null) {
         return false;
      }

      for (final IASTDeclaration member : sutClass.getMembers()) {
         if (!(member instanceof IASTSimpleDeclaration)) {
            continue;
         }

         final IASTDeclarator[] declarators = ((IASTSimpleDeclaration) member).getDeclarators();

         if (!(declarators.length == 1 && declarators[0] instanceof ICPPASTDeclarator)) {
            continue;
         }

         final IASTName name = declarators[0].getName();

         if (!resolvesToTemplateParam(getType(name))) {
            continue;
         }

         if (isTypeNotInitialized(name)) {
            return true;
         }
      }

      return false;
   }

   private boolean isTypeNotInitialized(final IASTName name) {
      final ICPPASTConstructorChainInitializer chainInitializer = initialisers.get(name.toString());

      if (chainInitializer == null) {
         return true;
      }

      final IASTInitializer initializer = chainInitializer.getInitializer();
      return !(initializer instanceof ICPPASTConstructorInitializer) || hasEmptyInitializer(initializer);
   }

   private static boolean hasEmptyInitializer(final IASTInitializer initializer) {
      return ((ICPPASTConstructorInitializer) initializer).getArguments().length == 0;
   }

   private boolean isSolelyDefaultCtor() {
      return missingCtors.size() == 1 && head(missingCtors).get().isDefaultConstructor();
   }

   @Override
   public int visit(final IASTInitializer initializer) {
      if (!(initializer instanceof ICPPASTConstructorInitializer)) {
         return PROCESS_CONTINUE;
      }

      final ICPPASTConstructorInitializer ctorInitializer = (ICPPASTConstructorInitializer) initializer;

      if (ASTUtil.isPartOf(ctorInitializer, ICPPASTConstructorChainInitializer.class)) {
         return handleCtorInitializer(initializer, ctorInitializer);
      } else if (ASTUtil.isPartOf(ctorInitializer, ICPPASTNewExpression.class)) {
         return handleNewExpression(ctorInitializer);
      } else if (ASTUtil.isPartOf(ctorInitializer, ICPPASTDeclarator.class)) {
         return handleSimpleDecl(ctorInitializer);
      }

      return PROCESS_CONTINUE;
   }

   private int handleSimpleDecl(final ICPPASTConstructorInitializer ctorInitializer) {
      final ICPPASTDeclarator declarator = ASTUtil.getAncestorOfType(ctorInitializer, ICPPASTDeclarator.class);

      if (resolvesToTemplateParam(getType(declarator.getName()))) {
         addToMissingCtors(declarator.getInitializer());
         return PROCESS_SKIP;
      }

      return PROCESS_CONTINUE;
   }

   private int handleNewExpression(final ICPPASTConstructorInitializer ctorInitializer) {
      final ICPPASTNewExpression newExpr = ASTUtil.getAncestorOfType(ctorInitializer, ICPPASTNewExpression.class);

      if (resolvesToTemplateParam(newExpr.getExpressionType())) {
         addToMissingCtors(newExpr.getInitializer());
         return PROCESS_SKIP;
      }

      return PROCESS_CONTINUE;
   }

   private int handleCtorInitializer(final IASTInitializer initializer, final ICPPASTConstructorInitializer ctorInitializer) {
      final ICPPASTConstructorChainInitializer ctor = ASTUtil.getAncestorOfType(ctorInitializer, ICPPASTConstructorChainInitializer.class);
      final IASTName memberInitializerId = ctor.getMemberInitializerId();

      if (resolvesToTemplateParam(getType(memberInitializerId))) {
         initialisers.put(memberInitializerId.toString(), ctor);
         addToMissingCtors(initializer);
         return PROCESS_SKIP;
      }

      return PROCESS_CONTINUE;
   }

   @Override
   public int visit(final IASTDeclaration decl) {
      if (!(decl instanceof IASTSimpleDeclaration && ASTUtil.isPartOf(decl, ICPPASTFunctionDefinition.class))) {
         return PROCESS_CONTINUE;
      }

      final ICPPASTConstructorInitializer ctorInit = ASTUtil.getChildOfType(decl, ICPPASTConstructorInitializer.class);
      final ICPPASTFunctionCallExpression funCall = ASTUtil.getChildOfType(decl, ICPPASTFunctionCallExpression.class);

      if (ctorInit != null || funCall != null) {
         return PROCESS_CONTINUE;
      }

      final IASTDeclarator[] declarators = ((IASTSimpleDeclaration) decl).getDeclarators();

      if (declarators.length == 0) {
         return PROCESS_CONTINUE;
      }

      if (resolvesToTemplateParam(getType(declarators[0].getName()))) {
         addToMissingCtors(nodeFactory.newInitializerList());
         return PROCESS_SKIP;
      }

      return PROCESS_CONTINUE;
   }

   private void addToMissingCtors(final IASTInitializer initializerToUse) {
      missingCtors.add(createCtorWith(initializerToUse));
   }

   @Override
   public int visit(final IASTExpression expression) {
      if (!(expression instanceof ICPPASTFunctionCallExpression)) {
         return PROCESS_CONTINUE;
      }

      final ICPPASTFunctionCallExpression funCall = (ICPPASTFunctionCallExpression) expression;
      final IASTExpression functionNameExpression = funCall.getFunctionNameExpression();

      if (!(functionNameExpression instanceof IASTIdExpression)) {
         return PROCESS_CONTINUE;
      }

      final IASTIdExpression idExpr = (IASTIdExpression) functionNameExpression;
      final IASTName name = idExpr.getName();

      if (!hasTemplateParamType(name)) {
         return PROCESS_CONTINUE;
      }

      addToMissingCtors(createInitializer(funCall));
      return PROCESS_SKIP;
   }

   private static ICPPASTConstructorInitializer createInitializer(final ICPPASTFunctionCallExpression call) {
      final IASTInitializerClause[] arguments = call.getArguments();
      final IASTInitializerClause[] clauses = new IASTInitializerClause[arguments.length];

      for (int i = 0; i < arguments.length; i++) {
         clauses[i] = arguments[i].copy();
      }

      return nodeFactory.newConstructorInitializer(clauses);
   }

   private boolean hasTemplateParamType(final IASTName name) {
      return getType(name) instanceof ICPPTemplateParameter && name.toString().equals(getTemplateParamName());
   }

   private static IType getType(final IASTName name) {
      final IBinding binding = name.resolveBinding();

      if (binding instanceof ICPPVariable) {
         final ICPPVariable var = (ICPPVariable) binding;
         return var.getType();
      } else if (binding instanceof IType) {
         return (IType) binding;
      }

      return null;
   }

   private Constructor createCtorWith(final IASTInitializer initializer) {
      final IASTName constructorName = nodeFactory.newName(getTestDoubleName().toCharArray());
      final IASTIdExpression idExpr = nodeFactory.newIdExpression(constructorName);
      final Collection<IASTInitializerClause> arguments = getArguments(initializer);
      final ICPPASTFunctionCallExpression call = nodeFactory.newFunctionCallExpression(idExpr, arguments.toArray(new IASTInitializerClause[arguments
               .size()]));
      call.setParent(getParent(initializer));
      return new Constructor(call);
   }

   private static Collection<IASTInitializerClause> getArguments(final IASTInitializer initializer) {
      if (initializer instanceof ICPPASTConstructorInitializer) {
         final CtorArgumentsCopier h = new CtorArgumentsCopier((ICPPASTConstructorInitializer) initializer);
         return h.getArguments();
      }

      return new ArrayList<>();
   }

   private static IASTNode getParent(final IASTNode ctor) {
      final ICPPASTFunctionDefinition parentFunction = ASTUtil.getAncestorOfType(ctor, ICPPASTFunctionDefinition.class);

      if (parentFunction == null) {
         return ASTUtil.getAncestorOfType(ctor, ICPPASTCompositeTypeSpecifier.class);
      }

      return parentFunction;
   }
}
