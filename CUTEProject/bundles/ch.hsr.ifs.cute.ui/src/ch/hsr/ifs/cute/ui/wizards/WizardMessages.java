package ch.hsr.ifs.cute.ui.wizards;

import org.eclipse.osgi.util.NLS;


public class WizardMessages extends NLS {

    private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.ui.wizards.messages";

    public static String VersionLabel;
    public static String NotInstalled;
    public static String FolderSelection;
    public static String ChooseASourceFolder;

    static {
        NLS.initializeMessages(BUNDLE_NAME, WizardMessages.class);
    }

    private WizardMessages() {

    }
}
