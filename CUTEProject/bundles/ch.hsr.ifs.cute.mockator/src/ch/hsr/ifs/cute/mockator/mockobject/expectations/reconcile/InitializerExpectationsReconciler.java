package ch.hsr.ifs.cute.mockator.mockobject.expectations.reconcile;

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
import ch.hsr.ifs.iltis.core.resources.StringUtil;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.cute.mockator.mockobject.registrations.finder.ExistingMemFunCallRegistration;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;


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
        final IASTDeclarationStatement declStmt = CPPVisitor.findAncestorWithType(expVector, IASTDeclarationStatement.class).orElse(null);
        final IASTEqualsInitializer eqInitializer = CPPVisitor.findChildWithType(declStmt, IASTEqualsInitializer.class).orElse(null);
        ILTISException.Unless.notNull("Not a valid call initialization", eqInitializer);
        return eqInitializer;
    }

    private static ICPPASTInitializerList getInitializerListFrom(final IASTEqualsInitializer eqInitializer) {
        ILTISException.Unless.assignableFrom("Initializer list expected", ICPPASTInitializerList.class, eqInitializer.getInitializerClause());
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
        if (clause instanceof ICPPASTInitializerList) {
            return ((ICPPASTInitializerList) clause).getClauses();
        }

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
