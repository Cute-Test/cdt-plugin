package ch.hsr.ifs.cute.ui.wizards.newsuitefile;

import org.eclipse.osgi.util.NLS;


public class NewSuiteFileMessages extends NLS {

    private static final String BUNDLE_NAME = "ch.hsr.ifs.cute.ui.wizards.newsuitefile.messages"; //$NON-NLS-1$

    public static String Title;
    public static String NoTestRunnersFound;
    public static String Description;
    public static String FileAlreadyExists;
    public static String FolderAlreadyExists;
    public static String ResourceAlreadyExists;
    public static String LinkToRunner;
    public static String ChooseExistingRunner;
    public static String SourceFolder;
    public static String Browse;
    public static String SuiteName;
    public static String EmptySourceFolderName;
    public static String NotAProjectFolder;
    public static String NotACXXProject;
    public static String NotInACXXProject;
    public static String NotASourceFolder;
    public static String NotAProjectOrFolder;
    public static String DoesNotExist;
    public static String InvalidFileName;
    public static String EnterSuiteName;
    public static String FileMustBeInsideSourceFolder;
    public static String FindMain;
    public static String FindRunners;

    static {
        NLS.initializeMessages(BUNDLE_NAME, NewSuiteFileMessages.class);
    }

    private NewSuiteFileMessages() {}

}
