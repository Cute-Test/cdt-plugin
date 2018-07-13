package ch.hsr.ifs.cute.mockator.refsupport.functions.params;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;


public class MethodParamEquivalenceTester {

   private final ICPPFunctionType methodType;

   public MethodParamEquivalenceTester(final ICPPMethod method) {
      methodType = method.getType();
   }

   public boolean hasSameParameters(final ICPPMethod otherMethod) {
      final ICPPFunctionType otherMethodType = otherMethod.getType();

      if (!haveEqualNumOfParams(otherMethodType)) return false;

      for (int i = 0; i < methodType.getParameterTypes().length; i++) {
         final IType methodParamType = methodType.getParameterTypes()[i];
         final IType otherParamType = otherMethodType.getParameterTypes()[i];

         if (!methodParamType.isSameType(otherParamType)) return false;
      }

      return true;
   }

   private boolean haveEqualNumOfParams(final ICPPFunctionType otherMethodType) {
      return methodType.getParameterTypes().length == otherMethodType.getParameterTypes().length;
   }
}
