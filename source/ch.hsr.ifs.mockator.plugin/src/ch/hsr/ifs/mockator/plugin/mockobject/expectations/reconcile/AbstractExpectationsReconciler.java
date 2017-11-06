package ch.hsr.ifs.mockator.plugin.mockobject.expectations.reconcile;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;


@SuppressWarnings("restriction")
abstract class AbstractExpectationsReconciler {

   protected static final CPPNodeFactory                    nodeFactory = CPPNodeFactory.getDefault();
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
