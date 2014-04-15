package ch.hsr.ifs.cute.elevator.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.codan.core.cxx.Activator;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;

public class DeclaratorCollector extends ASTVisitor {
    
    final private List<IASTDeclarator> declarators;
    
    public DeclaratorCollector() {
        declarators = new ArrayList<IASTDeclarator>();
        this.shouldVisitDeclarators = true;
    }
    
    @Override
    public int visit(IASTDeclarator declarator) {
        if (isFunctionDeclarator(declarator)) {
            return PROCESS_CONTINUE;
        }
        if (new DelaratorAnalyzer(declarator).isElevationCandidate() && !containsBoostAssign(declarator)) {
            declarators.add(declarator);
        }
        return PROCESS_CONTINUE;
    }
    
    public List<IASTDeclarator> getDeclarators() {
        return declarators;
    }
  
    private boolean containsBoostAssign(IASTDeclarator element) {
        if (!isEqualsInitializer(element.getInitializer())) {
            return false;
        }
        IASTInitializerClause clause = ((IASTEqualsInitializer) element.getInitializer()).getInitializerClause();
        if (!(clause instanceof IASTFunctionCallExpression)) {
            return false;
        }
        IASTFunctionCallExpression functionCall = (IASTFunctionCallExpression) clause;
        IASTExpression expr = functionCall.getFunctionNameExpression();
        if (!(expr instanceof IASTFunctionCallExpression)) {
            return false;
        }

        IASTName qualifiedFunctionName = ((IASTIdExpression) ((IASTFunctionCallExpression) expr).getFunctionNameExpression()).getName();
        IBinding functionNamebinding = qualifiedFunctionName.resolveBinding();
        IBinding owningNamespace = functionNamebinding.getOwner();
        try {
            String[] qualifiedNamespaceName = null;
            if (owningNamespace instanceof ICPPNamespace) {
                qualifiedNamespaceName = ((ICPPNamespace) owningNamespace).getQualifiedName();
            }
            if (qualifiedNamespaceName != null && qualifiedNamespaceName.length == 2 && qualifiedNamespaceName[0].equals("boost") && qualifiedNamespaceName[1].equals("assign")) {
                return true;
            }
        } catch (DOMException e) {
            Activator.log(e);
        }
        return false;
    }
    
    private boolean isFunctionDeclarator(IASTDeclarator element) {
        return element instanceof IASTFunctionDeclarator;
    }
    
    private boolean isEqualsInitializer(IASTInitializer initializer) {
        return initializer instanceof IASTEqualsInitializer;
    }
    

}
