package ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.action;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.TransformConfiguration;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util.TypeInformation;

/**
 * Action to change the ordering of a type transformation.
 * 
 * @author ythrier(at)hsr.ch
 */
public class TypeOrderingChangeAction implements IConfigChangeAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyChange(TransformConfiguration config,
            RefactoringStatus status) {
        int i = 0;
        for (TypeInformation type : config.getAllTypes())
            type.setOrderId(i++);
    }
}
