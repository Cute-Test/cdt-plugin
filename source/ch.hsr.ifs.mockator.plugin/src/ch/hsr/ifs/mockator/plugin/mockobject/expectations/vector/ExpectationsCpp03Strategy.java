package ch.hsr.ifs.mockator.plugin.mockobject.expectations.vector;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.CALLS;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;
import java.util.List;

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
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;

import ch.hsr.ifs.mockator.plugin.base.functional.F1;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.mockobject.expectations.BoostAssignInitializerCreator;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;
import ch.hsr.ifs.mockator.plugin.refsupport.finder.NameFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
public class ExpectationsCpp03Strategy implements ExpectationsCppStdStrategy {
  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

  @Override
  public List<IASTStatement> createExpectationsVector(
      Collection<? extends TestDoubleMemFun> memFuns, String newExpectationsName,
      ICPPASTFunctionDefinition testFunction, Maybe<IASTName> expectationsVector,
      LinkedEditModeStrategy linkedEdit) {
    List<IASTStatement> expectations = list();

    if (expectationsVector.isNone()) {
      expectations.add(createExpectationVectorDeclStmt(newExpectationsName));
      expectations.add(createBoostAssignInitializer(memFuns, newExpectationsName, linkedEdit));
    } else if (!hasBoostAssignInitializer(testFunction, expectationsVector.get())) {
      expectations.add(createBoostAssignInitializer(memFuns, expectationsVector.get().toString(),
          linkedEdit));
    }

    return expectations;
  }

  private static IASTExpressionStatement createBoostAssignInitializer(
      Collection<? extends TestDoubleMemFun> memFuns, String vectorName,
      LinkedEditModeStrategy linkedEdit) {
    BoostAssignInitializerCreator creator =
        new BoostAssignInitializerCreator(memFuns, vectorName, linkedEdit);
    return creator.createBoostAssignInitializer();
  }

  private static boolean hasBoostAssignInitializer(ICPPASTFunctionDefinition testFun,
      final IASTName expVector) {
    return new NameFinder(testFun).getNameMatchingCriteria(new F1<IASTName, Boolean>() {
      @Override
      public Boolean apply(IASTName name) {
        return name.toString().equals(expVector.toString()) && isInBoostAssign(name);
      }

      private boolean isInBoostAssign(IASTName name) {
        ICPPASTBinaryExpression binExp =
            AstUtil.getAncestorOfType(name, ICPPASTBinaryExpression.class);
        return binExp != null && binExp.getOperator() == IASTBinaryExpression.op_plusAssign;
      }
    }).isSome();
  }

  private static IASTDeclarationStatement createExpectationVectorDeclStmt(String expectationsName) {
    IASTName expectedName = nodeFactory.newName(expectationsName.toCharArray());
    ICPPASTDeclarator declarator = nodeFactory.newDeclarator(expectedName);
    IASTName callsName = nodeFactory.newName(CALLS.toCharArray());
    ICPPASTNamedTypeSpecifier namedType = nodeFactory.newTypedefNameSpecifier(callsName);
    IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(namedType);
    declaration.addDeclarator(declarator);
    return nodeFactory.newDeclarationStatement(declaration);
  }
}
