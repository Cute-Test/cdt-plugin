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
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;

@SuppressWarnings("restriction")
class WithCuteSuiteFileCreator extends MockFunctionFileCreator {
  private static final String CUTE_SUITE_FQ = "cute::suite";
  private static final String CUTE_SUITE_HEADER = "cute_suite.h";
  private static final String CUTE_HEADER = "cute.h";
  private static final String CUTE_MACRO = "CUTE";
  private static final String CUTE_SUITE_FUN_PREFIX = "make_suite_";
  private static final String CUTE_SUITE_NAME = "s";

  public WithCuteSuiteFileCreator(ModificationCollector collector,
      CRefactoringContext refactoringContext, ITranslationUnit originTu, ICProject mockatorProject,
      ICProject originProject, CppStandard cppStd, IProgressMonitor pm) {
    super(collector, refactoringContext, originTu, mockatorProject, originProject, cppStd, pm);
  }

  private static void insertCuteSuiteFunDecl(IASTTranslationUnit newTu, ASTRewrite rewriter,
      String suiteName) {
    IASTName suiteFunName = nodeFactory.newName((CUTE_SUITE_FUN_PREFIX + suiteName).toCharArray());
    IASTFunctionDeclarator funDecl = nodeFactory.newFunctionDeclarator(suiteFunName);
    IASTName typeName = nodeFactory.newName(CUTE_SUITE_FQ.toCharArray());
    ICPPASTNamedTypeSpecifier returnType = nodeFactory.newTypedefNameSpecifier(typeName);
    returnType.setStorageClass(IASTDeclSpecifier.sc_extern);
    IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(returnType);
    declaration.addDeclarator(funDecl);
    rewriter.insertBefore(newTu, null, declaration, null);
  }

  private static void insertCuteSuiteInclude(IASTTranslationUnit ast, ASTRewrite rewriter) {
    AstIncludeNode cuteSuite = new AstIncludeNode(CUTE_SUITE_HEADER);
    cuteSuite.insertInTu(ast, rewriter);
  }

  @Override
  protected void insertContentForHeaderFile(IASTTranslationUnit newTu, ASTRewrite rewriter,
      IASTName functionToMock, String suiteName) {
    insertCuteSuiteInclude(newTu, rewriter);
    insertCuteSuiteFunDecl(newTu, rewriter, suiteName);
  }

  @Override
  protected void insertAssertIncludes(IASTTranslationUnit newTu, ASTRewrite rewriter) {
    insertCuteInclude(newTu, rewriter);
  }

  @Override
  protected void createAddtitionalTestSupport(IASTTranslationUnit newTu, ASTRewrite rewriter,
      ICPPASTFunctionDeclarator funDeclToMock, String suiteName) {
    insertCuteSuiteFunction(newTu, rewriter, funDeclToMock, suiteName);
  }

  private static void insertCuteInclude(IASTTranslationUnit ast, ASTRewrite rewriter) {
    AstIncludeNode cute = new AstIncludeNode(CUTE_HEADER);
    cute.insertInTu(ast, rewriter);
  }

  private static void insertCuteSuiteFunction(IASTTranslationUnit newTu, ASTRewrite rewriter,
      ICPPASTFunctionDeclarator freeFunction, String suiteName) {
    IASTName cuteSuiteFunName =
        nodeFactory.newName((CUTE_SUITE_FUN_PREFIX + suiteName).toCharArray());
    ICPPASTFunctionDeclarator funDecl = nodeFactory.newFunctionDeclarator(cuteSuiteFunName);
    IASTName fullQfName = nodeFactory.newName(CUTE_SUITE_FQ.toCharArray());
    ICPPASTNamedTypeSpecifier funDeclSpec = nodeFactory.newTypedefNameSpecifier(fullQfName);
    IASTCompoundStatement funBody = nodeFactory.newCompoundStatement();
    ICPPASTFunctionDefinition cuiteSuiteFun =
        nodeFactory.newFunctionDefinition(funDeclSpec, funDecl, funBody);
    IASTDeclarationStatement cuteSuiteDeclStmt = createCuteSuiteVector();
    funBody.addStatement(cuteSuiteDeclStmt);
    IASTName suiteAstName = nodeFactory.newName(CUTE_SUITE_NAME.toCharArray());
    IASTIdExpression newIdExpression = nodeFactory.newIdExpression(suiteAstName);
    funBody.addStatement(createTestFunctionRegistration(freeFunction, newIdExpression));
    IASTReturnStatement returnStmt = nodeFactory.newReturnStatement(newIdExpression);
    funBody.addStatement(returnStmt);
    rewriter.insertBefore(newTu, null, cuiteSuiteFun, null);
  }

