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

  public CallsVectorTypeVerifier(IASTIdExpression idExpr) {
    type = getUnderlyingType(idExpr.getName());
  }

  public CallsVectorTypeVerifier(IASTName name) {
    type = getUnderlyingType(name);
  }

  private static IType getUnderlyingType(IASTName name) {
    IBinding binding = name.resolveBinding();

    if (!(binding instanceof IVariable))
      return null;

    return ((IVariable) binding).getType();
  }

  public boolean hasCallsVectorType() {
    if (!(type instanceof ITypedef))
      return false;

    String name = ((ITypedef) type).getName();
    return name.equals(MockatorConstants.CALLS);
  }

  public boolean isVectorOfCallsVector() {
    if (!(type instanceof ICPPTemplateInstance))
      return false;

    ICPPTemplateArgument[] templateArgs = ((ICPPTemplateInstance) type).getTemplateArguments();

    if (templateArgs.length == 0)
      return false;

    ICPPTemplateArgument fstTemplateArg = templateArgs[0];
    IType fstTypeValue = fstTemplateArg.getTypeValue();

    if (!(fstTypeValue instanceof ICPPTemplateInstance))
      return false;

    ICPPTemplateArgument[] innerTemplateArg =
        ((ICPPTemplateInstance) fstTypeValue).getTemplateArguments();

    if (innerTemplateArg.length == 0)
      return false;

    IType innerTypeValue = innerTemplateArg[0].getTypeValue();

    if (!(innerTypeValue instanceof ICPPClassType))
      return false;

    return ((ICPPClassType) innerTypeValue).getName().equals(MockatorConstants.CALL);
  }
}
