package ch.hsr.ifs.cute.mockator.mockobject.expectations.finder;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.mockobject.expectations.MemFunCallExpectation;
import ch.hsr.ifs.cute.mockator.refsupport.utils.NodeContainer;


// calls expectedMock = {{"Mock()"}, {"foo() const"}};
class InitializerExpectationsFinder extends AbstractExpectationsFinder {

    public InitializerExpectationsFinder(final Collection<MemFunCallExpectation> callExpectations, final NodeContainer<IASTName> expectationVector,
                                         final IASTName expectationsVectorName) {
        super(callExpectations, expectationVector, expectationsVectorName);
    }

    @Override
    protected void collectExpectations(final IASTStatement expectationStmt) {
        ILTISException.Unless.assignableFrom("Should be called with an declaration statement", IASTDeclarationStatement.class, expectationStmt);
        final IASTDeclarationStatement declStmt = (IASTDeclarationStatement) expectationStmt;
        final IASTDeclaration declaration = declStmt.getDeclaration();

        if (!(declaration instanceof IASTSimpleDeclaration)) return;

        final IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
        final IASTDeclSpecifier declSpecifier = simpleDecl.getDeclSpecifier();

        if (!isCallsVector(declSpecifier)) return;

        final IASTName matchingName = getMatchingName(simpleDecl);

        if (matchingName == null) return;

        final ICPPASTInitializerList initializer = CPPVisitor.findChildWithType(declaration, ICPPASTInitializerList.class).orElse(null);

        if (initializer == null) return;

        expectationVector.setNode(matchingName);
        callExpectations.addAll(getCallExpectations(initializer));
    }

    private Collection<MemFunCallExpectation> getCallExpectations(final ICPPASTInitializerList initializer) {
        final Collection<MemFunCallExpectation> callExpectations = new LinkedHashSet<>();

        for (final IASTInitializerClause clause : initializer.getClauses()) {
            if (!(clause instanceof ICPPASTInitializerList)) {
                continue;
            }
            final IASTInitializerClause[] clauses = ((ICPPASTInitializerList) clause).getClauses();
            ILTISException.Unless.isTrue("Not a valid call initializer", clauses.length > 0);
            ILTISException.Unless.isTrue("Not a string literal", isStringLiteral(clauses[0]));
            final MemFunCallExpectation memFunCall = toMemberFunctionCall(clauses[0]);
            callExpectations.add(memFunCall);
        }

        return callExpectations;
    }

    private static boolean isCallsVector(final IASTDeclSpecifier declSpecifier) {
        if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier)) return false;

        final ICPPASTNamedTypeSpecifier namedTypeSpec = (ICPPASTNamedTypeSpecifier) declSpecifier;
        return isCallsTypedef(namedTypeSpec);
    }

    private static boolean isCallsTypedef(final ICPPASTNamedTypeSpecifier namedTypeSpec) {
        return namedTypeSpec.getName().toString().equals(MockatorConstants.CALLS);
    }

    private IASTName getMatchingName(final IASTSimpleDeclaration simpleDecl) {
        final IASTDeclarator[] declarators = simpleDecl.getDeclarators();

        if (declarators.length < 1) return null;

        final IASTName name = declarators[0].getName();

        if (matchesName(name)) return name;

        return null;
    }
}
