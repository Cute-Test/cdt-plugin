package ch.hsr.ifs.mockator.plugin.mockobject;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.INIT_MOCKATOR;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.L_PARENTHESIS;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.R_PARENTHESIS;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.AllCallsVectorCreator;
import ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls.AllCallsVectorCreator.CallsVectorParent;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
class MockObjectToNsAdder {
  private static final ICPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private final CppStandard cppStd;
  private final ICPPASTCompositeTypeSpecifier testDoubleToMove;

  public MockObjectToNsAdder(CppStandard cppStd, ICPPASTCompositeTypeSpecifier testDoubleToMove) {
    this.cppStd = cppStd;
    this.testDoubleToMove = testDoubleToMove;
  }

  public void addTestDoubleToNs(IASTSimpleDeclaration testDouble,
      ICPPASTNamespaceDefinition parentNs) {
    ICPPASTCompositeTypeSpecifier testDoubleClass = getTestDoubleClass(testDouble);
    ICPPASTNamespaceDefinition mockObjectNs = createMockObjectNs(testDoubleClass);
    mockObjectNs.addDeclaration(createMockatorInitCall());
    IASTSimpleDeclaration allCallsVector = createAllCallsVector();
    mockObjectNs.addDeclaration(allCallsVector);
    mockObjectNs.addDeclaration(testDouble);
    parentNs.addDeclaration(mockObjectNs);
  }

  private static ICPPASTCompositeTypeSpecifier getTestDoubleClass(IASTSimpleDeclaration simpleDecl) {
    return AstUtil.getChildOfType(simpleDecl, ICPPASTCompositeTypeSpecifier.class);
  }

  private IASTSimpleDeclaration createAllCallsVector() {
    MockObject mockObject = new MockObject(testDoubleToMove);
    AllCallsVectorCreator creator =
        new AllCallsVectorCreator(mockObject.getNameOfAllCallsVector(),
            CallsVectorParent.Namespace, cppStd);
    return creator.getAllCallsVector();
  }

  private static IASTSimpleDeclaration createMockatorInitCall() {
    IASTName initMockator =
        nodeFactory.newName((INIT_MOCKATOR + L_PARENTHESIS + R_PARENTHESIS).toCharArray());
    return nodeFactory.newSimpleDeclaration(nodeFactory.newTypedefNameSpecifier(initMockator));
  }

  private static ICPPASTNamespaceDefinition createMockObjectNs(
      ICPPASTCompositeTypeSpecifier testDouble) {
    IASTName nsName = nodeFactory.newName(getMockObjectNsName(testDouble).toCharArray());
    return nodeFactory.newNamespaceDefinition(nsName);
  }

  private static String getMockObjectNsName(ICPPASTCompositeTypeSpecifier testDouble) {
    return testDouble.getName().toString() + MockatorConstants.NS_SUFFIX;
  }
}
