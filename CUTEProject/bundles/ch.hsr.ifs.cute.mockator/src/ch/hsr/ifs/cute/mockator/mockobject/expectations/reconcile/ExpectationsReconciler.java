package ch.hsr.ifs.cute.mockator.mockobject.expectations.reconcile;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.cute.mockator.mockobject.expectations.finder.ExpectationsFinder;
import ch.hsr.ifs.cute.mockator.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;


public class ExpectationsReconciler {

    private final ASTRewrite                rewriter;
    private final IASTName                  expectationsVector;
    private final ICPPASTFunctionDefinition testFun;
    private final LinkedEditModeStrategy    linkedEditMode;
    private final CppStandard               cppStd;

    public ExpectationsReconciler(final ASTRewrite rewriter, final IASTName expectationsVector, final ICPPASTFunctionDefinition testFun,
                                  final CppStandard cppStd, final LinkedEditModeStrategy linkedEditMode) {
        this.rewriter = rewriter;
        this.expectationsVector = expectationsVector;
        this.testFun = testFun;
        this.cppStd = cppStd;
        this.linkedEditMode = linkedEditMode;
    }

    public void consolidateExpectations(final Collection<? extends TestDoubleMemFun> toAdd,
            final Collection<ExistingMemFunCallRegistration> toRemove) {
        final IASTName assignedExpectationsVector = getAssignExpectationsVector();
        getExpectationsReconciler(toAdd, toRemove).reconcileExpectations(assignedExpectationsVector);
    }

    private IASTName getAssignExpectationsVector() {
        final ExpectationsFinder finder = new ExpectationsFinder(testFun);
        return finder.getExpectations(expectationsVector).getAssignExpectationsVector();
    }

    private AbstractExpectationsReconciler getExpectationsReconciler(final Collection<? extends TestDoubleMemFun> toAdd,
            final Collection<ExistingMemFunCallRegistration> toRemove) {
        switch (cppStd) {
        case Cpp03Std:
            return new BoostVectorExpectationsReconciler(rewriter, toAdd, toRemove, cppStd, linkedEditMode);
        case Cpp11Std:
            return new InitializerExpectationsReconciler(rewriter, toAdd, toRemove, cppStd, linkedEditMode);
        default:
            throw new ILTISException("Unsupported C++ standard").rethrowUnchecked();
        }
    }
}
