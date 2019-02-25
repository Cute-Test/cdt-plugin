package ch.hsr.ifs.cute.ui.wizards.newproject;

import org.eclipse.osgi.util.NLS;


public class NewProjectWizardMessages extends NLS {

    private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.ui.wizards.newproject.messages";

    public static String AddLibraryDependency;
    public static String CuteVersion;
    public static String SelectLibraryToTest;
    public static String SetCuteOptions;

    static {
        NLS.initializeMessages(BUNDLE_NAME, NewProjectWizardMessages.class);
    }

    private NewProjectWizardMessages() {

    }
}
