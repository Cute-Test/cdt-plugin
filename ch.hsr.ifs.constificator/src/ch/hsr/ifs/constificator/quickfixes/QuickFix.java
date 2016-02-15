package ch.hsr.ifs.constificator.quickfixes;

import java.util.ArrayList;

import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;

import ch.hsr.ifs.constificator.refactorings.MultiChangeRefactoring;
import ch.hsr.ifs.constificator.refactorings.MultiChangeRefactoringWizard;

public abstract class QuickFix extends AbstractAstRewriteQuickFix {

    private final ArrayList<Change> changes = new ArrayList<>();

    protected boolean hasMultipleChanges = false;

    @Override
    public void modifyAST(final IIndex index, final IMarker marker) {
        try {
            final ITranslationUnit tu = getTranslationUnitViaEditor(marker);
            final IASTTranslationUnit ast = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
            final IASTNodeSelector selector = ast.getNodeSelector(ast.getFilePath());

            final int nodeOffset = getOffset(marker, getDocument());
            final int nodeLength = Integer.parseInt(getProblemArgument(marker, 1));

            final IASTNode node = selector.findEnclosingNode(nodeOffset, nodeLength);
            handleNode(node, index);

            if (changes.isEmpty()) {
                return;
            }

            if (hasMultipleChanges) {
                final MultiChangeRefactoring refactoring = new MultiChangeRefactoring(changes);
                final MultiChangeRefactoringWizard wizard = new MultiChangeRefactoringWizard(refactoring);
                final RefactoringWizardOpenOperation operation = new RefactoringWizardOpenOperation(wizard);
                operation.run(null, "Add missing const qualifications");
            } else {
                changes.get(0).perform(new NullProgressMonitor());
            }
        } catch (CoreException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            changes.clear();
        }
    }

    protected abstract void handleNode(IASTNode node, IIndex index);

    public void addChange(final Change change) {
        changes.add(change);
    }

}
