package ch.hsr.ifs.cute.elevator.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;

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
        if (isConstructorChainInitializer(initializer) && !isElevated(initializer)) {
            initializers.add((ICPPASTConstructorChainInitializer) initializer);
        }
        return PROCESS_SKIP;
    }

    private boolean isConstructorChainInitializer(IASTInitializer initializer) {
        return initializer instanceof ICPPASTConstructorChainInitializer;
    }

    private boolean isElevated(IASTInitializer initializer) {
        return ((ICPPASTConstructorChainInitializer) initializer).getInitializer() instanceof ICPPASTInitializerList;
    }

    public List<ICPPASTConstructorChainInitializer> getInitializers() {
        return initializers;
    }
}
