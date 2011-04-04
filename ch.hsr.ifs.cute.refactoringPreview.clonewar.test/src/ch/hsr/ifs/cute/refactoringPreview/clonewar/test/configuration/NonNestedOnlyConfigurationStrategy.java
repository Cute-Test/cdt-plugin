package ch.hsr.ifs.cute.refactoringPreview.clonewar.test.configuration;

import java.util.List;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.action.TransformAction;

/**
 * Configuration strategy to change the actions to only change the
 * non nested types (non type id types).
 * 
 * @author ythrier(at)hsr.ch
 */
@SuppressWarnings("restriction")
public class NonNestedOnlyConfigurationStrategy extends AbstractConfigurationStrategy {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void changeConfiguration(CRefactoring refactoring) {
		List<TransformAction> actions = getActions(refactoring);
		for(TransformAction action : actions) {
			if((action.getNode().getParent() instanceof CPPASTTypeId))
				action.setPerform(false);
		}
	}

	/**
	 * Return a list of all actions.
	 * @param refactoring Refactoring.
	 * @return List of actions.
	 */
	private List<TransformAction> getActions(CRefactoring refactoring) {
		return getConfig(refactoring).getAllActions();
	}
}
