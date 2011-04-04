package ch.hsr.ifs.cute.refactoringPreview.clonewar.test.configuration;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util.TypeInformation;

/**
 * Strategy to disable type defaulting.
 * 
 * @author ythrier(at)hsr.ch
 */
@SuppressWarnings("restriction")
public class NoDefaultingConfigurationStrategy extends AbstractConfigurationStrategy {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void changeConfiguration(CRefactoring refactoring) {
		for(TypeInformation type : getConfig(refactoring).getAllTypes())
			type.setDefaulting(false);
	}
}
