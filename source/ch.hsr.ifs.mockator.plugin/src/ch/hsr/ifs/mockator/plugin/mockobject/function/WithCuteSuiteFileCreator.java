package ch.hsr.ifs.mockator.plugin.mockobject.function;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.PUSH_BACK;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.cpp.wrappers.CRefactoringContext;
import ch.hsr.ifs.iltis.cpp.wrappers.ModificationCollector;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;


class WithCuteSuiteFileCreator extends MockFunctionFileCreator {

   private static final String CUTE_SUITE_FQ         = "cute::suite";
   private static final String CUTE_SUITE_HEADER     = "cute_suite.h";
   private static final String CUTE_HEADER           = "cute.h";
   private static final String CUTE_MACRO            = "CUTE";
   private static final String CUTE_SUITE_FUN_PREFIX = "make_suite_";
   private static final String CUTE_SUITE_NAME       = "s";

   public WithCuteSuiteFileCreator(final ModificationCollector collector, final CRefactoringContext refactoringContext,
                                   final ITranslationUnit originTu, final ICProject mockatorProject, final ICProject originProject,
                                   final CppStandard cppStd, final IProgressMonitor pm) {
      super(collector, refactoringContext, originTu, mockatorProject, originProject, cppStd, pm);
   }

   private static void insertCuteSuiteFunDecl(final IASTTranslationUnit newTu, final ASTRewrite rewriter, final String suiteName) {
      final IASTName suiteFunName = nodeFactory.newName((CUTE_SUITE_FUN_PREFIX + suiteName).toCharArray());
      final IASTFunctionDeclarator funDecl = nodeFactory.newFunctionDeclarator(suiteFunName);
      final IASTName typeName = nodeFactory.newName(CUTE_SUITE_FQ.toCharArray());
      final ICPPASTNamedTypeSpecifier returnType = nodeFactory.newTypedefNameSpecifier(typeName);
      returnType.setStorageClass(IASTDeclSpecifier.sc_extern);
      final IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(returnType);
      declaration.addDeclarator(funDecl);
      rewriter.insertBefore(newTu, null, declaration, null);
   }

   private static void insertCuteSuiteInclude(final IASTTranslationUnit ast, final ASTRewrite rewriter) {
      final AstIncludeNode cuteSuite = new AstIncludeNode(CUTE_SUITE_HEADER);
      cuteSuite.insertInTu(ast, rewriter);
   }

   @Override
   protected void insertContentForHeaderFile(final IASTTranslationUnit newTu, final ASTRewrite rewriter, final IASTName functionToMock,
         final String suiteName) {
      insertCuteSuiteInclude(newTu, rewriter);
      insertCuteSuiteFunDecl(newTu, rewriter, suiteName);
   }

   @Override
   protected void insertAssertIncludes(final IASTTranslationUnit newTu, final ASTRewrite rewriter) {
      insertCuteInclude(newTu, rewriter);
   }

   @Override
   protected void createAddtitionalTestSupport(final IASTTranslationUnit newTu, final ASTRewrite rewriter,
         final ICPPASTFunctionDeclarator funDeclToMock, final String suiteName) {
      insertCuteSuiteFunction(newTu, rewriter, funDeclToMock, suiteName);
   }

   private static void insertCuteInclude(final IASTTranslationUnit ast, final ASTRewrite rewriter) {
      final AstIncludeNode cute = new AstIncludeNode(CUTE_HEADER);
      cute.insertInTu(ast, rewriter);
   }

