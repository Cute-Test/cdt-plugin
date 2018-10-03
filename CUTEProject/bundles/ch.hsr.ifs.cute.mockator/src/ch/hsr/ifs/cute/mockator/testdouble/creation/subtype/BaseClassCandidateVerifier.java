package ch.hsr.ifs.cute.mockator.testdouble.creation.subtype;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;


public class BaseClassCandidateVerifier {

    private final ICPPClassType classType;

    public BaseClassCandidateVerifier(final ICPPClassType classType) {
        this.classType = classType;
    }

    public boolean isConsideredAsBaseClass() {
        return hasNonPrivateVirtualDtor(classType);
    }

    private static boolean hasNonPrivateVirtualDtor(final ICPPClassType classType) {
        final ICPPMethod dtor = getDtor(classType);

        if (dtor != null && dtor.isVirtual() && isNotPrivate(dtor)) return true;

        for (final ICPPBase base : classType.getBases()) {
            final IBinding baseClass = base.getBaseClass();

            if (baseClass instanceof ICPPClassType && hasNonPrivateVirtualDtor((ICPPClassType) baseClass)) return true;
        }

        return false;
    }

    private static ICPPMethod getDtor(final ICPPClassType classType) {
        for (final ICPPMethod method : classType.getDeclaredMethods()) {
            if (method.isDestructor()) return method;
        }
        return null;
    }

    private static boolean isNotPrivate(final ICPPMethod memberFun) {
        return memberFun.getVisibility() != ICPPASTVisibilityLabel.v_private;
    }
}
