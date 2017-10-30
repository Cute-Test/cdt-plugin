package ch.hsr.ifs.mockator.plugin.refsupport.functions;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.COMMA;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.CONST_KEYWORD;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.SPACE;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.zipMap;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;

import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterSignatureHandler;


public class FunctionSignatureFormatter {

   private final ICPPASTFunctionDeclarator funDecl;

   public FunctionSignatureFormatter(ICPPASTFunctionDeclarator funDecl) {
      this.funDecl = funDecl;
   }

   public String getFunctionSignature() {
      return StringUtil.pythonFormat("%(funName)s(%(params)s)%(const)s", zipMap(array("funName", "params", "const"), array(getFunDeclName(),
            getParameters(), getConstDeclIfNecessary())));
   }

   private String getFunDeclName() {
      return funDecl.getName().getLastName().toString();
   }

   private String getConstDeclIfNecessary() {
      return funDecl.isConst() ? SPACE + CONST_KEYWORD : "";
   }

   private String getParameters() {
      Collection<String> params = new ParameterSignatureHandler(funDecl).getParameterSignatures();
      String delimiter = COMMA + SPACE;
      return StringUtil.join(params, delimiter);
   }
}
