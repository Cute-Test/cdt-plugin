package ch.hsr.ifs.cute.mockator.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;
import java.util.function.Consumer;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CRefactoringContext;

import ch.hsr.ifs.cute.mockator.project.nature.NatureHandler;
import ch.hsr.ifs.cute.mockator.project.properties.MarkMissingMemFuns;
import ch.hsr.ifs.iltis.testing.highlevel.testingplugin.cdttest.CDTTestingRefactoringTest;


@SuppressWarnings("restriction")
public abstract class AbstractRefactoringTest extends CDTTestingRefactoringTest {

    private static int DEFAULT_MARKER_COUNT = 1;
    protected int      markerCount;
    protected boolean  needsManagedCProject;
    protected boolean  newFileCreation;
    protected boolean  withCuteNature;
    protected boolean  fatalError;
    private String[]   newFiles;

    @Override
    protected void initAdditionalIncludes() throws Exception {
        stageExternalIncludePathsForBothProjects("mockator", "cute", "stl");
        super.initAdditionalIncludes();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setMockatorProjectOptions();

        if (needsManagedCProject) {
            activateManagedBuild();
        }

        if (withCuteNature) {
            addCuteNature();
        }
    }

    private void addCuteNature() throws CoreException {
        new NatureHandler(getCurrentProject()).addNature("ch.hsr.ifs.cute.ui.cutenature", new NullProgressMonitor());
    }

    protected IASTTranslationUnit getAst(final CRefactoringContext context) {
        try {
            return context.getAST(getTu(getPrimaryCElementFromCurrentProject().get()), new NullProgressMonitor());
        } catch (final CoreException e) {}
        fail("Not able to get AST for translation unit");
        return null;
    }

    protected ITranslationUnit getTu(final ICElement cElement) {
        final ISourceReference sourceRef = (ISourceReference) cElement;
        return CModelUtil.toWorkingCopy(sourceRef.getTranslationUnit());
    }

    private void setMockatorProjectOptions() {
        MarkMissingMemFuns.storeInProjectSettings(getCurrentProject(), MarkMissingMemFuns.AllMemFuns);
    }

    private void activateManagedBuild() throws CoreException {
        final CdtManagedProjectActivator expectedConfigurator = new CdtManagedProjectActivator(getExpectedProject());
        expectedConfigurator.activateManagedBuild();
        final CdtManagedProjectActivator currentConfigurator = new CdtManagedProjectActivator(getCurrentProject());
        currentConfigurator.activateManagedBuild();
    }

    @Test
    public void runTest() throws Throwable {
        closeWelcomeScreen();
        if (newFileCreation && !fatalError) {
            ensureNewFilesDoNotExist();
        }
        //      openPrimaryTestFileInEditor(); //FIXME check if this is needed
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
    protected void configureTest(final Properties properties) {
        markerCount = Integer.valueOf(properties.getProperty("markerCount", Integer.toString(DEFAULT_MARKER_COUNT)));
        expectedFinalWarnings = Integer.parseInt(properties.getProperty("expectedFinalWarnings", "0"));
        fatalError = Boolean.valueOf(properties.getProperty("fatalError", "false")).booleanValue();
        newFileCreation = Boolean.valueOf(properties.getProperty("newFileCreation", "false")).booleanValue();
        newFiles = getNewFiles(properties);
        needsManagedCProject = Boolean.valueOf(properties.getProperty("needsManagedCProject", "false")).booleanValue();
        withCuteNature = Boolean.valueOf(properties.getProperty("withCuteNature", "false")).booleanValue();
        super.configureTest(properties);
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
                currentProjectHolder.getFile(filePath).delete(true, new NullProgressMonitor());
            } catch (final CoreException e) {}
        });
    }

    private void filesDoExist() {
        executeOnNewFiles((filePath) -> assertTrue(currentProjectHolder.getFile(filePath).exists()));
    }

    private void filesDoNotExist() {
        executeOnNewFiles((filePath) -> assertFalse(currentProjectHolder.getFile(filePath).exists()));
    }

    private void executeOnNewFiles(final Consumer<String> f) {
        for (final String file : newFiles) {
            f.accept(file);
        }
    }
}
