package ch.hsr.ifs.cute.mockator.mockobject.expectations.reconcile;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.cute.mockator.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;


abstract class AbstractExpectationsReconciler {

   protected static final ICPPNodeFactory                   nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   protected final ASTRewrite                               rewriter;
   protected final Collection<? extends TestDoubleMemFun>   callsToAdd;
   protected final CppStandard                              cppStd;
   protected final LinkedEditModeStrategy                   linkedEdit;
   private final Collection<ExistingMemFunCallRegistration> callsToRemove;

   public AbstractExpectationsReconciler(final ASTRewrite rewriter, final Collection<? extends TestDoubleMemFun> toAdd,
                                         final Collection<ExistingMemFunCallRegistration> toRemove, final CppStandard cppStd,
                                         final LinkedEditModeStrategy linkedEditMode) {
      this.rewriter = rewriter;
      callsToAdd = toAdd;
      callsToRemove = toRemove;
      this.cppStd = cppStd;
      linkedEdit = linkedEditMode;
   }

   public abstract void reconcileExpectations(IASTName expectationsVector);

   protected boolean isToBeRemoved(final String funSignature) {
      return callsToRemove.contains(new ExistingMemFunCallRegistration(funSignature));
   }
}
