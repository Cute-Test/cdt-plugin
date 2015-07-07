package ch.hsr.ifs.cute.elevenator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;

import ch.hsr.ifs.cute.elevenator.definition.IVersionModificationOperation;

public class EvaluateContributions {
	private static final String IVERSIONMODIFICATOR_ID = "ch.hsr.ifs.cute.elevenator.versionmodification";

	public static void evaluateAll() {

		IExtensionRegistry registry = Platform.getExtensionRegistry();

		IConfigurationElement[] config = registry.getConfigurationElementsFor(IVERSIONMODIFICATOR_ID);
		try {
			for (IConfigurationElement e : config) {
				System.out.println("Evaluating extension");
				final Object o = e.createExecutableExtension("class");
				if (o instanceof IVersionModificationOperation) {
					executeExtension(o);
				}
			}
		} catch (CoreException ex) {
			System.out.println(ex.getMessage());
		}
	}

	private static void executeExtension(final Object o) {
		ISafeRunnable runnable = new ISafeRunnable() {
			@Override
			public void handleException(Throwable e) {
				System.out.println("Exception in client");
			}

			@Override
			public void run() throws Exception {
				((IVersionModificationOperation) o).perform();
			}
		};
		SafeRunner.run(runnable);
	}
}