   private static void insertCuteSuiteFunction(final IASTTranslationUnit newTu, final ASTRewrite rewriter,
         final ICPPASTFunctionDeclarator freeFunction, final String suiteName) {
      final IASTName cuteSuiteFunName = nodeFactory.newName((CUTE_SUITE_FUN_PREFIX + suiteName).toCharArray());
      final ICPPASTFunctionDeclarator funDecl = nodeFactory.newFunctionDeclarator(cuteSuiteFunName);
      final IASTName fullQfName = nodeFactory.newName(CUTE_SUITE_FQ.toCharArray());
      final ICPPASTNamedTypeSpecifier funDeclSpec = nodeFactory.newTypedefNameSpecifier(fullQfName);
      final IASTCompoundStatement funBody = nodeFactory.newCompoundStatement();
      final ICPPASTFunctionDefinition cuiteSuiteFun = nodeFactory.newFunctionDefinition(funDeclSpec, funDecl, funBody);
      final IASTDeclarationStatement cuteSuiteDeclStmt = createCuteSuiteVector();
      funBody.addStatement(cuteSuiteDeclStmt);
      final IASTName suiteAstName = nodeFactory.newName(CUTE_SUITE_NAME.toCharArray());
      final IASTIdExpression newIdExpression = nodeFactory.newIdExpression(suiteAstName);
      funBody.addStatement(createTestFunctionRegistration(freeFunction, newIdExpression));
      final IASTReturnStatement returnStmt = nodeFactory.newReturnStatement(newIdExpression);
      funBody.addStatement(returnStmt);
      rewriter.insertBefore(newTu, null, cuiteSuiteFun, null);
   }

   private static IASTDeclarationStatement createCuteSuiteVector() {
      final IASTName fullQualName = nodeFactory.newName(CUTE_SUITE_FQ.toCharArray());
      final ICPPASTNamedTypeSpecifier cuteSuiteType = nodeFactory.newTypedefNameSpecifier(fullQualName);
      final IASTSimpleDeclaration cuteSuiteDecl = nodeFactory.newSimpleDeclaration(cuteSuiteType);
      cuteSuiteDecl.addDeclarator(nodeFactory.newDeclarator(nodeFactory.newName(CUTE_SUITE_NAME.toCharArray())));
      return nodeFactory.newDeclarationStatement(cuteSuiteDecl);
   }

   private static IASTExpressionStatement createTestFunctionRegistration(final ICPPASTFunctionDeclarator funToMock,
         final IASTIdExpression newIdExpression) {
      final ICPPASTFieldReference pushBack = createPushBack(newIdExpression);
      final IASTInitializerClause[] funArgs = new IASTInitializerClause[1];
      funArgs[0] = createCuteTestWith(funToMock);
      final IASTFunctionCallExpression funExpr = nodeFactory.newFunctionCallExpression(pushBack, funArgs);
      return nodeFactory.newExpressionStatement(funExpr);
   }

   private static ICPPASTFunctionCallExpression createCuteTestWith(final ICPPASTFunctionDeclarator funToMock) {
      final IASTName cuteMacro = nodeFactory.newName(CUTE_MACRO.toCharArray());
      final String testFunctionName = MockatorConstants.TEST_FUNCTION_PREFIX + funToMock.getName().toString();
      final IASTIdExpression cuteMacroIdExpr = nodeFactory.newIdExpression(cuteMacro);
      final IASTName testFunName = nodeFactory.newName(testFunctionName.toCharArray());
      final IASTInitializerClause[] funArgs = new IASTInitializerClause[1];
      funArgs[0] = nodeFactory.newIdExpression(testFunName);
      return nodeFactory.newFunctionCallExpression(cuteMacroIdExpr, funArgs);
   }

   private static ICPPASTFieldReference createPushBack(final IASTIdExpression allCalls) {
      final IASTName pushBack = nodeFactory.newName(PUSH_BACK.toCharArray());
      return nodeFactory.newFieldReference(pushBack, allCalls);
   }

   @Override
   protected IASTExpressionStatement createAssertEqualStmt(final String fqCallsVectorName) {
      final IASTFunctionCallExpression assertEqual = nodeFactory.newFunctionCallExpression(getAssertMacro(), new IASTInitializerClause[] {
                                                                                                                                           createExpected(),
                                                                                                                                           createActual(
                                                                                                                                                 fqCallsVectorName) });
      return nodeFactory.newExpressionStatement(assertEqual);
   }

   private static IASTIdExpression createActual(final String fqCallsVectorName) {
      return nodeFactory.newIdExpression(nodeFactory.newName(fqCallsVectorName.toCharArray()));
   }

   private static IASTIdExpression createExpected() {
      return nodeFactory.newIdExpression(nodeFactory.newName(EXPECTED_VECTOR_NAME.toCharArray()));
   }

   private static IASTIdExpression getAssertMacro() {
      return nodeFactory.newIdExpression(nodeFactory.newName(MockatorConstants.CUTE_ASSERT_EQUAL.toCharArray()));
   }
}
