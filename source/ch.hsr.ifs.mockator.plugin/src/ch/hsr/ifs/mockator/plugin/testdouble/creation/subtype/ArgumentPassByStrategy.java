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

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


@SuppressWarnings("restriction")
enum ArgumentPassByStrategy {

   asPointer {

      @Override
      public void adaptArguments(final IASTName problemArg, final String nameOfNewInstance, final ASTRewrite rewriter) {
         final ICPPASTConstructorInitializer ctorInit = AstUtil.getAncestorOfType(problemArg, ICPPASTConstructorInitializer.class);
         final ICPPASTFunctionCallExpression funCall = AstUtil.getAncestorOfType(problemArg, ICPPASTFunctionCallExpression.class);

         if (ctorInit != null) {
            adaptCtorArguments(nameOfNewInstance, rewriter, ctorInit);
         } else if (funCall != null) {
            adaptFunCallArguments(nameOfNewInstance, rewriter, funCall);
         }
      }
   },
   asReference {

      @Override
      public void adaptArguments(final IASTName problemArgument, final String nameOfNewInstance, final ASTRewrite rewriter) {
         // no argument adaption necessary when passed by reference
      }
   };

   private static final CPPNodeFactory                      nodeFactory    = CPPNodeFactory.getDefault();
   private static final Map<String, ArgumentPassByStrategy> STRING_TO_ENUM = unorderedMap();

   static {
      for (final ArgumentPassByStrategy standard : values()) {
         STRING_TO_ENUM.put(standard.toString(), standard);
      }
   }

   public abstract void adaptArguments(IASTName problemArg, String nameOfNewInstance, ASTRewrite rewriter);

   private static void adaptFunCallArguments(final String nameOfNewInstance, final ASTRewrite rewriter, final ICPPASTFunctionCallExpression funCall) {
      final IASTInitializerClause[] argumentListWithAdaptedArgument = getArgumentListWithAdaptedArgument(funCall.getArguments(), nameOfNewInstance);
      final ICPPASTFunctionCallExpression newFunCall = funCall.copy();
      newFunCall.setArguments(argumentListWithAdaptedArgument);
      rewriter.replace(funCall, newFunCall, null);
   }

   private static void adaptCtorArguments(final String nameOfNewInstance, final ASTRewrite rewriter,
         final ICPPASTConstructorInitializer ctorInitializer) {
      final IASTInitializerClause[] argumentListWithAdaptedArgument = getArgumentListWithAdaptedArgument(ctorInitializer.getArguments(),
            nameOfNewInstance);
      final ICPPASTConstructorInitializer initializer = nodeFactory.newConstructorInitializer(argumentListWithAdaptedArgument);
      rewriter.replace(ctorInitializer, initializer, null);
   }

   private static IASTInitializerClause[] getArgumentListWithAdaptedArgument(final IASTInitializerClause[] arguments,
         final String nameOfNewInstance) {
      final List<IASTInitializerClause> params = list();

      for (final IASTInitializerClause arg : arguments) {
         if (arg instanceof IASTIdExpression) {
            final IASTIdExpression idExpr = (IASTIdExpression) arg;

            if (idExpr.getName().toString().equals(nameOfNewInstance)) {
               params.add(takeAddressOfArg(idExpr));
            }
         } else {
            params.add(arg.copy());
         }
      }

      return params.toArray(new IASTInitializerClause[params.size()]);
   }

   private static ICPPASTUnaryExpression takeAddressOfArg(final IASTIdExpression idExpr) {
      return nodeFactory.newUnaryExpression(IASTUnaryExpression.op_amper, idExpr.copy());
   }

   public static ArgumentPassByStrategy getStrategy(final IType type) {
      if (type instanceof ICPPReferenceType) {
         return asReference;
      } else if (type instanceof IPointerType) {
         return asPointer;
      }

      throw new ILTISException("Pass by value is not possible with subtype polymorphism").rethrowUnchecked();
   }

   public static ArgumentPassByStrategy fromName(final String name) {
      final ArgumentPassByStrategy strategy = STRING_TO_ENUM.get(name);

      if (strategy == null) {
         throw new ILTISException(String.format("Unknown pass by strategy '%s'", name)).rethrowUnchecked();
      }

      return strategy;
   }
}
