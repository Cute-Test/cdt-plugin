package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;


public class BaseClassCandidateVerifier {

   private final ICPPClassType classType;

   public BaseClassCandidateVerifier(ICPPClassType classType) {
      this.classType = classType;
   }

   public boolean isConsideredAsBaseClass() {
      return hasNonPrivateVirtualDtor(classType);
   }

   private static boolean hasNonPrivateVirtualDtor(ICPPClassType classType) {
      ICPPMethod dtor = getDtor(classType);

      if (dtor != null && dtor.isVirtual() && isNotPrivate(dtor)) return true;

      for (ICPPBase base : classType.getBases()) {
         IBinding baseClass = base.getBaseClass();

         if (baseClass instanceof ICPPClassType && hasNonPrivateVirtualDtor((ICPPClassType) baseClass)) return true;
      }

      return false;
   }

   private static ICPPMethod getDtor(ICPPClassType classType) {
      for (ICPPMethod method : classType.getDeclaredMethods()) {
         if (method.isDestructor()) return method;
      }
      return null;
   }

   private static boolean isNotPrivate(ICPPMethod memberFun) {
      return memberFun.getVisibility() != ICPPASTVisibilityLabel.v_private;
   }
}
