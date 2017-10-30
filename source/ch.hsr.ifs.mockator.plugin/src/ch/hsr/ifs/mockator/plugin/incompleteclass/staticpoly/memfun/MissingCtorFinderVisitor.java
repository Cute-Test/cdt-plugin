package ch.hsr.ifs.mockator.plugin.incompleteclass.staticpoly.memfun;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Collection;
import java.util.Map;

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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.incompleteclass.StaticPolyMissingMemFun;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.CtorArgumentsCopier;


@SuppressWarnings("restriction")
class MissingCtorFinderVisitor extends MissingMemFunVisitor {

   private static final CPPNodeFactory                           nodeFactory = CPPNodeFactory.getDefault();
   private final Collection<Constructor>                         missingCtors;
   private final Map<String, ICPPASTConstructorChainInitializer> initialisers;

   {
      shouldVisitDeclarations = true;
      shouldVisitExpressions = true;
      shouldVisitInitializers = true;
   }

   public MissingCtorFinderVisitor(ICPPASTCompositeTypeSpecifier testDouble, ICPPASTTemplateParameter templateParam, ICPPASTTemplateDeclaration sut) {
      super(testDouble, templateParam, sut);
      missingCtors = orderPreservingSet();
      initialisers = unorderedMap();
   }

   @Override
   public Collection<? extends StaticPolyMissingMemFun> getMissingMemberFunctions() {
      addDefaultCtorIfNecessary();

      if (isSolelyDefaultCtor()) return list();

      return missingCtors;
   }

   private void addDefaultCtorIfNecessary() {
      if (hasDefaultInitTemplateParamMember()) {
         missingCtors.add(createCtorWith(nodeFactory.newInitializerList()));
      }
   }

   private boolean hasDefaultInitTemplateParamMember() {
      ICPPASTCompositeTypeSpecifier sutClass = AstUtil.getChildOfType(sut, ICPPASTCompositeTypeSpecifier.class);

      if (sutClass == null) return false;

      for (IASTDeclaration member : sutClass.getMembers()) {
         if (!(member instanceof IASTSimpleDeclaration)) {
            continue;
         }

         IASTDeclarator[] declarators = ((IASTSimpleDeclaration) member).getDeclarators();

         if (!(declarators.length == 1 && declarators[0] instanceof ICPPASTDeclarator)) {
            continue;
         }

         IASTName name = declarators[0].getName();

         if (!resolvesToTemplateParam(getType(name))) {
            continue;
         }

         if (isTypeNotInitialized(name)) return true;
      }

      return false;
   }

   private boolean isTypeNotInitialized(IASTName name) {
      ICPPASTConstructorChainInitializer chainInitializer = initialisers.get(name.toString());

      if (chainInitializer == null) return true;

      IASTInitializer initializer = chainInitializer.getInitializer();
      return !(initializer instanceof ICPPASTConstructorInitializer) || hasEmptyInitializer(initializer);
   }

   private static boolean hasEmptyInitializer(IASTInitializer initializer) {
      return ((ICPPASTConstructorInitializer) initializer).getArguments().length == 0;
   }

   private boolean isSolelyDefaultCtor() {
      return missingCtors.size() == 1 && head(missingCtors).get().isDefaultConstructor();
   }

   @Override
   public int visit(IASTInitializer initializer) {
      if (!(initializer instanceof ICPPASTConstructorInitializer)) return PROCESS_CONTINUE;

      ICPPASTConstructorInitializer ctorInitializer = (ICPPASTConstructorInitializer) initializer;

      if (AstUtil.isPartOf(ctorInitializer, ICPPASTConstructorChainInitializer.class)) return handleCtorInitializer(initializer, ctorInitializer);
      else if (AstUtil.isPartOf(ctorInitializer, ICPPASTNewExpression.class)) return handleNewExpression(ctorInitializer);
      else if (AstUtil.isPartOf(ctorInitializer, ICPPASTDeclarator.class)) return handleSimpleDecl(ctorInitializer);

      return PROCESS_CONTINUE;
   }

   private int handleSimpleDecl(ICPPASTConstructorInitializer ctorInitializer) {
      ICPPASTDeclarator declarator = AstUtil.getAncestorOfType(ctorInitializer, ICPPASTDeclarator.class);

      if (resolvesToTemplateParam(getType(declarator.getName()))) {
         addToMissingCtors(declarator.getInitializer());
         return PROCESS_SKIP;
      }

      return PROCESS_CONTINUE;
   }

   private int handleNewExpression(ICPPASTConstructorInitializer ctorInitializer) {
      ICPPASTNewExpression newExpr = AstUtil.getAncestorOfType(ctorInitializer, ICPPASTNewExpression.class);

      if (resolvesToTemplateParam(newExpr.getExpressionType())) {
         addToMissingCtors(newExpr.getInitializer());
         return PROCESS_SKIP;
      }

      return PROCESS_CONTINUE;
   }

