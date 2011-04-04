package ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.action;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.TransformConfiguration;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util.TypeInformation;

/**
 * Action to change the ordering of a function transformation. The action
 * proposes an ordering in which the return type is the first template
 * parameter.
 * 
 * @author ythrier(at)hsr.ch
 */
public class FunctionOrderingChangeAction implements ConfigChangeAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyChange(TransformConfiguration config,
            RefactoringStatus status) {
        int i = 1;
        for (TypeInformation type : config.getAllTypes()) {
            if (config.hasReturnTypeAction(type))
                type.setOrderId(0);
            else
                type.setOrderId(i++);
        }
    }
}
