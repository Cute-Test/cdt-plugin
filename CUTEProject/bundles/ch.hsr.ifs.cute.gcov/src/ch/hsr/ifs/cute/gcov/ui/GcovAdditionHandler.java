/*******************************************************************************
 * Copyright (c) 2007-2015, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.ui;

import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.BuildAction;

import ch.hsr.ifs.cute.gcov.GcovNature;
import ch.hsr.ifs.cute.gcov.GcovPlugin;
import ch.hsr.ifs.cute.ui.ICuteWizardAdditionHandler;
import ch.hsr.ifs.cute.ui.ProjectTools;


/**
 * @author Emanuel Graf IFS
 *
 */
public class GcovAdditionHandler implements ICuteWizardAdditionHandler {

    public static final String GCOV_CONFG_ID = "gcov";

    private static final String MACOSX_LINKER_OPTION_FLAGS          = "macosx.cpp.link.option.flags";
    private static final String GCOV_LINKER_FLAGS                   = "-fprofile-arcs -ftest-coverage -std=c99";
    private static final String GCOV_CPP_ID                         = "gnu.cpp.compiler.option.debugging.codecov";
    private static final String GCOV_C_ID                           = "gnu.c.compiler.option.debugging.codecov";
    private static final String GNU_CPP_LINK_OPTION_FLAGS           = "gnu.cpp.link.option.flags";
    private static final String GNU_CPP_LINKER_ID                   = "cdt.managedbuild.tool.gnu.cpp.linker";
    private static final String MAC_CPP_LINKER_ID                   = "cdt.managedbuild.tool.macosx.cpp.linker";
    private static final String GNU_C_COMPILER_OPTION_MISC_OTHER    = "gnu.c.compiler.option.misc.other";
    private static final String GNU_C_COMPILER_ID                   = "cdt.managedbuild.tool.gnu.c.compiler";
    private static final String GNU_CPP_COMPILER_OPTION_OTHER_OTHER = "gnu.cpp.compiler.option.other.other";
    private static final String GNU_CPP_COMPILER_ID                 = "cdt.managedbuild.tool.gnu.cpp.compiler";
    private static final String GCOV_C_COMPILER_FLAGS               = "-std=c99 ";
    private static final String GCOV_CPP_COMPILER_LIB_FLAGS         = "-lgcov ";
    private static final String GCOV_C_COMPILER_LIB_FLAGS           = "-lgcov ";

    private GcovWizardAddition addition;

    public GcovAdditionHandler() {}

    public GcovAdditionHandler(GcovWizardAddition addition) {
        super();
        this.addition = addition;
    }

    @Override
    public void configureProject(IProject project, IProgressMonitor pm) throws CoreException {
        SubMonitor mon = SubMonitor.convert(pm, 2);
        if (isGcovEnabled()) {
            GcovNature.addGcovNature(project, mon);
            addGcovConfig(project);
        }
        mon.done();
    }

    protected boolean isGcovEnabled() {
        return addition != null ? addition.enableGcov : true;
    }

    public IConfiguration addGcovConfig(IProject project) throws CoreException {
        try {
            IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
            IConfiguration[] configs = info.getManagedProject().getConfigurations();
            for (IConfiguration config : configs) {
                if (config.getParent().getId().contains("debug")) {
                    IConfiguration newConfig = createGcovConfig(project, info, config);
                    ManagedBuildManager.setDefaultConfiguration(project, newConfig);
                    ManagedBuildManager.setSelectedConfiguration(project, newConfig);
                    ManagedBuildManager.saveBuildInfoLegacy(project, true);
                    return newConfig;
                }
            }
        } catch (BuildException e) {
            throw new CoreException(new Status(IStatus.ERROR, GcovPlugin.PLUGIN_ID, e.getMessage(), e));
        }
        return null;
    }

    private IConfiguration createGcovConfig(IProject project, IManagedBuildInfo info, IConfiguration config) throws BuildException {
        IConfiguration newConfig = info.getManagedProject().createConfigurationClone(config, GCOV_CONFG_ID);
        boolean isLibraryProject = ProjectTools.isLibraryProject(project);
        newConfig.setName("Debug Gcov");
        if (isLibraryProject) {
            setOptionInTool(newConfig, GNU_CPP_COMPILER_ID, GNU_CPP_COMPILER_OPTION_OTHER_OTHER, GCOV_CPP_COMPILER_LIB_FLAGS);
            setOptionInTool(newConfig, GNU_C_COMPILER_ID, GNU_C_COMPILER_OPTION_MISC_OTHER, GCOV_C_COMPILER_LIB_FLAGS);
        }
        checkOptionInTool(newConfig, GNU_CPP_COMPILER_ID, GCOV_CPP_ID, true);
        checkOptionInTool(newConfig, GNU_C_COMPILER_ID, GCOV_C_ID, true);
        setOptionInTool(newConfig, GNU_C_COMPILER_ID, GNU_C_COMPILER_OPTION_MISC_OTHER, GCOV_C_COMPILER_FLAGS);
        setOptionInTool(newConfig, GNU_CPP_LINKER_ID, GNU_CPP_LINK_OPTION_FLAGS, GCOV_LINKER_FLAGS);
        setOptionInTool(newConfig, MAC_CPP_LINKER_ID, MACOSX_LINKER_OPTION_FLAGS, GCOV_LINKER_FLAGS);
        return newConfig;
    }

