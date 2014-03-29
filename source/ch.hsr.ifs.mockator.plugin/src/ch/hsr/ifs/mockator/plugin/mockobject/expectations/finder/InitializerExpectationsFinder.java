package ch.hsr.ifs.mockator.plugin.mockobject.expectations.finder;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Collection;

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

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.MemFunCallExpectation;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NodeContainer;

// calls expectedMock = {{"Mock()"}, {"foo() const"}};
class InitializerExpectationsFinder extends AbstractExpectationsFinder {

  public InitializerExpectationsFinder(Collection<MemFunCallExpectation> callExpectations,
      NodeContainer<IASTName> expectationVector, IASTName expectationsVectorName) {
    super(callExpectations, expectationVector, expectationsVectorName);
  }

  @Override
  protected void collectExpectations(IASTStatement expectationStmt) {
    Assert.instanceOf(expectationStmt, IASTDeclarationStatement.class,
        "Should be called with an declaration statement");
    IASTDeclarationStatement declStmt = (IASTDeclarationStatement) expectationStmt;
    IASTDeclaration declaration = declStmt.getDeclaration();

    if (!(declaration instanceof IASTSimpleDeclaration))
      return;

    IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
    IASTDeclSpecifier declSpecifier = simpleDecl.getDeclSpecifier();

    if (!(isCallsVector(declSpecifier)))
      return;

    IASTName matchingName = getMatchingName(simpleDecl);

    if (matchingName == null)
      return;

    ICPPASTInitializerList initializer =
        AstUtil.getChildOfType(declaration, ICPPASTInitializerList.class);

    if (initializer == null)
      return;

    expectationVector.setNode(matchingName);
    callExpectations.addAll(getCallExpectations(initializer));
  }

  private Collection<MemFunCallExpectation> getCallExpectations(ICPPASTInitializerList initializer) {
    Collection<MemFunCallExpectation> callExpectations = orderPreservingSet();

    for (IASTInitializerClause clause : initializer.getClauses()) {
      if (!(clause instanceof ICPPASTInitializerList)) {
        continue;
      }
      IASTInitializerClause[] clauses = ((ICPPASTInitializerList) clause).getClauses();
      Assert.isTrue(clauses.length > 0, "Not a valid call initializer");
      Assert.isTrue(isStringLiteral(clauses[0]), "Not a string literal");
      MemFunCallExpectation memFunCall = toMemberFunctionCall(clauses[0]);
      callExpectations.add(memFunCall);
    }

    return callExpectations;
  }

  private static boolean isCallsVector(IASTDeclSpecifier declSpecifier) {
    if (!(declSpecifier instanceof ICPPASTNamedTypeSpecifier))
      return false;

    ICPPASTNamedTypeSpecifier namedTypeSpec = (ICPPASTNamedTypeSpecifier) declSpecifier;
    return isCallsTypedef(namedTypeSpec);
  }

  private static boolean isCallsTypedef(ICPPASTNamedTypeSpecifier namedTypeSpec) {
    return namedTypeSpec.getName().toString().equals(MockatorConstants.CALLS);
  }

  private IASTName getMatchingName(IASTSimpleDeclaration simpleDecl) {
    IASTDeclarator[] declarators = simpleDecl.getDeclarators();

    if (declarators.length < 1)
      return null;

    IASTName name = declarators[0].getName();

    if (matchesName(name))
      return name;

    return null;
  }
}
