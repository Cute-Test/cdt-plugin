package ch.hsr.ifs.cute.refactoringpreview.clonewar.test.tests;

import java.util.Properties;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.ltk.core.refactoring.RefactoringContext;

import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.action.TransformAction;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.configuration.TransformConfiguration;
import ch.hsr.ifs.cute.refactoringPreview.clonewar.app.transformation.util.TypeInformation;
import ch.hsr.ifs.cute.refactoringpreview.clonewar.test.CloneWarTest;

public class ExtractClassTemplateTest extends CloneWarTest {
	protected final String DEFAULT_ARGUMENTS_KEY = "defaultArguments";
	protected final String NON_NESTED_ONLY_KEY = "nonNestedOnly";

	protected boolean defaultArguments;
	protected boolean nonNestedOnly;

	@Override
	protected void configureTest(Properties properties) {
		defaultArguments = Boolean.parseBoolean(properties.getProperty(DEFAULT_ARGUMENTS_KEY, "false"));
		nonNestedOnly = Boolean.parseBoolean(properties.getProperty(NON_NESTED_ONLY_KEY, "false"));
		super.configureTest(properties);
	}

	@Override
	protected void simulateUserInput(RefactoringContext context) {
		TransformConfiguration config = refactoring.getTransformation().getConfig();
		for (TypeInformation type : config.getAllTypes()) {
			type.setDefaulting(defaultArguments);
		}

		if (nonNestedOnly) {
			for (TransformAction action : config.getAllActions()) {
				if ((action.getNode().getParent() instanceof ICPPASTTypeId))
					action.setPerform(false);
			}
		}
		super.simulateUserInput(context);
	}
}
