package ch.hsr.ifs.mockator.plugin.mockobject.expectations.reconcile;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.array;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;

import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;


class InitializerExpectationsReconciler extends AbstractExpectationsReconciler {

   public InitializerExpectationsReconciler(final ASTRewrite rewriter, final Collection<? extends TestDoubleMemFun> toAdd,
                                            final Collection<ExistingMemFunCallRegistration> toRemove, final CppStandard cppStd,
                                            final LinkedEditModeStrategy linkedEditMode) {
      super(rewriter, toAdd, toRemove, cppStd, linkedEditMode);
   }

   @Override
   public void reconcileExpectations(final IASTName expectationsVector) {
      final IASTEqualsInitializer eqInitializer = getEqualsInitializer(expectationsVector);
      final ICPPASTInitializerList callsInitializerList = getInitializerListFrom(eqInitializer);
      final ICPPASTInitializerList newCallsList = createCallsInitializerList();
      collectRegisteredCalls(callsInitializerList, newCallsList);
      collectNewCalls(newCallsList);
      replaceCallsInitializer(eqInitializer, newCallsList);
   }

   private static IASTEqualsInitializer getEqualsInitializer(final IASTName expVector) {
      final IASTDeclarationStatement declStmt = ASTUtil.getAncestorOfType(expVector, IASTDeclarationStatement.class);
      final IASTEqualsInitializer eqInitializer = ASTUtil.getChildOfType(declStmt, IASTEqualsInitializer.class);
      ILTISException.Unless.notNull(eqInitializer, "Not a valid call initialization");
      return eqInitializer;
   }

   private static ICPPASTInitializerList getInitializerListFrom(final IASTEqualsInitializer eqInitializer) {
      ILTISException.Unless.assignableFrom(ICPPASTInitializerList.class, eqInitializer.getInitializerClause(), "Initializer list expected");
      return (ICPPASTInitializerList) eqInitializer.getInitializerClause();
   }

   private static ICPPASTInitializerList createCallsInitializerList() {
      return nodeFactory.newInitializerList();
   }

   private void collectNewCalls(final ICPPASTInitializerList newCallsList) {
      for (final TestDoubleMemFun toAdd : callsToAdd) {
         final ICPPASTInitializerList call = createCallsInitializerList();
         call.addClause(nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal, StringUtil.quote(toAdd.getFunctionSignature())));

         for (final IASTInitializerClause initializer : toAdd.createDefaultArguments(cppStd, linkedEdit)) {
            call.addClause(initializer);
         }

         newCallsList.addClause(call);
      }
   }

   private void collectRegisteredCalls(final ICPPASTInitializerList callsInitializerList, final ICPPASTInitializerList newCallsList) {
      for (final IASTInitializerClause clause : callsInitializerList.getClauses()) {
         final IASTInitializerClause[] arguments = getArguments(clause);

         if (!isValidFunRegistrationVector(arguments)) {
            continue;
         }

         if (!isToBeRemoved(getFunctionSignature(arguments))) {
            newCallsList.addClause(clause.copy());
         }
      }
   }

   private static boolean isValidFunRegistrationVector(final IASTInitializerClause[] arguments) {
      return arguments.length > 0 && arguments[0] instanceof IASTLiteralExpression;
   }

   private static IASTInitializerClause[] getArguments(final IASTInitializerClause clause) {
      if (clause instanceof ICPPASTInitializerList) { return ((ICPPASTInitializerList) clause).getClauses(); }

      return array();
   }

   private static String getFunctionSignature(final IASTInitializerClause[] arguments) {
      return String.valueOf(((IASTLiteralExpression) arguments[0]).getValue());
   }

   private void replaceCallsInitializer(final IASTEqualsInitializer eqInitializer, final ICPPASTInitializerList newCallsList) {
      final IASTEqualsInitializer copy = nodeFactory.newEqualsInitializer(newCallsList);
      rewriter.replace(eqInitializer, copy, null);
   }
}
