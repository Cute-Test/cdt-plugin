package ch.hsr.ifs.mockator.plugin.mockobject.expectations;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.CALL;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.tail;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpressionList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;


@SuppressWarnings("restriction")
public class BoostAssignInitializerCreator {

   private static final CPPNodeFactory                  nodeFactory = CPPNodeFactory.getDefault();
   private final Collection<? extends TestDoubleMemFun> memFuns;
   private final String                                 expectationsName;
   private final LinkedEditModeStrategy                 linkedEditStrategy;

   public BoostAssignInitializerCreator(Collection<? extends TestDoubleMemFun> memFuns, String expectationsName,
                                        LinkedEditModeStrategy linkedEditStrategy) {
      this.memFuns = memFuns;
      this.expectationsName = expectationsName;
      this.linkedEditStrategy = linkedEditStrategy;
   }

   public IASTExpressionStatement createBoostAssignInitializer() {
      Assert.isFalse(memFuns.isEmpty(), "Should not be called with no fun signatures");
      ICPPASTExpressionList expressionList = nodeFactory.newExpressionList();
      IASTIdExpression vector = nodeFactory.newIdExpression(nodeFactory.newName(expectationsName.toCharArray()));
      ICPPASTBinaryExpression expression = nodeFactory.newBinaryExpression(IASTBinaryExpression.op_plusAssign, vector, createNextCall(head(memFuns)
            .get(), linkedEditStrategy));
      expressionList.addExpression(expression);
      addAllSignatures(expressionList, tail(memFuns));
      return nodeFactory.newExpressionStatement(expressionList);
   }

   private void addAllSignatures(ICPPASTExpressionList expressions, Collection<? extends TestDoubleMemFun> signatures) {
      for (TestDoubleMemFun s : signatures) {
         expressions.addExpression(createNextCall(s, linkedEditStrategy));
      }
   }

   private static ICPPASTFunctionCallExpression createNextCall(TestDoubleMemFun memFun, LinkedEditModeStrategy edit) {
      IASTIdExpression ctorCall = nodeFactory.newIdExpression(nodeFactory.newName(CALL.toCharArray()));
      IASTInitializerClause[] funArgs = getCallExpectations(memFun, edit).toArray(new IASTInitializerClause[] {});
      return nodeFactory.newFunctionCallExpression(ctorCall, funArgs);
   }

   private static List<IASTInitializerClause> getCallExpectations(TestDoubleMemFun memFun, LinkedEditModeStrategy edit) {
      List<IASTInitializerClause> clauses = list();
      clauses.add(createSignatureLiteral(memFun.getFunctionSignature()));
      clauses.addAll(memFun.createDefaultArguments(CppStandard.Cpp03Std, edit));
      return clauses;
   }

   private static ICPPASTLiteralExpression createSignatureLiteral(String signature) {
      return nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal, StringUtil.quote(signature));
   }
}
