package ch.hsr.ifs.cute.mockator.refsupport.functions;

import static ch.hsr.ifs.cute.mockator.MockatorConstants.COMMA;
import static ch.hsr.ifs.cute.mockator.MockatorConstants.SPACE;
import static ch.hsr.ifs.iltis.cpp.core.util.constants.CommonCPPConstants.CONST_KEYWORD;

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import ch.hsr.ifs.cute.mockator.refsupport.functions.params.ParameterSignatureHandler;


public class FunctionSignatureFormatter {

   private final ICPPASTFunctionDeclarator funDecl;

   public FunctionSignatureFormatter(final ICPPASTFunctionDeclarator funDecl) {
      this.funDecl = funDecl;
   }

   public String getFunctionSignature() {
      return String.format("%s(%s)%s", getFunDeclName(), getParameters(), getConstDeclIfNecessary());
   }

   private String getFunDeclName() {
      return funDecl.getName().getLastName().toString();
   }

   private String getConstDeclIfNecessary() {
      return funDecl.isConst() ? SPACE + CONST_KEYWORD : "";
   }

   private String getParameters() {
      final Collection<String> params = new ParameterSignatureHandler(funDecl).getParameterSignatures();
      final String delimiter = COMMA + SPACE;
      return params.stream().collect(Collectors.joining(delimiter));
   }
}
