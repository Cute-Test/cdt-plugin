package ch.hsr.ifs.mockator.plugin.testdouble.movetons;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;


class TestDoubleToNsMover {

   private final ASTRewrite  rewriter;
   private final CppStandard cppStd;

   public TestDoubleToNsMover(final ASTRewrite rewriter, final CppStandard cppStd) {
      this.rewriter = rewriter;
      this.cppStd = cppStd;
   }

   public void moveToNamespace(final ICPPASTCompositeTypeSpecifier testDoubleToMove) {
      final ICPPASTFunctionDefinition testFunction = getParentFunction(testDoubleToMove);
      ILTISException.Unless.notNull("Test double is not a local class", testFunction);
      final IASTSimpleDeclaration movedTestDouble = getClassDeclaration(testDoubleToMove).copy();
      insertTestDoubleInNamespace(movedTestDouble, testDoubleToMove, testFunction);
      removeTestDoubleInFunction(testDoubleToMove);
      insertUsingNamespaceStmt(getTestDoubleClass(movedTestDouble), testFunction);
   }

   private static ICPPASTCompositeTypeSpecifier getTestDoubleClass(final IASTSimpleDeclaration simpleDecl) {
      return CPPVisitor.findChildWithType(simpleDecl, ICPPASTCompositeTypeSpecifier.class).orElse(null);
   }

   private static IASTSimpleDeclaration getClassDeclaration(final ICPPASTCompositeTypeSpecifier testDouble) {
      return CPPVisitor.findAncestorWithType(testDouble, IASTSimpleDeclaration.class).orElse(null);
   }

   private void insertTestDoubleInNamespace(final IASTSimpleDeclaration testDouble, final ICPPASTCompositeTypeSpecifier testDoubleToMove,
         final ICPPASTFunctionDefinition testFunction) {
      final TestDoubleInNsInserter inserter = new TestDoubleInNsInserter(rewriter, cppStd);
      inserter.insertTestDouble(testDouble, testDoubleToMove, testFunction);
   }

   private void removeTestDoubleInFunction(final ICPPASTCompositeTypeSpecifier testDoubleToMove) {
      rewriter.remove(getClassDeclaration(testDoubleToMove), null);
   }

   private static ICPPASTFunctionDefinition getParentFunction(final IASTCompositeTypeSpecifier testDouble) {
      final ICPPASTFunctionDefinition testFun = CPPVisitor.findAncestorWithType(testDouble, ICPPASTFunctionDefinition.class).orElse(null);
      ILTISException.Unless.notNull("Test double class must be a member of a function!", testFun);
      return testFun;
   }

   private void insertUsingNamespaceStmt(final ICPPASTCompositeTypeSpecifier testDouble, final ICPPASTFunctionDefinition testFun) {
      final TestDoubleUsingNsHandler namespaceHandler = new TestDoubleUsingNsHandler(testDouble, rewriter);
      namespaceHandler.insertUsingNamespaceStmt(testFun);
   }
}