    private void checkOptionInTool(IConfiguration config, String toolId, String optionId, boolean optionValue) throws BuildException {
        ITool[] tools = config.getToolsBySuperClassId(toolId);
        for (ITool tool : tools) {
            IOption option = tool.getOptionBySuperClassId(optionId);
            ManagedBuildManager.setOption(config, tool, option, optionValue);
        }
    }

    private void setOptionInTool(IConfiguration config, String toolId, String optionId, String optionValue) throws BuildException {
        ITool[] tools = config.getToolsBySuperClassId(toolId);
        for (ITool tool : tools) {
            IOption option = tool.getOptionBySuperClassId(optionId);
            String value = option.getValue() == null ? optionValue : option.getStringValue().trim() + " " + optionValue; //$NON-NLS-1$
            ManagedBuildManager.setOption(config, tool, option, value);
        }
    }

    @Override
    public void configureLibProject(IProject libProject) throws CoreException {
        if (isGcovEnabled() && libProjectNeedGcovConfig(libProject)) {
            addGcovConfig(libProject);
            BuildAction buildAction = new BuildAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                    IncrementalProjectBuilder.INCREMENTAL_BUILD);
            buildAction.selectionChanged(new StructuredSelection(libProject));
            buildAction.run();
        }
    }

    private boolean libProjectNeedGcovConfig(IProject libProject) {
        IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(libProject);
        for (String name : info.getConfigurationNames()) {
            if (name.equalsIgnoreCase("debug gcov")) {
                return false;
            }
        }
        return true;
    }

    public void adaptLibraryPath(IProject project, IProject referencedProject) throws CoreException {
        adaptLibraryPathForLangId(project, referencedProject, GNU_CPP_LINKER_ID);
        adaptLibraryPathForLangId(project, referencedProject, MAC_CPP_LINKER_ID);
    }

    private void adaptLibraryPathForLangId(IProject project, IProject referencedProject, String linkerID) throws CoreException {
        ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project);
        ICLanguageSetting lang = getLanguageSettingsForId(linkerID, projectDescription);
        if (lang != null) {
            String gcovLibPath = getGcovLibraryOutputPath(referencedProject);
            CLibraryPathEntry entry = CDataUtil.createCLibraryPathEntry(gcovLibPath, ICSettingEntry.VALUE_WORKSPACE_PATH);
            List<ICLanguageSettingEntry> entries = lang.getSettingEntriesList(ICSettingEntry.LIBRARY_PATH);
            ICLanguageSettingEntry obsoleteDebugEntry = findDebugEntry(referencedProject, entries);
            if (obsoleteDebugEntry != null) {
                entries.remove(obsoleteDebugEntry);
            }
            entries.add(entry);
            lang.setSettingEntries(ICSettingEntry.LIBRARY_PATH, entries);
            CoreModel.getDefault().setProjectDescription(project, projectDescription);
        }
    }

    private ICLanguageSettingEntry findDebugEntry(IProject referencedProject, List<ICLanguageSettingEntry> entries) {
        String debugPath = "/" + referencedProject.getName() + "/Debug";
        for (ICLanguageSettingEntry entry : entries) {
            String value = entry.getValue();
            if (value != null && value.equals(debugPath)) {
                return entry;
            }
        }
        return null;
    }

    private String getGcovLibraryOutputPath(IProject referencedProject) {
        return "/" + referencedProject.getName() + "/" + CoreModel.getDefault().getProjectDescription(referencedProject).getActiveConfiguration()
                .getName();
    }

    private ICLanguageSetting getLanguageSettingsForId(String linkerID, ICProjectDescription projectDescription) {
        ICConfigurationDescription activeConfiguration = projectDescription.getActiveConfiguration();
        ICFolderDescription folderDescription = activeConfiguration.getRootFolderDescription();
        ICLanguageSetting[] languageSettings = folderDescription.getLanguageSettings();

        for (ICLanguageSetting languageSetting : languageSettings) {
            String langId = languageSetting.getId();
            if (langId.contains(linkerID)) {
                return languageSetting;
            }
        }
        return null;
    }
}
