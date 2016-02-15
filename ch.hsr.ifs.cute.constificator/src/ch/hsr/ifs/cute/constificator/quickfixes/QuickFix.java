package ch.hsr.ifs.cute.constificator.quickfixes;

import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

import ch.hsr.ifs.cute.constificator.core.asttools.ASTRewriteCache;
import ch.hsr.ifs.cute.constificator.refactorings.MultiChangeRefactoring;
import ch.hsr.ifs.cute.constificator.refactorings.MultiChangeRefactoringWizard;

public abstract class QuickFix extends AbstractAstRewriteQuickFix {

    protected boolean hasMultipleChanges = false;

	@Override
	protected IIndex getIndexFromMarker(IMarker marker) throws CoreException {
		CCorePlugin.getIndexManager().joinIndexer(1000, new NullProgressMonitor());
		return super.getIndexFromMarker(marker);
	}
	
    @Override
    public void modifyAST(final IIndex index, final IMarker marker) {

        try {
        	ASTRewriteCache cache = new ASTRewriteCache(index);
            final ITranslationUnit tu = getTranslationUnitViaEditor(marker);
            final IASTTranslationUnit ast = cache.getASTTranslationUnit(tu);
            
    		int start = marker.getAttribute(IMarker.CHAR_START, -1);
    		int end = marker.getAttribute(IMarker.CHAR_END, -1);
    		final IASTNode node = ast.getNodeSelector(null).findNode(start, end - start);
            handleNode(node, index, cache);

            if (hasMultipleChanges) {
            	
                final MultiChangeRefactoring refactoring = new MultiChangeRefactoring(cache);
                final MultiChangeRefactoringWizard wizard = new MultiChangeRefactoringWizard(refactoring);
                final RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(wizard);
                int status = operation.run(null, "Add missing const qualifications");
                if (status != IDialogConstants.CANCEL_ID) {
                	marker.delete();
                }
            } else {
            	cache.getChange().perform(new NullProgressMonitor());
            	marker.delete();
            }
            
        } catch (CoreException | InterruptedException e) {
            e.printStackTrace();
        }
        
    }

    protected abstract void handleNode(IASTNode node, IIndex index, ASTRewriteCache cache);

}
