package ch.hsr.ifs.mockator.plugin.mockobject.expectations.reconcile;

import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.finder.ExpectationsFinder;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;


public class ExpectationsReconciler {

   private final ASTRewrite                rewriter;
   private final IASTName                  expectationsVector;
   private final ICPPASTFunctionDefinition testFun;
   private final LinkedEditModeStrategy    linkedEditMode;
   private final CppStandard               cppStd;

   public ExpectationsReconciler(ASTRewrite rewriter, IASTName expectationsVector, ICPPASTFunctionDefinition testFun, CppStandard cppStd,
                                 LinkedEditModeStrategy linkedEditMode) {
      this.rewriter = rewriter;
      this.expectationsVector = expectationsVector;
      this.testFun = testFun;
      this.cppStd = cppStd;
      this.linkedEditMode = linkedEditMode;
   }

   public void consolidateExpectations(Collection<? extends TestDoubleMemFun> toAdd, Collection<ExistingMemFunCallRegistration> toRemove) {
      IASTName assignedExpectationsVector = getAssignExpectationsVector();
      getExpectationsReconciler(toAdd, toRemove).reconcileExpectations(assignedExpectationsVector);
   }

   private IASTName getAssignExpectationsVector() {
      ExpectationsFinder finder = new ExpectationsFinder(testFun);
      return _2(finder.getExpectations(expectationsVector));
   }

   private AbstractExpectationsReconciler getExpectationsReconciler(Collection<? extends TestDoubleMemFun> toAdd,
         Collection<ExistingMemFunCallRegistration> toRemove) {
      switch (cppStd) {
      case Cpp03Std:
         return new BoostVectorExpectationsReconciler(rewriter, toAdd, toRemove, cppStd, linkedEditMode);
      case Cpp11Std:
         return new InitializerExpectationsReconciler(rewriter, toAdd, toRemove, cppStd, linkedEditMode);
      default:
         throw new MockatorException("Unsupported C++ standard");
      }
   }
}
