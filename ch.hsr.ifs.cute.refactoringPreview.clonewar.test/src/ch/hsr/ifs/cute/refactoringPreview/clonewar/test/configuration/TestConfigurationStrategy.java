package ch.hsr.ifs.cute.refactoringPreview.clonewar.test.configuration;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring2;

/**
 * A test configuration strategy to change the configuration of a test
 * allowing to check different behaviour (e.g. of an user).
 * @author ythrier(at)hsr.ch
 */
@SuppressWarnings("restriction")
public interface TestConfigurationStrategy {

	/**
	 * Change the configuration of the refactoring to simulate different
	 * behavior.
	 * @param refactoring Refactoring to adjust config.
	 */
	public void changeConfiguration(CRefactoring2 refactoring);
}
