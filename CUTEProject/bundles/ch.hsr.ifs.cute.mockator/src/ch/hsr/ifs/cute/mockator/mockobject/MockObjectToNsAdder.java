package ch.hsr.ifs.cute.mockator.mockobject;

import static ch.hsr.ifs.cute.mockator.MockatorConstants.INIT_MOCKATOR;
import static ch.hsr.ifs.cute.mockator.MockatorConstants.L_PARENTHESIS;
import static ch.hsr.ifs.cute.mockator.MockatorConstants.R_PARENTHESIS;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.cute.mockator.MockatorConstants;
import ch.hsr.ifs.cute.mockator.mockobject.support.allcalls.AllCallsVectorCreator;
import ch.hsr.ifs.cute.mockator.mockobject.support.allcalls.AllCallsVectorCreator.CallsVectorParent;
import ch.hsr.ifs.cute.mockator.project.properties.CppStandard;


class MockObjectToNsAdder {

    private static final ICPPNodeFactory        nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
    private final CppStandard                   cppStd;
    private final ICPPASTCompositeTypeSpecifier testDoubleToMove;

    public MockObjectToNsAdder(final CppStandard cppStd, final ICPPASTCompositeTypeSpecifier testDoubleToMove) {
        this.cppStd = cppStd;
        this.testDoubleToMove = testDoubleToMove;
    }

    public void addTestDoubleToNs(final IASTSimpleDeclaration testDouble, final ICPPASTNamespaceDefinition parentNs) {
        final ICPPASTCompositeTypeSpecifier testDoubleClass = getTestDoubleClass(testDouble);
        final ICPPASTNamespaceDefinition mockObjectNs = createMockObjectNs(testDoubleClass);
        mockObjectNs.addDeclaration(createMockatorInitCall());
        final IASTSimpleDeclaration allCallsVector = createAllCallsVector();
        mockObjectNs.addDeclaration(allCallsVector);
        mockObjectNs.addDeclaration(testDouble);
        parentNs.addDeclaration(mockObjectNs);
    }

    private static ICPPASTCompositeTypeSpecifier getTestDoubleClass(final IASTSimpleDeclaration simpleDecl) {
        return CPPVisitor.findChildWithType(simpleDecl, ICPPASTCompositeTypeSpecifier.class).orElse(null);
    }

    private IASTSimpleDeclaration createAllCallsVector() {
        final MockObject mockObject = new MockObject(testDoubleToMove);
        final AllCallsVectorCreator creator = new AllCallsVectorCreator(mockObject.getNameOfAllCallsVector(), CallsVectorParent.Namespace, cppStd);
        return creator.getAllCallsVector();
    }

    private static IASTSimpleDeclaration createMockatorInitCall() {
        final IASTName initMockator = nodeFactory.newName((INIT_MOCKATOR + L_PARENTHESIS + R_PARENTHESIS).toCharArray());
        return nodeFactory.newSimpleDeclaration(nodeFactory.newTypedefNameSpecifier(initMockator));
    }

    private static ICPPASTNamespaceDefinition createMockObjectNs(final ICPPASTCompositeTypeSpecifier testDouble) {
        final IASTName nsName = nodeFactory.newName(getMockObjectNsName(testDouble).toCharArray());
        return nodeFactory.newNamespaceDefinition(nsName);
    }

    private static String getMockObjectNsName(final ICPPASTCompositeTypeSpecifier testDouble) {
        return testDouble.getName().toString() + MockatorConstants.NS_SUFFIX;
    }
}
