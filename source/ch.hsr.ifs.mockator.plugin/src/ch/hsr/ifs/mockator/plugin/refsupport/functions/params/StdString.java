package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;

import ch.hsr.ifs.iltis.cpp.util.CPPNameConstants;


public class StdString {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();

   public boolean isStdString(final IASTInitializerClause param) {
      if (param instanceof IASTLiteralExpression) { return isStringLiteral((IASTLiteralExpression) param); }

      return false;
   }

   private static boolean isStringLiteral(final IASTLiteralExpression literal) {
      return literal.getKind() == IASTLiteralExpression.lk_string_literal;
   }

   public ICPPASTDeclSpecifier createStdStringDecl() {
      final IASTName stdString = nodeFactory.newName(CPPNameConstants.STD_STRING.toCharArray());
      final ICPPASTNamedTypeSpecifier declspec = nodeFactory.newTypedefNameSpecifier(stdString);
      declspec.setConst(true);
      return declspec;
   }
}
