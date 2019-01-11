package ch.hsr.ifs.cute.core;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;


public class EmptyDebugLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

    public static final String TESTING_PROCESS_FACTORY_ID = "ch.hsr.ifs.cute.core.DebugProcessFactory";

    /**
     * Represents an empty tab group. Actual tabs are added via the <code>org.eclipse.debug.ui.launchConfigurationTabs</code> extension point.
     */
    @Override
    public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
        setTabs(new ILaunchConfigurationTab[0]);
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        super.performApply(configuration);
        configuration.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, TESTING_PROCESS_FACTORY_ID); // set our own process factory....
    }

}
