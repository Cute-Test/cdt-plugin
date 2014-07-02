package ch.hsr.ifs.cute.macronator.test.common;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.Test;

import ch.hsr.ifs.cdttesting.cdttest.CDTTestingTest;
import ch.hsr.ifs.cute.macronator.MacronatorPlugin;
import ch.hsr.ifs.cute.macronator.common.SuppressedMacros;

public class SuppressedMacrosTest extends CDTTestingTest {

    private ProjectScope projectScope;

    @Test
    @Override
    public void runTest() throws Throwable {
        projectScope = new ProjectScope(cproject.getProject());
        IEclipsePreferences node = projectScope.getNode(MacronatorPlugin.PLUGIN_ID);
        node.put(MacronatorPlugin.SUPPRESSED_MACROS_PREF_KEY, "SUPPRESSED_MACRO_1;SUPPRESSED_MACRO_2");
        SuppressedMacros suppressedMacros = new SuppressedMacros(project);
        assertFalse(suppressedMacros.isSuppressed("UNSUPPRESSED_MACRO"));
        assertTrue(suppressedMacros.isSuppressed("SUPPRESSED_MACRO_1"));
        assertTrue(suppressedMacros.isSuppressed("SUPPRESSED_MACRO_2"));
        suppressedMacros.remove("SUPPRESSED_MACRO_1");
        assertFalse(suppressedMacros.isSuppressed("SUPPRESSED_MACRO_1"));
        suppressedMacros.add("MACRO");
        assertTrue(suppressedMacros.isSuppressed("MACRO"));
    }
}
