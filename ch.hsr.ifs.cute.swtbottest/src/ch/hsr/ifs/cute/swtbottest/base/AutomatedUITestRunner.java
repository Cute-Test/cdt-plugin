package ch.hsr.ifs.cute.swtbottest.base;


import static org.junit.Assert.fail;

import java.io.InvalidClassException;
import java.lang.reflect.Field;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;

import ch.hsr.ifs.cute.swtbottest.annotations.TestProjectCategory;
import ch.hsr.ifs.cute.swtbottest.annotations.TestProjectName;
import ch.hsr.ifs.cute.swtbottest.annotations.TestProjectType;

/**
 * @author Felix Morgner IFS
 * @author Hansruedi Patzen IFS
 *
 */
public class AutomatedUITestRunner extends SWTBotJunit4ClassRunner {

	private Class<AutomatedUITest> fAutomatedUITestClass;

	public AutomatedUITestRunner(Class<?> testClass) throws Exception {
		super(testClass);
		fAutomatedUITestClass = findAutomatedUITest(testClass);
		handleAnnotation("fTestClassName", getTestClass().getName());
		handleProjectCategory(testClass);
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		handleProjectType(method);
		handleProjectName(method);
		super.runChild(method, notifier);
	}

	private void handleProjectCategory(Class<?> cls) {
		TestProjectCategory type = cls.getAnnotation(TestProjectCategory.class);
		if (type != null) {
			handleAnnotation("fProjectCategory", type.value());
		} else {
			fail("Test is missing project category annotation!");
		}
	}

	private void handleProjectType(FrameworkMethod method) {
		TestProjectType type = method.getAnnotation(TestProjectType.class);
		if (type != null) {
			handleAnnotation("fProjectType", type.value());
		} else {
			fail("Test is missing project type annotation!");
		}
	}

	private void handleProjectName(FrameworkMethod method) {
		TestProjectName name = method.getAnnotation(TestProjectName.class);
		handleAnnotation("fProjectName", name != null ? name.value() : method.getName());
	}

	private void handleAnnotation(String fieldName, String annotationValue) {
		try {
			Field projectTypeField = fAutomatedUITestClass.getDeclaredField(fieldName);
			projectTypeField.setAccessible(true);
			projectTypeField.set(null, annotationValue);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			fail("Failed to initialze project properties!");
		}
	}

	@SuppressWarnings("unchecked")
	private Class<AutomatedUITest> findAutomatedUITest(Class<?> cls) throws Exception {
		while (cls != null && cls != AutomatedUITest.class) {
			cls = cls.getSuperclass();
		}

		if (cls == null) {
			throw new InvalidClassException("Expected AutomatedUITest to be part of the class hierarchy");
		}

		return (Class<AutomatedUITest>) cls;
	}

}
