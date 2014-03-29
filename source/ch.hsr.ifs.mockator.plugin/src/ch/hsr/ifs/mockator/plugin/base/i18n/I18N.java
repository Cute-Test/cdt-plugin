package ch.hsr.ifs.mockator.plugin.base.i18n;

import org.eclipse.osgi.util.NLS;

public abstract class I18N extends NLS {
  private static final String BUNDLE_NAME = "OSGI-INF.l10n.bundle";

  // Plug-in
  public static String BundleName;
  public static String BundleProvider;
  public static String AddNatureMenu;
  public static String RemoveNatureMenu;
  public static String PluginVisibleName;
  public static String TestSourceActionsActionSetLabel;
  public static String TestSourceGeneratorsMenuLabel;

  // Error messages
  public static String ExceptionCaughtMessage;
  public static String ExceptionCaughtTitle;
  public static String NatureRemovalFailedTitle;
  public static String NatureRemovalFailedMsg;
  public static String NatureAdditionFailedTitle;
  public static String NatureAdditionFailedMsg;
  public static String WrapFunctionNoValidFunction;
  public static String ExceptionErrorTitle;

  // Checkers and Quick fixes
  public static String MockatorProblemCategoryDesc;
  public static String CreateObjectSeamQuickfix;
  public static String CreateCompileSeamQuickfix;
  public static String CreateMissingMemberFunctionQuickfix;
  public static String RecordMemFunsByChoosingFunArgsQuickfix;
  public static String RecordMemFunsByChoosingFunSignaturesQuickfix;
  public static String MemberFunctionsToImplementTitle;

  // Wizards
  public static String AddMockatorSupportToCUTEProject;
  public static String NewSuiteWizardNewCuiteSuite;
  public static String NewSuiteWizardCreateNewSuite;
  public static String NewSuiteWizardSourceFolder;
  public static String NewSuiteWizardBrowse;
  public static String NewSuiteWizardSuiteName;
  public static String NewSuiteWizardLinkToRunner;
  public static String NewSuiteWizardChooseRunMethod;
  public static String NewSuiteWizardNoRunners;
  public static String NewSuiteWizardEnterSuiteName;
  public static String NewSuiteWizardFileMustBeInsideSourceFolder;
  public static String NewSuiteWizardFolder;
  public static String NewSuiteWizardNotExisting;
  public static String NewSuiteWizardFileNameInvalid;
  public static String NewSuiteWizardInvalidIdentifier;
  public static String NewSuiteWizardFileAlreadyExist;
  public static String NewSuiteWizardFolderAlreadyExists;
  public static String NewSuiteWizardResourceAlreadyExists;
  public static String NewSuiteWizardBrowseFolderNameEmpty;
  public static String NewSuiteWizardIsNotProjectOrFolder;
  public static String NewSuiteWizardNotaCppProject;
  public static String NewSuiteWizardIsNotInCppProject;
  public static String NewSuiteWizardIsNotSourceFolder;
  public static String NewSuiteWizardNewCuiteSuiteFile;
  public static String NewSuiteWizardFolderSelection;
  public static String NewSuiteWizardSourceFolderSelection;

  // Property page
  public static String CppStandardDesc;
  public static String FunctionAnalyzeStrategyDesc;
  public static String AllFunctionsDesc;
  public static String TestFunctionsDesc;
  public static String Cpp03Desc;
  public static String Cpp11Desc;
  public static String OrderDependentDesc;
  public static String OrderIndependentDesc;
  public static String AssertStrategyDesc;
  public static String LinkedEditStrategyDesc;
  public static String ChooseFunctionsDesc;
  public static String ChooseArgumentsDesc;
  public static String MarkMemFunsDesc;
  public static String AllMemFuns;
  public static String OnlyReferencedFromTest;

  // Extract Interface Refactoring
  public static String ExtractInterfacePageTitle;
  public static String ExtractInterfaceChooseMemFuns;
  public static String ExtractInterfaceName;
  public static String ExtractInterfaceUseTypeWherePossible;
  public static String ExtractInterfaceSelectAll;
  public static String ExtractInterfaceDeselectAll;
  public static String ExtractInterfaceDialogTitle;
  public static String ExtractInterfaceAnalyzingInProgress;
  public static String ExtractInterfacePerformingChangesInProgress;
  public static String ExtractInterfaceNameInvalid;
  public static String ExtractInterfaceDeleteObsoleteIncludesChangeDesc;
  public static String ExtractInterfaceReplaceOccurrencesChangeDesc;
  public static String ExtractInterfaceActionSet;
  public static String ExtractInterfaceMenu;
  public static String ExtractInterfaceActionLabel;
  public static String ExtractInterfaceCommandName;
  public static String ExtractInterfaceShadowedFunction;

  // Preprocessor
  public static String PreprocessorCommandDescription;
  public static String PreprocessorName;
  public static String PreprocessorActionLabel;

