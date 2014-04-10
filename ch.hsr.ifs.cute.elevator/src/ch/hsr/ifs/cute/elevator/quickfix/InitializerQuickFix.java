package ch.hsr.ifs.cute.elevator.quickfix;

import org.eclipse.cdt.codan.core.cxx.Activator;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

import ch.hsr.ifs.cute.elevator.ast.ConstructorChainConverter;
import ch.hsr.ifs.cute.elevator.ast.DeclaratorConverter;

/**
 * quick fix
 */
public class InitializerQuickFix extends AbstractAstRewriteQuickFix {

    private IASTTranslationUnit ast;

    public String getLabel() {
        return "Replace with uniform variable initialization";
    }

    @Override
    public void modifyAST(final IIndex index, final IMarker marker) {
        try {
            int astFlags = ITranslationUnit.AST_SKIP_INDEXED_HEADERS | ITranslationUnit.AST_PARSE_INACTIVE_CODE;
            ast = getTranslationUnitViaEditor(marker).getAST(index, astFlags);
            IASTNode astNode = getAstNameFromMarker(marker);
            IASTNode targetStatement = getEnclosingNodeOfInterest(astNode);
            if (isDeclarator(targetStatement)) {
                IASTDeclarator newDeclarator = new DeclaratorConverter((IASTDeclarator) targetStatement).convert();
                performChange(targetStatement, newDeclarator);
            } else if (isConstructorChainInitializer(targetStatement)) {
                ICPPASTConstructorChainInitializer newInitializer = new ConstructorChainConverter((ICPPASTConstructorChainInitializer) targetStatement).convert();
                performChange(targetStatement, newInitializer);
            }
            marker.delete();
        } catch (CoreException e) {
            Activator.log(e);
        }
    }
    
    /**
     * recursively traverses up the AST node hierarchy until a node of interest (declarator or member initializer) is found.
     * 
     */
    private IASTNode getEnclosingNodeOfInterest(IASTNode node) {
        return (node.getParent() == null) || isDeclarator(node) || isConstructorChainInitializer(node) ? node : getEnclosingNodeOfInterest(node.getParent());
    }

    private boolean isConstructorChainInitializer(IASTNode node) {
        return node != null && node instanceof ICPPASTConstructorChainInitializer;
    }

    private boolean isDeclarator(IASTNode node) {
        return node != null && node instanceof IASTDeclarator;
    }

    private void performChange(IASTNode target, IASTNode replacement) throws CoreException {
        ASTRewrite rewrite = ASTRewrite.create(ast);
        rewrite.replace(target, replacement, null);
        Change change = rewrite.rewriteAST();
        change.perform(new NullProgressMonitor());
    }
    
    private IASTNode getAstNameFromMarker(IMarker marker) {
        int markerOffset = marker.getAttribute(IMarker.CHAR_START, -1);
        int markerLength = marker.getAttribute(IMarker.CHAR_END, -1) - markerOffset;   
        return ast.getNodeSelector(null).findEnclosingNode(markerOffset, markerLength);
    }
}
