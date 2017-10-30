package ch.hsr.ifs.mockator.plugin.mockobject.asserteq;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;
import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.filter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.CallsVectorTypeVerifier;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


public enum AssertKind {
   // assert(expectedMock == callsMock[1]);
   cassert(MockatorConstants.C_ASSERT_EQUAL, false) {

      @Override
   protected Optional<ExpectedActualPair> getExpectedActual(final IASTStatement stmt) {
         final ICPPASTBinaryExpression startingNode = AstUtil.getChildOfType(stmt, ICPPASTBinaryExpression.class);
         return ExpectedActualPair.from(collectExpectedActual(startingNode));
      }
   },
   // ASSERT_EQUAL(expectedMock, callsMock[1]);
   cute_equals(MockatorConstants.CUTE_ASSERT_EQUAL, true) {

      @Override
   protected Optional<ExpectedActualPair> getExpectedActual(final IASTStatement stmt) {
         final ICPPASTFunctionCallExpression startingNode = AstUtil.getChildOfType(stmt, ICPPASTFunctionCallExpression.class);
         return ExpectedActualPair.from(collectExpectedActual(startingNode));
      }
   },
   // ASSERT_ANY_ORDER(expectedMock, callsMock[1]);
   any_order(MockatorConstants.ASSERT_ANY_ORDER, true) {

      @Override
   protected Optional<ExpectedActualPair> getExpectedActual(final IASTStatement stmt) {
         final ICPPASTFunctionCallExpression startingNode = AstUtil.getChildOfType(stmt, ICPPASTFunctionCallExpression.class);
         final IASTInitializerClause[] arguments = startingNode.getArguments();

         if (arguments.length < 2) {
         return Optional.empty();
         }

         final List<IASTIdExpression> callVectors = list();
         callVectors.add(collectExpectedActual(arguments[0]).get(0));
         callVectors.add(collectExpectedActual(arguments[1]).get(0));
         return ExpectedActualPair.from(callVectors);
      }
   },
   // ASSERT_MATCHES(expectedMock, callsMock[1]);
   assert_matches(MockatorConstants.ASSERT_MATCHES, true) {

      @Override
   protected Optional<ExpectedActualPair> getExpectedActual(final IASTStatement stmt) {
         final ICPPASTFunctionCallExpression startingNode = AstUtil.getChildOfType(stmt, ICPPASTFunctionCallExpression.class);
         return ExpectedActualPair.from(collectExpectedActual(startingNode));
      }
   };

   private final String  code;
   private final boolean includeInProposals;

   private static final Map<String, AssertKind> STRING_TO_ENUM = unorderedMap();

   static {
      for (final AssertKind each : values()) {
         STRING_TO_ENUM.put(each.toString(), each);
      }
   }

   protected abstract Optional<ExpectedActualPair> getExpectedActual(IASTStatement stmt);

   private AssertKind(final String code, final boolean includeInProposals) {
      this.code = code;
      this.includeInProposals = includeInProposals;
   }

   @Override
   public String toString() {
      return code;
   }

   public static Optional<AssertKind> fromCode(final String code) {
      return Optional.of(STRING_TO_ENUM.get(code));
   }

   public static Collection<AssertKind> getAssertProposals() {
      return filter(values(), new F1<AssertKind, Boolean>() {

         @Override
         public Boolean apply(final AssertKind kind) {
            return kind.includeInProposals;
         }
      });
   }

   private static boolean hasCallsVectorType(final IASTIdExpression idExpr) {
      final CallsVectorTypeVerifier verifier = new CallsVectorTypeVerifier(idExpr);
      return verifier.isVectorOfCallsVector() || verifier.hasCallsVectorType();
   }

   private static List<IASTIdExpression> collectExpectedActual(final IASTNode startingNode) {
      final List<IASTIdExpression> callVectors = list();

      if (startingNode == null) {
         return callVectors;
      }

      startingNode.accept(new ASTVisitor() {

         {
            shouldVisitNames = true;
         }

         @Override
         public int visit(final IASTName name) {
            final IASTNode parent = name.getParent();

            if (!(parent instanceof IASTIdExpression)) {
               return PROCESS_SKIP;
            }

            final IASTIdExpression idExpr = (IASTIdExpression) parent;

            if (hasCallsVectorType(idExpr)) {
               callVectors.add(idExpr);
            }

            return PROCESS_SKIP;
         }
      });
      return callVectors;
   }

   public static class ExpectedActualPair extends Pair<IASTIdExpression, IASTIdExpression> {

      private ExpectedActualPair(final IASTIdExpression expected, final IASTIdExpression actual) {
         super(expected, actual);
      }

      private static Optional<ExpectedActualPair> from(final List<IASTIdExpression> actualExpectedIds) {
         if (actualExpectedIds.size() == 2) {
            return Optional.of(new ExpectedActualPair(actualExpectedIds.get(0), actualExpectedIds.get(1)));
         } else {
            return Optional.empty();
         }
      }
   }
}
