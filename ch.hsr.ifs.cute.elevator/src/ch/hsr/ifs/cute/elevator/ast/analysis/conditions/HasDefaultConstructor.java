package ch.hsr.ifs.cute.elevator.ast.analysis.conditions;

import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructor;

/**
 * Checks if type of the supplied node has a constructor with {@link IASTInitializerList} argument.
 */
public class HasDefaultConstructor extends Condition {

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
        if (binding instanceof CPPConstructor) {
            ICPPConstructor[] constructors = ((CPPConstructor) binding).getClassOwner().getConstructors();
            for (ICPPConstructor constructor : constructors) {
                if (constructor.getParameters().length == 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
