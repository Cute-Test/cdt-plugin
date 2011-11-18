package ch.hsr.ifs.cute.refactoringPreview.clonewar.test.configuration;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring2;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.CloneWarRefactoring;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.TransformConfiguration;

/**
 * Abstract configuration strategy with helper methods.
 * 
 * @author ythrier(at)hsr.ch
 */
@SuppressWarnings("restriction")
public abstract class AbstractConfigurationStrategy implements	TestConfigurationStrategy {

	/**
	 * Return the config of the refactoring.
	 * @param refactoring Refactoring.
	 * @return Refactoring config.
	 */
	protected TransformConfiguration getConfig(CRefactoring2 refactoring) {
		return ((CloneWarRefactoring)refactoring).getTransformation().getConfig();
	}
}