package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


@SuppressWarnings("restriction")
enum ArgumentPassByStrategy {

   asPointer {

   @Override
   public void adaptArguments(IASTName problemArg, String nameOfNewInstance, ASTRewrite rewriter) {
      ICPPASTConstructorInitializer ctorInit = AstUtil.getAncestorOfType(problemArg, ICPPASTConstructorInitializer.class);
      ICPPASTFunctionCallExpression funCall = AstUtil.getAncestorOfType(problemArg, ICPPASTFunctionCallExpression.class);

      if (ctorInit != null) {
         adaptCtorArguments(nameOfNewInstance, rewriter, ctorInit);
      } else if (funCall != null) {
         adaptFunCallArguments(nameOfNewInstance, rewriter, funCall);
      }
   }
   },
   asReference {

   @Override
   public void adaptArguments(IASTName problemArgument, String nameOfNewInstance, ASTRewrite rewriter) {
      // no argument adaption necessary when passed by reference
   }
   };

   private static final CPPNodeFactory                      nodeFactory    = CPPNodeFactory.getDefault();
   private static final Map<String, ArgumentPassByStrategy> STRING_TO_ENUM = unorderedMap();

   static {
      for (ArgumentPassByStrategy standard : values()) {
         STRING_TO_ENUM.put(standard.toString(), standard);
      }
   }

   public abstract void adaptArguments(IASTName problemArg, String nameOfNewInstance, ASTRewrite rewriter);

   private static void adaptFunCallArguments(String nameOfNewInstance, ASTRewrite rewriter, ICPPASTFunctionCallExpression funCall) {
      IASTInitializerClause[] argumentListWithAdaptedArgument = getArgumentListWithAdaptedArgument(funCall.getArguments(), nameOfNewInstance);
      ICPPASTFunctionCallExpression newFunCall = funCall.copy();
      newFunCall.setArguments(argumentListWithAdaptedArgument);
      rewriter.replace(funCall, newFunCall, null);
   }

   private static void adaptCtorArguments(String nameOfNewInstance, ASTRewrite rewriter, ICPPASTConstructorInitializer ctorInitializer) {
      IASTInitializerClause[] argumentListWithAdaptedArgument = getArgumentListWithAdaptedArgument(ctorInitializer.getArguments(), nameOfNewInstance);
      ICPPASTConstructorInitializer initializer = nodeFactory.newConstructorInitializer(argumentListWithAdaptedArgument);
      rewriter.replace(ctorInitializer, initializer, null);
   }

   private static IASTInitializerClause[] getArgumentListWithAdaptedArgument(IASTInitializerClause[] arguments, String nameOfNewInstance) {
      List<IASTInitializerClause> params = list();

      for (IASTInitializerClause arg : arguments) {
         if (arg instanceof IASTIdExpression) {
            IASTIdExpression idExpr = (IASTIdExpression) arg;

            if (idExpr.getName().toString().equals(nameOfNewInstance)) {
               params.add(takeAddressOfArg(idExpr));
            }
         } else {
            params.add(arg.copy());
         }
      }

      return params.toArray(new IASTInitializerClause[params.size()]);
   }

   private static ICPPASTUnaryExpression takeAddressOfArg(IASTIdExpression idExpr) {
      return nodeFactory.newUnaryExpression(IASTUnaryExpression.op_amper, idExpr.copy());
   }

   public static ArgumentPassByStrategy getStrategy(IType type) {
      if (type instanceof ICPPReferenceType) return asReference;
      else if (type instanceof IPointerType) return asPointer;

      throw new MockatorException("Pass by value is not possible with subtype polymorphism");
   }

   public static ArgumentPassByStrategy fromName(String name) {
      ArgumentPassByStrategy strategy = STRING_TO_ENUM.get(name);

      if (strategy == null) throw new MockatorException(String.format("Unknown pass by strategy '%s'", name));

      return strategy;
   }
}