  private static IASTDeclarationStatement createCuteSuiteVector() {
    IASTName fullQualName = nodeFactory.newName(CUTE_SUITE_FQ.toCharArray());
    ICPPASTNamedTypeSpecifier cuteSuiteType = nodeFactory.newTypedefNameSpecifier(fullQualName);
    IASTSimpleDeclaration cuteSuiteDecl = nodeFactory.newSimpleDeclaration(cuteSuiteType);
    cuteSuiteDecl.addDeclarator(nodeFactory.newDeclarator(nodeFactory.newName(CUTE_SUITE_NAME
        .toCharArray())));
    return nodeFactory.newDeclarationStatement(cuteSuiteDecl);
  }

  private static IASTExpressionStatement createTestFunctionRegistration(
      ICPPASTFunctionDeclarator funToMock, IASTIdExpression newIdExpression) {
    ICPPASTFieldReference pushBack = createPushBack(newIdExpression);
    IASTInitializerClause[] funArgs = new IASTInitializerClause[1];
    funArgs[0] = createCuteTestWith(funToMock);
    IASTFunctionCallExpression funExpr = nodeFactory.newFunctionCallExpression(pushBack, funArgs);
    return nodeFactory.newExpressionStatement(funExpr);
  }

  private static ICPPASTFunctionCallExpression createCuteTestWith(
      ICPPASTFunctionDeclarator funToMock) {
    IASTName cuteMacro = nodeFactory.newName(CUTE_MACRO.toCharArray());
    String testFunctionName =
        MockatorConstants.TEST_FUNCTION_PREFIX + funToMock.getName().toString();
    IASTIdExpression cuteMacroIdExpr = nodeFactory.newIdExpression(cuteMacro);
    IASTName testFunName = nodeFactory.newName(testFunctionName.toCharArray());
    IASTInitializerClause[] funArgs = new IASTInitializerClause[1];
    funArgs[0] = nodeFactory.newIdExpression(testFunName);
    return nodeFactory.newFunctionCallExpression(cuteMacroIdExpr, funArgs);
  }

  private static ICPPASTFieldReference createPushBack(IASTIdExpression allCalls) {
    IASTName pushBack = nodeFactory.newName(PUSH_BACK.toCharArray());
    return nodeFactory.newFieldReference(pushBack, allCalls);
  }

  @Override
  protected IASTExpressionStatement createAssertEqualStmt(String fqCallsVectorName) {
    IASTFunctionCallExpression assertEqual =
        nodeFactory.newFunctionCallExpression(getAssertMacro(), new IASTInitializerClause[] {
            createExpected(), createActual(fqCallsVectorName)});
    return nodeFactory.newExpressionStatement(assertEqual);
  }

  private static IASTIdExpression createActual(String fqCallsVectorName) {
    return nodeFactory.newIdExpression(nodeFactory.newName(fqCallsVectorName.toCharArray()));
  }

  private static IASTIdExpression createExpected() {
    return nodeFactory.newIdExpression(nodeFactory.newName(EXPECTED_VECTOR_NAME.toCharArray()));
  }

  private static IASTIdExpression getAssertMacro() {
    return nodeFactory.newIdExpression(nodeFactory.newName(MockatorConstants.CUTE_ASSERT_EQUAL
        .toCharArray()));
  }
}
