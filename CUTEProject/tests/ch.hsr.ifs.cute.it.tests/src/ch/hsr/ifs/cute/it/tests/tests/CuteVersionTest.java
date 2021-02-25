package ch.hsr.ifs.cute.it.tests.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.hsr.ifs.iltis.core.resources.IOUtil;

import ch.hsr.ifs.iltis.cpp.versionator.definition.CPPVersion;

import ch.hsr.ifs.cute.headers.ICuteHeaders;
import ch.hsr.ifs.cute.headers.versions.CuteHeaders2;
import ch.hsr.ifs.cute.it.tests.annotations.TestProjectCategory;
import ch.hsr.ifs.cute.it.tests.annotations.TestProjectType;
import ch.hsr.ifs.cute.it.tests.base.AutomatedUITest;
import ch.hsr.ifs.cute.it.tests.base.AutomatedUITestRunner;
import ch.hsr.ifs.cute.it.tests.util.BotConditions;


@TestProjectCategory("CUTE")
@RunWith(AutomatedUITestRunner.class)
public class CuteVersionTest extends AutomatedUITest {

    private void assertCuteVersion(ICProject project, ICuteHeaders cuteVersion) throws Exception {
        String cuteVersionFile = "cute/cute_version.h";
        IFile file = getFile(project, cuteVersionFile);
        String content = IOUtil.FileIO.read(file);
        assertTrue("Cute version " + cuteVersion.getVersionNumber() + " not found in " + cuteVersionFile + ".", content.contains(
                "#define CUTE_LIB_VERSION \"" + cuteVersion.getVersionNumber() + "\""));
    }

    @Test
    @TestProjectType("CUTE Project")
    public void newProjectDefaultVersion() throws Exception {
        ICProject project = createProject();
        final ICuteHeaders defaultCuteVersion = ICuteHeaders.getDefaultHeaders(CPPVersion.getForProject(project.getProject()));

        ICuteHeaders cuteVersion = ICuteHeaders.getForProject(project.getProject());
        //TODO adjust once a new Cute version was included
        assertTrue(cuteVersion == CuteHeaders2._2_1);
        assertCuteVersion(project, defaultCuteVersion);
    }

    private void setCuteVersion(final ICuteHeaders cuteVersion) {
        fBot.button("Next >").click();
        SWTBotCombo versionComboBox = fBot.comboBoxWithLabel("CUTE Version:");
        fBot.waitUntil(BotConditions.comboBoxHasEntries(versionComboBox));
        versionComboBox.setSelection(CUTE_HEADERS_PREFIX + cuteVersion.getVersionNumber());
    }

    private static final String CUTE_HEADERS_PREFIX = "CUTE Headers ";

    @Test
    @TestProjectType("CUTE Project")
    public void newProjectV221() throws Exception {
        ICuteHeaders expectedVersion = CuteHeaders2._2_1;
        assertEquals("2.2.1", expectedVersion.getVersionNumber());

        ICProject project = createProject(() -> setCuteVersion(expectedVersion));
        ICuteHeaders cuteVersion = ICuteHeaders.getForProject(project.getProject());
        assertTrue(cuteVersion == expectedVersion);
        assertCuteVersion(project, expectedVersion);
    }

}
