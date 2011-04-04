package ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.action;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.TransformConfiguration;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util.TypeInformation;

/**
 * Action to propose a template name for the given types.
 * 
 * @author ythrier(at)hsr.ch
 */
public class NewTemplateChangeAction extends TemplateChangeAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public void applyChange(TransformConfiguration config,
            RefactoringStatus status) {
        proposeUniqueNames(config);
        for (TypeInformation type : config.getAllTypes()) {
            type.setDefaultType(getDefaultTypeOf(type, config, status));
        }
    }
}
