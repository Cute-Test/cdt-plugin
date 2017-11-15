package ch.hsr.ifs.mockator.plugin.refsupport.functions.params;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;


// Inspired by TDD
public class ParameterNameCreator {

   private static final ICPPNodeFactory nodeFactory = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private final Map<String, Boolean>  nameHistory;

   public ParameterNameCreator(final Map<String, Boolean> nameHistory) {
      this.nameHistory = nameHistory;
   }

   public IASTName getParamName(final IASTInitializerClause clause) {
      return createName(replaceNonAlphanumericCharacters(clause));
   }

   public IASTName getParamName(final IType type) {
      return createName(getParameterCharacter(getFallBackName(type)));
   }

   public IASTName getParamName(final String type) {
      return createName(getParameterCharacter(type));
   }

   public IASTName getParamName(final IASTCompositeTypeSpecifier klass) {
      return createName(getParameterCharacter(String.valueOf(klass.getName().getSimpleID())));
   }

   public IASTName createParameterName(final IASTIdExpression idExpr) {
      return createName(String.valueOf(idExpr.getName().getSimpleID()));
   }

   private static String getFallBackName(IType type) {
      type = ASTUtil.unwindPointerOrRefType(type);

      if (type instanceof IQualifierType) {
         type = ((IQualifierType) type).getType();
      }

      if (type instanceof ITypedef) {
         return ASTTypeUtil.getQualifiedName((ICPPBinding) type).substring(0, 1);
      }
      if (type instanceof ICPPTemplateInstance || type instanceof ICPPClassType) {
         return ASTTypeUtil.getType(type).toLowerCase();
      } else if (type instanceof IBasicType) {
         return ((IBasicType) type).getKind().toString().substring(1).toLowerCase();
      }

      return " ";
   }

   private String getParameterCharacter(final String fallBackVarName) {
      String newName = Character.toString(fallBackVarName.charAt(0)).toLowerCase();

      while (nameHistory.get(newName) != null) {
         newName = (char) (newName.charAt(0) + 1) + "";
      }

      return newName;
   }

   private IASTName createName(final String name) {
      final char[] result = name.toCharArray();

      if (result.length > 0) {
         result[0] = Character.toLowerCase(result[0]);
      }

      return makeUniqueName(String.valueOf(result));
   }

   private IASTName makeUniqueName(final String name) {
      String newName = name;

      if (nameHistory.get(newName) != null) {
         newName = newName + "1";
      }

      final StringBuilder sb = new StringBuilder(newName);

      while (nameHistory.get(newName) != null) {
         sb.append(newName.charAt(newName.length() - 1) + 1);
      }

      newName = sb.toString();
      nameHistory.put(newName, true);
      return nodeFactory.newName(newName.toCharArray());
   }

   private static String replaceNonAlphanumericCharacters(final IASTInitializerClause clause) {
      return clause.getRawSignature().replaceAll("[\\P{Alpha}&&\\P{Digit}]", "");
   }
}
