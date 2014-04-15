package ch.hsr.ifs.cute.elevator.quickfix;

import org.eclipse.cdt.codan.core.cxx.Activator;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

import ch.hsr.ifs.cute.elevator.ast.ConstructorChainConverter;
import ch.hsr.ifs.cute.elevator.ast.DeclaratorConverter;
import ch.hsr.ifs.cute.elevator.ast.NewExpressionConverter;
import ch.hsr.ifs.cute.elevator.ast.NodeProperties;

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
            NodeProperties astNodeProperties = new NodeProperties(astNode);
            
            if (astNodeProperties.hasAncestor(ICPPASTNewExpression.class)) {               
                ICPPASTNewExpression expression = astNodeProperties.getAncestor(ICPPASTNewExpression.class);                
                ICPPASTNewExpression convertedExpression = new NewExpressionConverter(expression).convert();             
                performChange(expression, convertedExpression);
            } else if (astNodeProperties.hasAncestor(IASTDeclarator.class)) {
                IASTDeclarator declarator = astNodeProperties.getAncestor(IASTDeclarator.class);
                IASTDeclarator convertedDeclarator = new DeclaratorConverter(declarator).convert();
                performChange(declarator, convertedDeclarator);
                
            } else if (astNodeProperties.hasAncestor(ICPPASTConstructorChainInitializer.class)) {
                ICPPASTConstructorChainInitializer initializer = astNodeProperties.getAncestor(ICPPASTConstructorChainInitializer.class);
                ICPPASTConstructorChainInitializer convertedInitializer = new ConstructorChainConverter(initializer).convert();
                performChange(initializer, convertedInitializer);
            }
            marker.delete();
        } catch (CoreException e) {
            Activator.log(e);
        }
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
