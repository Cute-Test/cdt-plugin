package ch.hsr.ifs.mockator.plugin.mockobject.expectations.reconcile;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpressionList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.BoostAssignInitializerCreator;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


class BoostVectorExpectationsReconciler extends AbstractExpectationsReconciler {

   public BoostVectorExpectationsReconciler(final ASTRewrite rewriter, final Collection<? extends TestDoubleMemFun> toAdd,
                                            final Collection<ExistingMemFunCallRegistration> toRemove, final CppStandard cppStd,
                                            final LinkedEditModeStrategy linkedEditMode) {
      super(rewriter, toAdd, toRemove, cppStd, linkedEditMode);
   }

   @Override
   public void reconcileExpectations(final IASTName expectations) {
      final IASTExpressionStatement boostAssign = AstUtil.getAncestorOfType(expectations, IASTExpressionStatement.class);

      if (boostAssign != null) {
         rewriteExistingExpectations(boostAssign);
      } else {
         addNewBoostAssignInitializer(expectations);
      }
   }

   private void addNewBoostAssignInitializer(final IASTName expectations) {
      final IASTExpressionStatement boostAssignInitializer = new BoostAssignInitializerCreator(callsToAdd, expectations.toString(), linkedEdit)
            .createBoostAssignInitializer();
      final ICPPASTFunctionDefinition testFun = AstUtil.getAncestorOfType(expectations, ICPPASTFunctionDefinition.class);
      final IASTExpressionStatement insertionPoint = getInsertionPointForBoostInitializer(expectations);
      rewriter.insertBefore(testFun.getBody(), insertionPoint, boostAssignInitializer, null);
   }

   private static IASTExpressionStatement getInsertionPointForBoostInitializer(final IASTName vector) {
      final IASTName[] references = vector.getTranslationUnit().getReferences(vector.resolveBinding());

      if (references.length > 0) {
         return AstUtil.getAncestorOfType(references[0], IASTExpressionStatement.class);
      }

      return null;
   }

   private void rewriteExistingExpectations(final IASTExpressionStatement vector) {
      final List<ICPPASTFunctionCallExpression> expectationsCalls = list();
      final IASTExpression expression = vector.getExpression();

      if (expression instanceof ICPPASTBinaryExpression) {
         collectStillRegisteredSingleCall(expression, expectationsCalls);
      } else if (expression instanceof IASTExpressionList) {
         collectStillRegisteredCalls(expression, expectationsCalls);
      }

      addMissingExpectations(expectationsCalls);
      rewriteExpectations(vector, expression, expectationsCalls);
   }

   private void rewriteExpectations(final IASTExpressionStatement expectationsVector, final IASTExpression expr,
         final List<ICPPASTFunctionCallExpression> expectationCalls) {
      if (expectationCalls.isEmpty()) {
         rewriter.remove(expectationsVector, null);
         return;
      }

      final ICPPASTBinaryExpression newBinExp = getBinaryExpr(expr).copy();
      newBinExp.setOperand2(expectationCalls.get(0));

      if (expectationCalls.size() > 1) {
         replaceExpressionList(expectationsVector, expectationCalls, newBinExp);
      } else {
         replaceExpressionStmt(expectationsVector, newBinExp);
      }
   }

   private void replaceExpressionList(final IASTExpressionStatement expectationsVector, final List<ICPPASTFunctionCallExpression> expectationCalls,
         final ICPPASTBinaryExpression newBinExp) {
      final ICPPASTExpressionList newExpressionList = nodeFactory.newExpressionList();
      newExpressionList.addExpression(newBinExp);

      for (int i = 1; i < expectationCalls.size(); i++) {
         newExpressionList.addExpression(expectationCalls.get(i));
      }
      replaceExpressionStmt(expectationsVector, newExpressionList);
   }

   private void replaceExpressionStmt(final IASTExpressionStatement expectationsVector, final IASTExpression toReplace) {
      final IASTExpressionStatement newCallVectorInit = nodeFactory.newExpressionStatement(toReplace);
      rewriter.replace(expectationsVector, newCallVectorInit, null);
   }

   private void addMissingExpectations(final List<ICPPASTFunctionCallExpression> expectationsCalls) {
      for (final TestDoubleMemFun toAdd : callsToAdd) {
         final ICPPASTLiteralExpression newCall = createFunSignatureLiteral(toAdd);
         final ICPPASTInitializerList newInitializerList = nodeFactory.newInitializerList();
         newInitializerList.addClause(newCall);
         addDefaultArgs(toAdd, newInitializerList);
         final IASTName callsName = nodeFactory.newName(MockatorConstants.CALL.toCharArray());
         final ICPPASTFunctionCallExpression newFunCall = nodeFactory.newFunctionCallExpression(nodeFactory.newIdExpression(callsName),
               newInitializerList.getClauses());
         expectationsCalls.add(newFunCall);
      }
   }

   private void addDefaultArgs(final TestDoubleMemFun memFun, final ICPPASTInitializerList newInitializerList) {
      for (final IASTInitializerClause initializer : memFun.createDefaultArguments(cppStd, linkedEdit)) {
         newInitializerList.addClause(initializer);
      }
   }

   private static ICPPASTLiteralExpression createFunSignatureLiteral(final TestDoubleMemFun memFun) {
      return nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal, StringUtil.quote(memFun.getFunctionSignature()));
   }

   private void collectStillRegisteredCalls(final IASTExpression expression, final List<ICPPASTFunctionCallExpression> expectations) {
      for (final IASTExpression expr : ((IASTExpressionList) expression).getExpressions()) {
         final ICPPASTFunctionCallExpression funCall = AstUtil.getChildOfType(expr, ICPPASTFunctionCallExpression.class);
         addCallIfStillRegistered(funCall, expectations);
      }
   }

   private void collectStillRegisteredSingleCall(final IASTExpression expr, final List<ICPPASTFunctionCallExpression> expectationsCalls) {
      final ICPPASTFunctionCallExpression funCall = (ICPPASTFunctionCallExpression) getBinaryExpr(expr).getOperand2();
      addCallIfStillRegistered(funCall, expectationsCalls);
   }

   private static ICPPASTBinaryExpression getBinaryExpr(final IASTExpression expression) {
      final ICPPASTBinaryExpression binExp = AstUtil.getChildOfType(expression, ICPPASTBinaryExpression.class);
      Assert.notNull(binExp, "Not a valid expectation vector assignment");
      return binExp;
   }

   private void addCallIfStillRegistered(final ICPPASTFunctionCallExpression fun, final List<ICPPASTFunctionCallExpression> calls) {
      Assert.isTrue(fun.getArguments().length > 0, "Not a valid call expectation");
      final IASTInitializerClause firstArg = fun.getArguments()[0];
      Assert.instanceOf(firstArg, IASTLiteralExpression.class, "Only literals allowed as 1st argument");
      final IASTLiteralExpression literal = (IASTLiteralExpression) firstArg;

      if (!isToBeRemoved(literal.toString())) {
         calls.add(fun.copy());
      }
   }
}
