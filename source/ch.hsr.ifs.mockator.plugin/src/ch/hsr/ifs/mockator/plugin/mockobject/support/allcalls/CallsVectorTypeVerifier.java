package ch.hsr.ifs.mockator.plugin.mockobject.support.allcalls;

import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;


public class CallsVectorTypeVerifier {

   private final IType type;

   public CallsVectorTypeVerifier(final IASTIdExpression idExpr) {
      type = getUnderlyingType(idExpr.getName());
   }

   public CallsVectorTypeVerifier(final IASTName name) {
      type = getUnderlyingType(name);
   }

   private static IType getUnderlyingType(final IASTName name) {
      final IBinding binding = name.resolveBinding();

      if (!(binding instanceof IVariable)) return null;

      return ((IVariable) binding).getType();
   }

   public boolean hasCallsVectorType() {
      if (!(type instanceof ITypedef)) return false;

      final String name = ((ITypedef) type).getName();
      return name.equals(MockatorConstants.CALLS);
   }

   public boolean isVectorOfCallsVector() {
      if (!(type instanceof ICPPTemplateInstance)) return false;

      final ICPPTemplateArgument[] templateArgs = ((ICPPTemplateInstance) type).getTemplateArguments();

      if (templateArgs.length == 0) return false;

      final ICPPTemplateArgument fstTemplateArg = templateArgs[0];
      final IType fstTypeValue = fstTemplateArg.getTypeValue();

      if (!(fstTypeValue instanceof ICPPTemplateInstance)) return false;

      final ICPPTemplateArgument[] innerTemplateArg = ((ICPPTemplateInstance) fstTypeValue).getTemplateArguments();

      if (innerTemplateArg.length == 0) return false;

      final IType innerTypeValue = innerTemplateArg[0].getTypeValue();

      if (!(innerTypeValue instanceof ICPPClassType)) return false;

      return ((ICPPClassType) innerTypeValue).getName().equals(MockatorConstants.CALL);
   }
}