  // Wrap function
  public static String WrapFunctionNoGnuLinkerFoundTitile;
  public static String WrapFunctionNoGnuLinkerFoundMsg;
  public static String WrapFunctionFunDefinitionInSameTuTitile;
  public static String WrapFunctionFunDefinitionInSameTuMsg;
  public static String WrapFunctionDoNotShowAgainMsg;
  public static String WrapFunctionActivate;
  public static String WrapFunctionDeactivate;
  public static String WrapFunctionDelete;
  public static String WrapFunctionGnuLinuxReqTitle;
  public static String WrapFunctionGnuLinuxReqMsg;
  public static String WrapFunctionShLibsNotSupportedTitle;
  public static String WrapFunctionShLibsNotSupportedMsg;
  public static String WrapFunctionProblemName;
  public static String WrapFunctionProblemMsg;
  public static String WrapFunctionCommandDescription;
  public static String WrapFunctionName;
  public static String WrapFunctionActionLabel;
  public static String WrapFunctionCheckerName;
  public static String MaintainRunTimeWrappingMenu;

  // Inconsistent expectations
  public static String InconsistentExpectationsQuickfix;
  public static String InconsistentExpectationsProblemMsg;
  public static String InconsistentExpectationsProblemName;

  // Shadow function
  public static String ShadowFunLibProjectNecessaryTitle;
  public static String ShadowFunLibProjectNecessaryMsg;
  public static String ShadowFunLibProjectNoRefExecTitle;
  public static String ShadowFunLibProjectNoRefExecMsg;
  public static String ShadowFunctionCommandDescription;
  public static String ShadowFunctionName;
  public static String ShadowFunctionActionLabel;

  // Run-time wrap
  public static String RuntimeWrapCreateInfrastructure;
  public static String RuntimeWrapShLibProjectNecessaryTitle;
  public static String RuntimeWrapShLibProjectNecessaryMsg;
  public static String RuntimeWrapLibProjectNoRefExecTitle;
  public static String RuntimeWrapLibProjectNoRefExecMsg;
  public static String RuntimeWrapCreateInfrastructureFailed;
  public static String RuntimeWrapLibWindowsNotSupportedTitle;
  public static String RuntimeWrapLibWindowsNotSupportedMsg;
  public static String RunnerFinderFindMain;
  public static String RunnerFinderFindRunners;

  // Trace function
  public static String TraceFunctionActivate;
  public static String TraceFunctionDeactivate;
  public static String TraceFunctionProblemName;
  public static String TraceFunctionProblemMsg;
  public static String TraceFunctionCheckerName;

  // Mock function
  public static String MockFunctionPreconditionsNotSatisfied;
  public static String MockFunctionOnlyWorksWithLibs;
  public static String MockFunctionNeedsMockatorProject;
  public static String MockFunctionActionLabel;
  public static String MockFunctionCommandDescription;
  public static String MockFunctionCommandName;

  // Refactoring descriptions
  public static String ConsistentExpectationsRefactoringDesc;
  public static String ConvertToMockObjectRefactoringDesc;
  public static String CreateTestDoubleRefactoringDesc;
  public static String DeleteWrappedFunctionRefactoringDesc;
  public static String FakeObjectRefactoringDesc;
  public static String MockObjectRefactoringDesc;
  public static String ToggleTracingFunctionRefactoringDesc;
  public static String MoveTestDoubleToNsRefactoringDesc;
  public static String RemoveInitMockatorRefactoringDesc;
  public static String GnuOptionRefactoringDesc;
  public static String LdPreloadRefactoringDesc;
  public static String LinkSuiteToRunnerRefactoringDesc;
  public static String MockFunctionRefactoringDesc;
  public static String PreprocessorRefactoringDesc;
  public static String ShadwoFunctionRefactoringDesc;

  // Move Test Double
  public static String MoveTestDoubleToNamespaceActionLabel;
  public static String MoveTestDoubleToNamespaceCommandDescription;
  public static String MoveTestDoubleToNamespaceCommandName;

  // Toggle Tracing
  public static String ToggleTracingFunCallsActionLabel;
  public static String ToggleTracingFunCallsName;
  public static String ToggleTracingFunCallsCommandDescription;

  // Convert to Mock Object
  public static String ConvertToMockObjectCommandDescription;
  public static String ConvertToMockObjectCommandName;
  public static String ConvertToMockObjectActionLabel;

  // Missing Mem Fun
  public static String MissingMemberFunctionsInCompileSeamProblemName;
  public static String MissingMemberFunctionsInObjectSeamProblemName;
  public static String NeccesaryMemberFunctionsMissingMsg;
  public static String SubTypeTestDoubleMissingMsg;
  public static String SubTypeMissingTestDoubleCheckerName;
  public static String SubTypeMissingTestDoubleProblemName;
  public static String StaticPolyTestDoubleMissingMsg;
  public static String StaticPolyMissingTestDoubleCheckerName;
  public static String StaticPolyMissingTestDoubleProblemName;
  public static String StaticPolyMissingMemberFunctionsCheckerName;
  public static String SubtypePolyMissingMemberFunctionsCheckerName;

  static {
    NLS.initializeMessages(BUNDLE_NAME, I18N.class);
  }
}
