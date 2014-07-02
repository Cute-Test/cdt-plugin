package ch.hsr.ifs.cute.macronator.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import ch.hsr.ifs.cute.macronator.MacronatorPlugin;

public class SuppressedMacros {

    private Set<String> suppressedMacros;
    private final Preferences projectPreferences;

    public SuppressedMacros(IProject project) {
        this.projectPreferences = getProjectNode(project);
        this.suppressedMacros = new HashSet<String>();
    }

    public void add(String macroName) {
        if (macroName.length() > 0) {
            suppressedMacros.add(macroName);
        }
        persistSuppressedMacros();
    }

    public void remove(String macroName) {
        suppressedMacros.remove(macroName);
        persistSuppressedMacros();
    }

    public boolean isSuppressed(String macroName) {
        readSuppressedMacros();
        return suppressedMacros.contains(macroName);
    }

    private void readSuppressedMacros() {
        String suppressedMacroProperty = projectPreferences.get(MacronatorPlugin.SUPPRESSED_MACROS_PREF_KEY, "");
        String[] split = suppressedMacroProperty.split(";");
        suppressedMacros = new HashSet<String>(Arrays.asList(split));
    }

    private Preferences getProjectNode(IProject project) {
        IScopeContext projectScope = new ProjectScope(project);
        Preferences projectNode = projectScope.getNode(MacronatorPlugin.PLUGIN_ID);
        return projectNode;
    }

    private void persistSuppressedMacros() {
        StringBuilder suppressedMacroProperty = new StringBuilder();
        for (String macro : suppressedMacros) {
            suppressedMacroProperty.append(macro + ";");
        }
        projectPreferences.put(MacronatorPlugin.SUPPRESSED_MACROS_PREF_KEY, suppressedMacroProperty.toString());
        try {
            projectPreferences.flush();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }
}
