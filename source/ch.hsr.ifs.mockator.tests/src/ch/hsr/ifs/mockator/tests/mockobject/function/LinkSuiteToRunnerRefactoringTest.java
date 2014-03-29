package ch.hsr.ifs.mockator.tests.mockobject.function;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.head;

import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Refactoring;

import ch.hsr.ifs.mockator.plugin.mockobject.function.suite.refactoring.LinkSuiteToRunnerRefactoring;
import ch.hsr.ifs.mockator.plugin.mockobject.function.suite.refactoring.RunnerFinder;
import ch.hsr.ifs.mockator.tests.MockatorRefactoringTest;

public class LinkSuiteToRunnerRefactoringTest extends MockatorRefactoringTest {
  private String newSuiteName;

  @Override
  protected void configureTest(Properties p) {
    super.configureTest(p);
    newSuiteName = p.getProperty("newSuiteName");
    markerCount = 0;
  }

  @Override
  protected Refactoring createRefactoring() {
    try {
      LinkSuiteToRunnerRefactoring runnerRefactoring =
          new LinkSuiteToRunnerRefactoring(getActiveCElement(), selection, cproject);
      runnerRefactoring.setSuiteName(newSuiteName);
      runnerRefactoring.setTestRunner(getTestRunnerFunction());
      runnerRefactoring.setDestinationPath(cproject.getPath());
      return runnerRefactoring;
    } catch (CoreException e) {
      throw new AssertionError(e.getMessage(), e);
    }
  }

  private IASTFunctionDefinition getTestRunnerFunction() throws CoreException {
    RunnerFinder runnerFinder = new RunnerFinder(cproject);
    List<IASTFunctionDefinition> testRunners =
        runnerFinder.findTestRunners(new NullProgressMonitor());

    if (testRunners.isEmpty())
      return null;

    return head(testRunners).get();
  }
}
