package ch.hsr.ifs.mockator.plugin.mockobject.expectations.vector;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.CALLS;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;

@SuppressWarnings("restriction")
public class ExpectationsCpp11Strategy implements ExpectationsCppStdStrategy {
  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

  @Override
  public List<IASTStatement> createExpectationsVector(
      Collection<? extends TestDoubleMemFun> memFuns, String newExpectationsName,
      ICPPASTFunctionDefinition testFunction, Maybe<IASTName> expectationsVector,
      LinkedEditModeStrategy linkedEdit) {
    if (expectationsVector.isSome())
      return list();

    return getExpectations(memFuns, newExpectationsName, linkedEdit);
  }

  private static List<IASTStatement> getExpectations(
      Collection<? extends TestDoubleMemFun> memFuns, String newExpectations,
      LinkedEditModeStrategy linkedEdit) {
    IASTName calls = nodeFactory.newName(CALLS.toCharArray());
    IASTSimpleDeclaration declaration =
        nodeFactory.newSimpleDeclaration(nodeFactory.newTypedefNameSpecifier(calls));
    IASTInitializerClause callInitializer = getCallInitializer(memFuns, linkedEdit);
    IASTEqualsInitializer equalsInitializer = nodeFactory.newEqualsInitializer(callInitializer);
    ICPPASTDeclarator declarator =
        nodeFactory.newDeclarator(nodeFactory.newName(newExpectations.toCharArray()));
    declarator.setInitializer(equalsInitializer);
    declaration.addDeclarator(declarator);
    return list((IASTStatement) nodeFactory.newDeclarationStatement(declaration));
  }

  private static IASTInitializerClause getCallInitializer(
      Collection<? extends TestDoubleMemFun> memFuns, LinkedEditModeStrategy strategy) {
    ICPPASTInitializerList result = nodeFactory.newInitializerList();
    for (TestDoubleMemFun memFun : memFuns) {
      addToInitializerList(result, memFun, strategy);
    }
    return result;
  }

  private static void addToInitializerList(ICPPASTInitializerList vectorInitializerList,
      TestDoubleMemFun memFun, LinkedEditModeStrategy strategy) {
    ICPPASTInitializerList callInitializerList = nodeFactory.newInitializerList();
    for (IASTInitializerClause clause : getCallExpectations(memFun, strategy)) {
      callInitializerList.addClause(clause);
    }
    vectorInitializerList.addClause(callInitializerList);
  }

  private static List<IASTInitializerClause> getCallExpectations(TestDoubleMemFun fun,
      LinkedEditModeStrategy strategy) {
    List<IASTInitializerClause> clauses = list();
    clauses.add(getSignatureLiteral(fun.getFunctionSignature()));
    clauses.addAll(fun.createDefaultArguments(CppStandard.Cpp11Std, strategy));
    return clauses;
  }

  private static ICPPASTLiteralExpression getSignatureLiteral(String signature) {
    return nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal,
        StringUtil.quote(signature));
  }
}
