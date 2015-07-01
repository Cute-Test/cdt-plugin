package ch.hsr.ifs.cute.elevator.ast.analysis.conditions;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

import ch.hsr.ifs.cute.elevator.Activator;

/**
 * Checks if type of the supplied node has a constructor with {@link IASTInitializerList} argument.
 */
public class HasInitializerListConstructor extends Condition {
	// TODO: duplicated code with HasDefaultConstructor

    @Override
    public boolean satifies(final IASTNode node) {
        if (node instanceof IASTImplicitNameOwner) {
            return hasInitializerListConstructor((IASTImplicitNameOwner) node);
        }
        return false;
    }

    private boolean hasInitializerListConstructor(IASTImplicitNameOwner implicitNameOwner) {
        IASTImplicitName[] implicitNames = implicitNameOwner.getImplicitNames();
        for (IASTImplicitName name : implicitNames) {
            if (containsInitializerList(name.getBinding())) {
                return true;
            }
        }
        return false;
    }

    private boolean containsInitializerList(IBinding binding) {
        if (binding instanceof ICPPConstructor) {
            ICPPConstructor[] constructors = ((ICPPConstructor) binding).getClassOwner().getConstructors();
            for (ICPPConstructor constructor : constructors) {
                if (cotainsInitializerList(constructor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean cotainsInitializerList(ICPPConstructor constructor) {
        for (ICPPParameter parameter : constructor.getParameters()) {
            if (parameter.getType() instanceof ICPPClassType) {
                ICPPClassType type = (ICPPClassType) parameter.getType();
                try {
                    return type.getScope().toString().equals("std") && type.getName().startsWith("initializer_list");
                } catch (DOMException e) {
                    Activator.log(e);
                }
            }
        }
        return false;
    }
}
