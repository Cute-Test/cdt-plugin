package ch.hsr.ifs.cute.elevator.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;

/**
 * ASTVisitor that collects all ConstructorChainInitializers.
 * 
 */
public class InitializerCollector extends ASTVisitor {

    private final List<ICPPASTConstructorChainInitializer> initializers;

    public InitializerCollector() {
        initializers = new ArrayList<ICPPASTConstructorChainInitializer>();
        shouldVisitInitializers = true;
    }

    @Override
    public int visit(IASTInitializer initializer) {
         if (initializer instanceof ICPPASTConstructorChainInitializer) {
            ICPPASTConstructorChainInitializer ctorInitializer = (ICPPASTConstructorChainInitializer) initializer;
            collectIfElevationCandidate(ctorInitializer);
        }
        return PROCESS_SKIP;
    }

   

    private void collectIfElevationCandidate(ICPPASTConstructorChainInitializer initializer) {
        if (!(isElevated(initializer) || isReference(initializer))) {
            initializers.add(initializer);
        }
    }

    private boolean isReference(ICPPASTConstructorChainInitializer initializer) {
        IASTName identifier = initializer.getMemberInitializerId();
        IBinding binding = identifier.resolveBinding();
        return binding instanceof IVariable && ((IVariable) binding).getType() instanceof ICPPReferenceType;
    }

    private boolean isElevated(ICPPASTConstructorChainInitializer initializer) {
        return initializer.getInitializer() instanceof ICPPASTInitializerList;
    }

    public List<ICPPASTConstructorChainInitializer> getInitializers() {
        return initializers;
    }
}
