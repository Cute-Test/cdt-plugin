package ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util.referencelookup;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.internal.ui.viewsupport.IndexUI;
import org.eclipse.core.runtime.CoreException;

/**
 * Lookup strategy for function references (calls) of specialized (already
 * template) functions.
 * 
 * @author ythrier(at)hsr.ch
 */
@SuppressWarnings("restriction")
public class FunctionSpecializedReferenceLookupStrategy extends
        AbstractReferenceLookupStrategy<ICPPASTFunctionCallExpression> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processCandidates(IASTName name, IIndex index,
            List<ICPPASTFunctionCallExpression> calls) throws CoreException {
        for (IBinding binding : (List<? extends IBinding>) findSpecializations(
                name, index)) {
            IIndexName[] callIndexes = index.findNames(binding,
                    IIndex.FIND_ALL_OCCURRENCES);
            for (IIndexName callIndex : callIndexes)
                processCall(callIndex, calls);
        }
    }

    /**
     * Find the specialized function calls.
     * 
     * @param name
     *            AST Name.
     * @param index
     *            Index entry.
     * @return List of specialized candidates.
     * @throws CoreException
     *             Core exception.
     */
    private List<? extends IBinding> findSpecializations(IASTName name,
            IIndex index) throws CoreException {
        return IndexUI.findSpecializations(getBinding(index, name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isReferenceExpression(IASTNode node) {
        return (node instanceof ICPPASTFunctionCallExpression);
    }
}
