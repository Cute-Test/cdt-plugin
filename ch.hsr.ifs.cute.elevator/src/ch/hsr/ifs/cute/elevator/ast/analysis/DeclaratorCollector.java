package ch.hsr.ifs.cute.elevator.ast.analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;

public class DeclaratorCollector extends ASTVisitor {
    
    final private List<IASTDeclarator> declarators;
    
    public DeclaratorCollector() {
        declarators = new ArrayList<IASTDeclarator>();
        this.shouldVisitDeclarators = true;
    }
    
    @Override
    public int visit(IASTDeclarator declarator) {
        if (isFunctionDeclarator(declarator)) {
            return PROCESS_SKIP;
        }
        if (new DelaratorAnalyzer(declarator).isElevationCandidate()) {
            declarators.add(declarator);
        }
        return PROCESS_CONTINUE;
    }
    
    public List<IASTDeclarator> getDeclarators() {
        return declarators;
    }
  
    private boolean isFunctionDeclarator(IASTDeclarator element) {
        return element instanceof IASTFunctionDeclarator;
    }
}
