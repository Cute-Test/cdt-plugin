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
import org.eclipse.core.runtime.IProgressMonitor;

import ch.hsr.ifs.iltis.cpp.wrappers.CRefactoringContext;
import ch.hsr.ifs.iltis.cpp.wrappers.ModificationCollector;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;


class WithoutCuteFileCreator extends MockFunctionFileCreator {

   public WithoutCuteFileCreator(final ModificationCollector collector, final CRefactoringContext cRefContext, final ITranslationUnit originTu,
                                 final ICProject mockatorProject, final ICProject originProject, final CppStandard cppStd,
                                 final IProgressMonitor pm) {
      super(collector, cRefContext, originTu, mockatorProject, originProject, cppStd, pm);
   }

   @Override
   protected void insertContentForHeaderFile(final IASTTranslationUnit newTu, final ASTRewrite rewriter, final IASTName functionToMock,
         final String suiteName) {
      insertTestFunDecl(functionToMock, newTu, rewriter);
   }

   private void insertTestFunDecl(final IASTName functionToMock, final IASTTranslationUnit newTu, final ASTRewrite rewriter) {
      final ICPPASTFunctionDeclarator testFunDecl = createTestFunctionDecl(functionToMock);
      final ICPPASTSimpleDeclSpecifier declSpec = nodeFactory.newSimpleDeclSpecifier();
      declSpec.setType(IASTSimpleDeclSpecifier.t_void);
      declSpec.setStorageClass(IASTDeclSpecifier.sc_extern);
      final IASTSimpleDeclaration simpleDecl = nodeFactory.newSimpleDeclaration(declSpec);
      simpleDecl.addDeclarator(testFunDecl);
      rewriter.insertBefore(newTu, null, simpleDecl, null);
   }

   @Override
   protected void insertAssertIncludes(final IASTTranslationUnit newTu, final ASTRewrite rewriter) {
      insertCAssertInclude(newTu, rewriter);
   }

   @Override
   protected void createAddtitionalTestSupport(final IASTTranslationUnit newTu, final ASTRewrite rewriter,
         final ICPPASTFunctionDeclarator funDeclToMock, final String suiteName) {}

   private static void insertCAssertInclude(final IASTTranslationUnit newTu, final ASTRewrite rewriter) {
      final AstIncludeNode cute = new AstIncludeNode(MockatorConstants.C_ASSERT_INCLUDE, true);
      cute.insertInTu(newTu, rewriter);
   }

   @Override
   protected IASTExpressionStatement createAssertEqualStmt(final String fqCallsVectorName) {
      final IASTInitializerClause[] params = createAssertEqualParams(fqCallsVectorName);
      final IASTFunctionCallExpression assertEqual = nodeFactory.newFunctionCallExpression(createCAssert(), params);
      return nodeFactory.newExpressionStatement(assertEqual);
   }

   private static IASTInitializerClause[] createAssertEqualParams(final String fqCallsVectorName) {
      final ICPPASTBinaryExpression binOp = nodeFactory.newBinaryExpression(IASTBinaryExpression.op_equals, createExpectation(), createActual(
            fqCallsVectorName));
      return new IASTInitializerClause[] { binOp };
   }

   private static IASTIdExpression createActual(final String fqCallsVectorName) {
      final IASTName actualName = nodeFactory.newName(fqCallsVectorName.toCharArray());
      return nodeFactory.newIdExpression(actualName);
   }

   private static IASTIdExpression createExpectation() {
      return nodeFactory.newIdExpression(nodeFactory.newName(EXPECTED_VECTOR_NAME.toCharArray()));
   }

   private static IASTIdExpression createCAssert() {
      return nodeFactory.newIdExpression(nodeFactory.newName(MockatorConstants.C_ASSERT_EQUAL.toCharArray()));
   }
}
