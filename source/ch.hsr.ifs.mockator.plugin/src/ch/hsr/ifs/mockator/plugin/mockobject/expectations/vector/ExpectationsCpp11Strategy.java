package ch.hsr.ifs.mockator.plugin.mockobject.expectations.vector;

import static ch.hsr.ifs.iltis.core.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.CALLS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.core.functional.OptHelper;

import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.incompleteclass.TestDoubleMemFun;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;


public class ExpectationsCpp11Strategy implements ExpectationsCppStdStrategy {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

   @Override
   public List<IASTStatement> createExpectationsVector(final Collection<? extends TestDoubleMemFun> memFuns, final String newExpectationsName,
            final ICPPASTFunctionDefinition testFunction, final Optional<IASTName> expectationsVector, final LinkedEditModeStrategy linkedEdit) {
      return OptHelper.returnIfPresentElse(expectationsVector, () -> new ArrayList<>(), () -> getExpectations(memFuns, newExpectationsName,
               linkedEdit));
   }

   private static List<IASTStatement> getExpectations(final Collection<? extends TestDoubleMemFun> memFuns, final String newExpectations,
            final LinkedEditModeStrategy linkedEdit) {
      final IASTName calls = nodeFactory.newName(CALLS.toCharArray());
      final IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(nodeFactory.newTypedefNameSpecifier(calls));
      final IASTInitializerClause callInitializer = getCallInitializer(memFuns, linkedEdit);
      final IASTEqualsInitializer equalsInitializer = nodeFactory.newEqualsInitializer(callInitializer);
      final ICPPASTDeclarator declarator = nodeFactory.newDeclarator(nodeFactory.newName(newExpectations.toCharArray()));
      declarator.setInitializer(equalsInitializer);
      declaration.addDeclarator(declarator);
      return list((IASTStatement) nodeFactory.newDeclarationStatement(declaration));
   }

   private static IASTInitializerClause getCallInitializer(final Collection<? extends TestDoubleMemFun> memFuns,
            final LinkedEditModeStrategy strategy) {
      final ICPPASTInitializerList result = nodeFactory.newInitializerList();
      for (final TestDoubleMemFun memFun : memFuns) {
         addToInitializerList(result, memFun, strategy);
      }
      return result;
   }

   private static void addToInitializerList(final ICPPASTInitializerList vectorInitializerList, final TestDoubleMemFun memFun,
            final LinkedEditModeStrategy strategy) {
      final ICPPASTInitializerList callInitializerList = nodeFactory.newInitializerList();
      for (final IASTInitializerClause clause : getCallExpectations(memFun, strategy)) {
         callInitializerList.addClause(clause);
      }
      vectorInitializerList.addClause(callInitializerList);
   }

   private static List<IASTInitializerClause> getCallExpectations(final TestDoubleMemFun fun, final LinkedEditModeStrategy strategy) {
      final List<IASTInitializerClause> clauses = new ArrayList<>();
      clauses.add(getSignatureLiteral(fun.getFunctionSignature()));
      clauses.addAll(fun.createDefaultArguments(CppStandard.Cpp11Std, strategy));
      return clauses;
   }

   private static ICPPASTLiteralExpression getSignatureLiteral(final String signature) {
      return nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal, StringUtil.quote(signature));
   }
}
