package ch.hsr.ifs.mockator.plugin.mockobject.expectations;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.CALL;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.tail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpressionList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;


public class BoostAssignInitializerCreator {

   private static final ICPPNodeFactory                 nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final Collection<? extends TestDoubleMemFun> memFuns;
   private final String                                 expectationsName;
   private final LinkedEditModeStrategy                 linkedEditStrategy;

   public BoostAssignInitializerCreator(final Collection<? extends TestDoubleMemFun> memFuns, final String expectationsName,
                                        final LinkedEditModeStrategy linkedEditStrategy) {
      this.memFuns = memFuns;
      this.expectationsName = expectationsName;
      this.linkedEditStrategy = linkedEditStrategy;
   }

   public IASTExpressionStatement createBoostAssignInitializer() {
      ILTISException.Unless.isFalse(memFuns.isEmpty(), "Should not be called with no fun signatures");
      final ICPPASTExpressionList expressionList = nodeFactory.newExpressionList();
      final IASTIdExpression vector = nodeFactory.newIdExpression(nodeFactory.newName(expectationsName.toCharArray()));
      final ICPPASTBinaryExpression expression = nodeFactory.newBinaryExpression(IASTBinaryExpression.op_plusAssign, vector, createNextCall(head(
               memFuns).get(), linkedEditStrategy));
      expressionList.addExpression(expression);
      addAllSignatures(expressionList, tail(memFuns));
      return nodeFactory.newExpressionStatement(expressionList);
   }

   private void addAllSignatures(final ICPPASTExpressionList expressions, final Collection<? extends TestDoubleMemFun> signatures) {
      for (final TestDoubleMemFun s : signatures) {
         expressions.addExpression(createNextCall(s, linkedEditStrategy));
      }
   }

   private static ICPPASTFunctionCallExpression createNextCall(final TestDoubleMemFun memFun, final LinkedEditModeStrategy edit) {
      final IASTIdExpression ctorCall = nodeFactory.newIdExpression(nodeFactory.newName(CALL.toCharArray()));
      final IASTInitializerClause[] funArgs = getCallExpectations(memFun, edit).toArray(new IASTInitializerClause[] {});
      return nodeFactory.newFunctionCallExpression(ctorCall, funArgs);
   }

   private static List<IASTInitializerClause> getCallExpectations(final TestDoubleMemFun memFun, final LinkedEditModeStrategy edit) {
      final List<IASTInitializerClause> clauses = new ArrayList<>();
      clauses.add(createSignatureLiteral(memFun.getFunctionSignature()));
      clauses.addAll(memFun.createDefaultArguments(CppStandard.Cpp03Std, edit));
      return clauses;
   }

   private static ICPPASTLiteralExpression createSignatureLiteral(final String signature) {
      return nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal, StringUtil.quote(signature));
   }
}
