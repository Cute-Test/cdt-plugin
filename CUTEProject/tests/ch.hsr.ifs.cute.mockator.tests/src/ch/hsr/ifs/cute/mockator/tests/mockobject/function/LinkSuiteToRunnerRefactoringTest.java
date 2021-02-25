package ch.hsr.ifs.cute.mockator.tests.mockobject.function;

import static ch.hsr.ifs.iltis.core.collections.CollectionUtil.head;

import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.cute.mockator.mockobject.function.suite.refactoring.LinkSuiteToRunnerRefactoring;
import ch.hsr.ifs.cute.mockator.mockobject.function.suite.refactoring.RunnerFinder;
import ch.hsr.ifs.cute.mockator.tests.AbstractRefactoringTest;


public class LinkSuiteToRunnerRefactoringTest extends AbstractRefactoringTest {

    private String newSuiteName;

    @Override
    protected void configureTest(final Properties p) {
        super.configureTest(p);
        newSuiteName = p.getProperty("newSuiteName");
        markerCount = 0;
    }

    @Override
    protected Refactoring createRefactoring() {
        try {
            final LinkSuiteToRunnerRefactoring runnerRefactoring = new LinkSuiteToRunnerRefactoring(getPrimaryCElementFromCurrentProject().get(),
                    getSelectionOfPrimaryTestFile(), getCurrentCProject());
            runnerRefactoring.setSuiteName(newSuiteName);
            runnerRefactoring.setTestRunner(getTestRunnerFunction());
            runnerRefactoring.setDestinationPath(getCurrentCProject().getPath());
            return runnerRefactoring;
        } catch (final CoreException e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    private IASTFunctionDefinition getTestRunnerFunction() throws CoreException {
        final RunnerFinder runnerFinder = new RunnerFinder(getCurrentCProject());
        final List<IASTFunctionDefinition> testRunners = runnerFinder.findTestRunners(new NullProgressMonitor());

        if (testRunners.isEmpty()) return null;

        return head(testRunners).get();
    }
}
