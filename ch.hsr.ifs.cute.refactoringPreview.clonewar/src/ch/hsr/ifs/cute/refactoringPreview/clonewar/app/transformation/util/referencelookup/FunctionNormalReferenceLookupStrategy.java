package ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util.referencelookup;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.core.runtime.CoreException;

/**
 * Lookup strategy for function references (calls).
 *
 * @author ythrier(at)hsr.ch
 */
public class FunctionNormalReferenceLookupStrategy extends
        AbstractReferenceLookupStrategy<ICPPASTFunctionCallExpression> {

    public FunctionNormalReferenceLookupStrategy(RefactoringASTCache astCache) {
        super(astCache);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processCandidates(IASTName name, IIndex index,
            List<ICPPASTFunctionCallExpression> calls) throws CoreException {
        IIndexName[] callIndexes = index.findNames(getBinding(index, name),
                IIndex.FIND_ALL_OCCURRENCES
                        | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
        for (IIndexName callIndex : callIndexes)
            processCall(callIndex, calls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isReferenceExpression(IASTNode node) {
        return (node instanceof ICPPASTFunctionCallExpression);
    }
}