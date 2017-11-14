package ch.hsr.ifs.mockator.tests;

import java.util.Properties;
import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

import ch.hsr.ifs.iltis.cpp.wrappers.CRefactoringContext;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingRefactoringTest;
import ch.hsr.ifs.cdttesting.testsourcefile.TestSourceFile;
import ch.hsr.ifs.mockator.plugin.project.nature.NatureHandler;
import ch.hsr.ifs.mockator.plugin.project.properties.MarkMissingMemFuns;


public abstract class MockatorRefactoringTest extends CDTTestingRefactoringTest {

   private static int DEFAULT_MARKER_COUNT = 1;
   protected int      markerCount;
   protected boolean  needsManagedCProject;
   protected boolean  newFileCreation;
   protected boolean  withCuteNature;
   protected boolean  fatalError;
   private String[]   newFiles;

   @Override
   public void setUp() throws Exception {
      addMockatorIncludePaths();
      super.setUp();
      setFormatterOptions();
      setMockatorProjectOptions();

      if (needsManagedCProject) {
         activateManagedBuild();
      }

      if (withCuteNature) {
         addCuteNature();
      }
   }

   private void addCuteNature() throws CoreException {
      new NatureHandler(project).addNature("ch.hsr.ifs.cute.ui.cutenature", new NullProgressMonitor());
   }

   private void addMockatorIncludePaths() {
      for (final String includePath : new String[] { "mockator", "cute", "stl" }) {
         addIncludeDirPath(includePath);
      }
   }

   protected IASTTranslationUnit getAst(final CRefactoringContext context) {
      try {
         return context.getAST(getTu(getActiveCElement()), new NullProgressMonitor());
      }
      catch (final CoreException e) {}
      fail("Not able to get AST for translation unit");
      return null;
   }

   protected ITranslationUnit getTu(final ICElement cElement) {
      final ISourceReference sourceRef = (ISourceReference) cElement;
      return CModelUtil.toWorkingCopy(sourceRef.getTranslationUnit());
   }

   private void setMockatorProjectOptions() {
      MarkMissingMemFuns.storeInProjectSettings(project, MarkMissingMemFuns.AllMemFuns);
   }

   private void setFormatterOptions() {
      new FormatterOptionsLoader(cproject).setFormatterOptions();
   }

   @Override
   protected void compareFiles() throws Exception {
      for (final TestSourceFile testFile : fileMap.values()) {
         final String expectedSource = testFile.getExpectedSource();
         final String actualSource = getCurrentSource(testFile.getName());
         new AssertThat(actualSource).isEqualByIgnoringWhitespace(expectedSource);
      }
   }

   @Override
   protected String makeProjectAbsolutePath(final String relativePath) {
      final IPath projectPath = project.getLocation();
      return projectPath.append(relativePath).toOSString();
   }

   private void activateManagedBuild() throws CoreException {
      final CdtManagedProjectActivator configurator = new CdtManagedProjectActivator(cproject.getProject());
      configurator.activateManagedBuild();
   }

   @Override
   @Test
   public void runTest() throws Throwable {
      closeWelcomeScreen();
      if (newFileCreation && !fatalError) {
         ensureNewFilesDoNotExist();
      }
      openActiveFileInEditor();
      if (fatalError) {
         runRefactoringAndAssertFailure();
      } else {
         runRefactoringAndAssertSuccess();
      }
      if (newFileCreation) {
         filesDoExist();
      }
   }

   @Override
   protected void configureTest(final Properties refProps) {
      markerCount = Integer.valueOf(refProps.getProperty("markerCount", Integer.toString(DEFAULT_MARKER_COUNT)));
      expectedFinalWarnings = Integer.parseInt(refProps.getProperty("expectedFinalWarnings", "0"));
      fatalError = Boolean.valueOf(refProps.getProperty("fatalError", "false")).booleanValue();
      newFileCreation = Boolean.valueOf(refProps.getProperty("newFileCreation", "false")).booleanValue();
      newFiles = getNewFiles(refProps);
      needsManagedCProject = Boolean.valueOf(refProps.getProperty("needsManagedCProject", "false")).booleanValue();
      withCuteNature = Boolean.valueOf(refProps.getProperty("withCuteNature", "false")).booleanValue();
   }

   @SuppressWarnings("nls")
   private static String[] getNewFiles(final Properties refactoringProperties) {
      return String.valueOf(refactoringProperties.getProperty("newFiles", "")).replaceAll(" ", "").split(",");
   }

   private void ensureNewFilesDoNotExist() throws Exception {
      removeFiles();
      filesDoNotExist();
   }

   private void removeFiles() {
      executeOnNewFiles((filePath) -> {
         try {
            getFile(filePath).delete(true, new NullProgressMonitor());
         }
         catch (final CoreException e) {}
      });
   }

   private void filesDoExist() {
      executeOnNewFiles((filePath) -> assertTrue(getFile(filePath).exists()));
   }

   private void filesDoNotExist() {
      executeOnNewFiles((filePath) -> assertFalse(getFile(filePath).exists()));
   }

   private void executeOnNewFiles(final Consumer<String> f) {
      for (final String file : newFiles) {
         f.accept(file);
      }
   }

   private IFile getFile(final String filePath) {
      return project.getFile(new Path(filePath));
   }
}