   private int handleCtorInitializer(IASTInitializer initializer, ICPPASTConstructorInitializer ctorInitializer) {
      ICPPASTConstructorChainInitializer ctor = AstUtil.getAncestorOfType(ctorInitializer, ICPPASTConstructorChainInitializer.class);
      IASTName memberInitializerId = ctor.getMemberInitializerId();

      if (resolvesToTemplateParam(getType(memberInitializerId))) {
         initialisers.put(memberInitializerId.toString(), ctor);
         addToMissingCtors(initializer);
         return PROCESS_SKIP;
      }

      return PROCESS_CONTINUE;
   }

   @Override
   public int visit(IASTDeclaration decl) {
      if (!(decl instanceof IASTSimpleDeclaration && AstUtil.isPartOf(decl, ICPPASTFunctionDefinition.class))) return PROCESS_CONTINUE;

      ICPPASTConstructorInitializer ctorInit = AstUtil.getChildOfType(decl, ICPPASTConstructorInitializer.class);
      ICPPASTFunctionCallExpression funCall = AstUtil.getChildOfType(decl, ICPPASTFunctionCallExpression.class);

      if (ctorInit != null || funCall != null) return PROCESS_CONTINUE;

      IASTDeclarator[] declarators = ((IASTSimpleDeclaration) decl).getDeclarators();

      if (declarators.length == 0) return PROCESS_CONTINUE;

      if (resolvesToTemplateParam(getType(declarators[0].getName()))) {
         addToMissingCtors(nodeFactory.newInitializerList());
         return PROCESS_SKIP;
      }

      return PROCESS_CONTINUE;
   }

   private void addToMissingCtors(IASTInitializer initializerToUse) {
      missingCtors.add(createCtorWith(initializerToUse));
   }

   @Override
   public int visit(IASTExpression expression) {
      if (!(expression instanceof ICPPASTFunctionCallExpression)) return PROCESS_CONTINUE;

      ICPPASTFunctionCallExpression funCall = (ICPPASTFunctionCallExpression) expression;
      IASTExpression functionNameExpression = funCall.getFunctionNameExpression();

      if (!(functionNameExpression instanceof IASTIdExpression)) return PROCESS_CONTINUE;

      IASTIdExpression idExpr = (IASTIdExpression) functionNameExpression;
      IASTName name = idExpr.getName();

      if (!hasTemplateParamType(name)) return PROCESS_CONTINUE;

      addToMissingCtors(createInitializer(funCall));
      return PROCESS_SKIP;
   }

   private static ICPPASTConstructorInitializer createInitializer(ICPPASTFunctionCallExpression call) {
      IASTInitializerClause[] arguments = call.getArguments();
      IASTInitializerClause[] clauses = new IASTInitializerClause[arguments.length];

      for (int i = 0; i < arguments.length; i++) {
         clauses[i] = arguments[i].copy();
      }

      return nodeFactory.newConstructorInitializer(clauses);
   }

   private boolean hasTemplateParamType(IASTName name) {
      return getType(name) instanceof ICPPTemplateParameter && name.toString().equals(getTemplateParamName());
   }

   private static IType getType(IASTName name) {
      IBinding binding = name.resolveBinding();

      if (binding instanceof ICPPVariable) {
         ICPPVariable var = (ICPPVariable) binding;
         return var.getType();
      } else if (binding instanceof IType) return (IType) binding;

      return null;
   }

   private Constructor createCtorWith(IASTInitializer initializer) {
      IASTName constructorName = nodeFactory.newName(getTestDoubleName().toCharArray());
      IASTIdExpression idExpr = nodeFactory.newIdExpression(constructorName);
      Collection<IASTInitializerClause> arguments = getArguments(initializer);
      ICPPASTFunctionCallExpression call = nodeFactory.newFunctionCallExpression(idExpr, arguments.toArray(new IASTInitializerClause[arguments
            .size()]));
      call.setParent(getParent(initializer));
      return new Constructor(call);
   }

   private static Collection<IASTInitializerClause> getArguments(IASTInitializer initializer) {
      if (initializer instanceof ICPPASTConstructorInitializer) {
         CtorArgumentsCopier h = new CtorArgumentsCopier((ICPPASTConstructorInitializer) initializer);
         return h.getArguments();
      }

      return list();
   }

   private static IASTNode getParent(IASTNode ctor) {
      ICPPASTFunctionDefinition parentFunction = AstUtil.getAncestorOfType(ctor, ICPPASTFunctionDefinition.class);

      if (parentFunction == null) return AstUtil.getAncestorOfType(ctor, ICPPASTCompositeTypeSpecifier.class);

      return parentFunction;
   }
}
