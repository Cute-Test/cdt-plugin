package ch.hsr.ifs.cute.mockator.mockobject.expectations.vector;

import static ch.hsr.ifs.cute.mockator.MockatorConstants.CALLS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.cute.mockator.mockobject.expectations.BoostAssignInitializerCreator;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.cute.mockator.refsupport.finder.NameFinder;


public class ExpectationsCpp03Strategy implements ExpectationsCppStdStrategy {

    private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

    @Override
    public List<IASTStatement> createExpectationsVector(final Collection<? extends TestDoubleMemFun> memFuns, final String newExpectationsName,
            final ICPPASTFunctionDefinition testFunction, final Optional<IASTName> expectationsVector, final LinkedEditModeStrategy linkedEdit) {
        final List<IASTStatement> expectations = new ArrayList<>();

        if (!expectationsVector.isPresent()) {
            expectations.add(createExpectationVectorDeclStmt(newExpectationsName));
            expectations.add(createBoostAssignInitializer(memFuns, newExpectationsName, linkedEdit));
        } else if (!hasBoostAssignInitializer(testFunction, expectationsVector.get())) {
            expectations.add(createBoostAssignInitializer(memFuns, expectationsVector.get().toString(), linkedEdit));
        }

        return expectations;
    }

    private static IASTExpressionStatement createBoostAssignInitializer(final Collection<? extends TestDoubleMemFun> memFuns, final String vectorName,
            final LinkedEditModeStrategy linkedEdit) {
        final BoostAssignInitializerCreator creator = new BoostAssignInitializerCreator(memFuns, vectorName, linkedEdit);
        return creator.createBoostAssignInitializer();
    }

    private static boolean hasBoostAssignInitializer(final ICPPASTFunctionDefinition testFun, final IASTName expVector) {
        return new NameFinder(testFun).getNameMatchingCriteria((name) -> {
            final ICPPASTBinaryExpression binExp = CPPVisitor.findAncestorWithType(name, ICPPASTBinaryExpression.class).orElse(null);
            return name.toString().equals(expVector.toString()) && binExp != null && binExp.getOperator() == IASTBinaryExpression.op_plusAssign;
        }).isPresent();
    }

    private static IASTDeclarationStatement createExpectationVectorDeclStmt(final String expectationsName) {
        final IASTName expectedName = nodeFactory.newName(expectationsName.toCharArray());
        final ICPPASTDeclarator declarator = nodeFactory.newDeclarator(expectedName);
        final IASTName callsName = nodeFactory.newName(CALLS.toCharArray());
        final ICPPASTNamedTypeSpecifier namedType = nodeFactory.newTypedefNameSpecifier(callsName);
        final IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(namedType);
        declaration.addDeclarator(declarator);
        return nodeFactory.newDeclarationStatement(declaration);
    }
}
