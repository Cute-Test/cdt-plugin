package ch.hsr.ifs.cute.ui.wizards.newproject.newsuiteproject;

import org.eclipse.osgi.util.NLS;


public class NewSuiteProjectMessages extends NLS {

    private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.ui.wizards.newproject.newsuiteproject.messages"; //$NON-NLS-1$

    public static String Name;
    public static String EnterSuiteName;
    public static String MustNotBeTest;
    public static String InvalidSuiteName;
    public static String SetSuiteName;
    public static String TestSuiteName;
    public static String CustomSuiteName;
    public static String NewTestSuiteName;
    public static String SuiteName;

    static {
        NLS.initializeMessages(BUNDLE_NAME, NewSuiteProjectMessages.class);
    }

    private NewSuiteProjectMessages() {}

}
