package ch.hsr.ifs.mockator.plugin.mockobject.function;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
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
class WithoutCuteFileCreator extends MockFunctionFileCreator {

   public WithoutCuteFileCreator(ModificationCollector collector, CRefactoringContext cRefContext, ITranslationUnit originTu,
                                 ICProject mockatorProject, ICProject originProject, CppStandard cppStd, IProgressMonitor pm) {
      super(collector, cRefContext, originTu, mockatorProject, originProject, cppStd, pm);
   }

   @Override
   protected void insertContentForHeaderFile(IASTTranslationUnit newTu, ASTRewrite rewriter, IASTName functionToMock, String suiteName) {
      insertTestFunDecl(functionToMock, newTu, rewriter);
   }

   private void insertTestFunDecl(IASTName functionToMock, IASTTranslationUnit newTu, ASTRewrite rewriter) {
      ICPPASTFunctionDeclarator testFunDecl = createTestFunctionDecl(functionToMock);
      ICPPASTSimpleDeclSpecifier declSpec = nodeFactory.newSimpleDeclSpecifier();
      declSpec.setType(IASTSimpleDeclSpecifier.t_void);
      declSpec.setStorageClass(IASTDeclSpecifier.sc_extern);
      IASTSimpleDeclaration simpleDecl = nodeFactory.newSimpleDeclaration(declSpec);
      simpleDecl.addDeclarator(testFunDecl);
      rewriter.insertBefore(newTu, null, simpleDecl, null);
   }

   @Override
   protected void insertAssertIncludes(IASTTranslationUnit newTu, ASTRewrite rewriter) {
      insertCAssertInclude(newTu, rewriter);
   }

   @Override
   protected void createAddtitionalTestSupport(IASTTranslationUnit newTu, ASTRewrite rewriter, ICPPASTFunctionDeclarator funDeclToMock,
         String suiteName) {}

   private static void insertCAssertInclude(IASTTranslationUnit newTu, ASTRewrite rewriter) {
      AstIncludeNode cute = new AstIncludeNode(MockatorConstants.C_ASSERT_INCLUDE, true);
      cute.insertInTu(newTu, rewriter);
   }

   @Override
   protected IASTExpressionStatement createAssertEqualStmt(String fqCallsVectorName) {
      IASTInitializerClause[] params = createAssertEqualParams(fqCallsVectorName);
      IASTFunctionCallExpression assertEqual = nodeFactory.newFunctionCallExpression(createCAssert(), params);
      return nodeFactory.newExpressionStatement(assertEqual);
   }

   private static IASTInitializerClause[] createAssertEqualParams(String fqCallsVectorName) {
      ICPPASTBinaryExpression binOp = nodeFactory.newBinaryExpression(IASTBinaryExpression.op_equals, createExpectation(), createActual(
            fqCallsVectorName));
      return new IASTInitializerClause[] { binOp };
   }

   private static IASTIdExpression createActual(String fqCallsVectorName) {
      IASTName actualName = nodeFactory.newName(fqCallsVectorName.toCharArray());
      return nodeFactory.newIdExpression(actualName);
   }

   private static IASTIdExpression createExpectation() {
      return nodeFactory.newIdExpression(nodeFactory.newName(EXPECTED_VECTOR_NAME.toCharArray()));
   }

   private static IASTIdExpression createCAssert() {
      return nodeFactory.newIdExpression(nodeFactory.newName(MockatorConstants.C_ASSERT_EQUAL.toCharArray()));
   }
}
