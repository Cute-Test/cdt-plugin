package ch.hsr.ifs.mockator.plugin.mockobject.expectations.reconcile;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


@SuppressWarnings("restriction")
class InitializerExpectationsReconciler extends AbstractExpectationsReconciler {

   public InitializerExpectationsReconciler(ASTRewrite rewriter, Collection<? extends TestDoubleMemFun> toAdd,
                                            Collection<ExistingMemFunCallRegistration> toRemove, CppStandard cppStd,
                                            LinkedEditModeStrategy linkedEditMode) {
      super(rewriter, toAdd, toRemove, cppStd, linkedEditMode);
   }

   @Override
   public void reconcileExpectations(IASTName expectationsVector) {
      IASTEqualsInitializer eqInitializer = getEqualsInitializer(expectationsVector);
      ICPPASTInitializerList callsInitializerList = getInitializerListFrom(eqInitializer);
      ICPPASTInitializerList newCallsList = createCallsInitializerList();
      collectRegisteredCalls(callsInitializerList, newCallsList);
      collectNewCalls(newCallsList);
      replaceCallsInitializer(eqInitializer, newCallsList);
   }

   private static IASTEqualsInitializer getEqualsInitializer(IASTName expVector) {
      IASTDeclarationStatement declStmt = AstUtil.getAncestorOfType(expVector, IASTDeclarationStatement.class);
      IASTEqualsInitializer eqInitializer = AstUtil.getChildOfType(declStmt, IASTEqualsInitializer.class);
      Assert.notNull(eqInitializer, "Not a valid call initialization");
      return eqInitializer;
   }

   private static ICPPASTInitializerList getInitializerListFrom(IASTEqualsInitializer eqInitializer) {
      Assert.instanceOf(eqInitializer.getInitializerClause(), ICPPASTInitializerList.class, "Initializer list expected");
      return (ICPPASTInitializerList) eqInitializer.getInitializerClause();
   }

   private static ICPPASTInitializerList createCallsInitializerList() {
      return nodeFactory.newInitializerList();
   }

   private void collectNewCalls(ICPPASTInitializerList newCallsList) {
      for (TestDoubleMemFun toAdd : callsToAdd) {
         ICPPASTInitializerList call = createCallsInitializerList();
         call.addClause(nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal, StringUtil.quote(toAdd.getFunctionSignature())));

         for (IASTInitializerClause initializer : toAdd.createDefaultArguments(cppStd, linkedEdit)) {
            call.addClause(initializer);
         }

         newCallsList.addClause(call);
      }
   }

   private void collectRegisteredCalls(ICPPASTInitializerList callsInitializerList, ICPPASTInitializerList newCallsList) {
      for (IASTInitializerClause clause : callsInitializerList.getClauses()) {
         IASTInitializerClause[] arguments = getArguments(clause);

         if (!isValidFunRegistrationVector(arguments)) {
            continue;
         }

         if (!isToBeRemoved(getFunctionSignature(arguments))) {
            newCallsList.addClause(clause.copy());
         }
      }
   }

   private static boolean isValidFunRegistrationVector(IASTInitializerClause[] arguments) {
      return arguments.length > 0 && arguments[0] instanceof IASTLiteralExpression;
   }

   private static IASTInitializerClause[] getArguments(IASTInitializerClause clause) {
      if (clause instanceof ICPPASTInitializerList) return ((ICPPASTInitializerList) clause).getClauses();

      return array();
   }

   private static String getFunctionSignature(IASTInitializerClause[] arguments) {
      return String.valueOf(((IASTLiteralExpression) arguments[0]).getValue());
   }

   private void replaceCallsInitializer(IASTEqualsInitializer eqInitializer, ICPPASTInitializerList newCallsList) {
      IASTEqualsInitializer copy = nodeFactory.newEqualsInitializer(newCallsList);
      rewriter.replace(eqInitializer, copy, null);
   }
}
