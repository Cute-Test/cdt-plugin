package ch.hsr.ifs.cute.mockator.mockobject.expectations.qf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;
import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.infos.ConsistentExpectationsInfo;
import ch.hsr.ifs.cute.mockator.mockobject.asserteq.AssertEqualFinderVisitor;
import ch.hsr.ifs.cute.mockator.mockobject.asserteq.AssertKind.ExpectedActualPair;
import ch.hsr.ifs.cute.mockator.mockobject.expectations.reconcile.ExpectationsReconciler;
import ch.hsr.ifs.cute.mockator.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.cute.mockator.mockobject.support.allcalls.CallsVectorTypeVerifier;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.cute.mockator.testdouble.entities.ExistingTestDoubleMemFun;


class ConsistentExpectationsRefactoring extends MockatorRefactoring {

    private final ConsistentExpectationsInfo     info;
    private final LinkedEditModeStrategy         linkedEditMode;
    private final CppStandard                    cppStd;
    private final List<ExistingTestDoubleMemFun> expectationsToAdd;

    public ConsistentExpectationsRefactoring(final ICElement cElement, final Optional<ITextSelection> selection, final ICProject project,
                                             final ConsistentExpectationsInfo info, final CppStandard cppStd,
                                             final LinkedEditModeStrategy linkedEditMode) {
        super(cElement, selection);
        this.info = info;
        this.cppStd = cppStd;
        this.linkedEditMode = linkedEditMode;
        expectationsToAdd = new ArrayList<>();
    }

    @Override
    public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) throws CoreException {
        final RefactoringStatus status = super.checkInitialConditions(pm);
        final Optional<IASTName> expectationsVector = getSelectedName(getAST(tu, pm));

        if (!expectationsVector.isPresent()) {
            status.addFatalError("Not a valid name selected");
        } else if (!isOfCallsVectorType(expectationsVector)) {
            status.addFatalError("Not a valid calls vector selected");
        }
        return status;
    }

    private static boolean isOfCallsVectorType(final Optional<IASTName> expectationsVector) {
        return new CallsVectorTypeVerifier(expectationsVector.get()).hasCallsVectorType();
    }

    @Override
    protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
            OperationCanceledException {
        final IASTTranslationUnit ast = getAST(tu, pm);
        getSelectedName(ast).ifPresent((expectations) -> reconcileExpectations(collector, ast, expectations));
    }

    private void reconcileExpectations(final ModificationCollector collector, final IASTTranslationUnit ast, final IASTName expectationsVector) {
        final ASTRewrite rewriter = collector.rewriterForTranslationUnit(ast);
        collectExpectationsToAdd(ast, expectationsVector);
        consolidateExpectations(expectationsVector, rewriter);
    }

    private void consolidateExpectations(final IASTName expectationsVector, final ASTRewrite rewriter) {
        final ExpectationsReconciler reconciler = new ExpectationsReconciler(rewriter, expectationsVector, getTestFunction(expectationsVector),
                cppStd, linkedEditMode);
        reconciler.consolidateExpectations(expectationsToAdd, info.getExpectationsToRemove());
    }

    private void collectExpectationsToAdd(final IASTTranslationUnit ast, final IASTName expectationsVector) {
        for (final ExistingMemFunCallRegistration registration : collectRegisteredCalls(expectationsVector, ast)) {
            if (info.expectationsToAdd.contains(registration.getMemFunSignature())) {
                expectationsToAdd.add(registration.getExistingMemFun());
            }
        }
    }

    private Collection<ExistingMemFunCallRegistration> collectRegisteredCalls(final IASTName expectationsVector, final IASTTranslationUnit ast) {
        final RegistrationCandidatesFinder finder = new RegistrationCandidatesFinder(ast, cppStd);
        final ICPPASTFunctionDefinition testFunction = getTestFunction(expectationsVector);

        return getRegistrationVector(testFunction, expectationsVector).map(regVector -> finder.findCallRegistrations(regVector)).orElse(Lists.mutable
                .empty());
    }

    private static Optional<IASTName> getRegistrationVector(final ICPPASTFunctionDefinition testFun, final IASTName expectationsVector) {
        for (final ExpectedActualPair expectedActual : getAssertedCalls(testFun)) {
            if (equalsName(expectedActual.actual(), expectationsVector)) {
                return Optional.of(expectedActual.expected().getName());
            }

            if (equalsName(expectedActual.expected(), expectationsVector)) {
                return Optional.of(expectedActual.actual().getName());
            }
        }

        return Optional.empty();
    }

    private static boolean equalsName(final IASTIdExpression idExpr, final IASTName name) {
        return Arrays.equals(idExpr.getName().toCharArray(), name.toCharArray());
    }

    private static Collection<ExpectedActualPair> getAssertedCalls(final IASTFunctionDefinition function) {
        final AssertEqualFinderVisitor visitor = new AssertEqualFinderVisitor(Optional.empty());
        function.accept(visitor);
        return visitor.getExpectedActual();
    }

    private static ICPPASTFunctionDefinition getTestFunction(final IASTName expectationsVector) {
        return CPPVisitor.findAncestorWithType(expectationsVector, ICPPASTFunctionDefinition.class).orElse(null);
    }

    @Override
    public String getDescription() {
        return I18N.ConsistentExpectationsRefactoringDesc;
    }

    Collection<ExistingTestDoubleMemFun> getExpectationsToAdd() {
        return expectationsToAdd;
    }
}
