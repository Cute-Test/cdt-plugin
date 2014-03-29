package ch.hsr.ifs.mockator.plugin.project.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;

public class MockatorPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {
  private Button cpp03Button;
  private Button cpp11Button;
  private Button onlyTestFunctionsButton;
  private Button allFunctionsButton;
  private Button orderDependentButton;
  private Button orderIndependentButton;
  private Button linkedEditFunctionsButton;
  private Button linkedEditArgumentsButton;
  private Button onlyMarkReferencedMemFunsButton;
  private Button markAllMemFunsButton;

  @Override
  public boolean performOk() {
    saveProjectOptions();
    toggleCppStdSupport();
    return super.performOk();
  }

  private void saveProjectOptions() {
    CppStandard.storeInProjectSettings(getProject(), getSelectedCppStd());
    FunctionsToAnalyze.storeInProjectSettings(getProject(), getSelectedFunctionsToAnalyze());
    AssertionOrder.storeInProjectSettings(getProject(), getSelectedAssertionOrder());
    LinkedEditModeStrategy.storeInProjectSettings(getProject(), getSelectedLinkedEditStrategy());
    MarkMissingMemFuns.storeInProjectSettings(getProject(), getSelectedMarkMemFuns());
  }

  private void toggleCppStdSupport() {
    getSelectedCppStd().toggleCppStdSupport(getProject());
  }

  @Override
  protected Control createContents(Composite parent) {
    Composite comp = createContainer(parent);
    createCppStdSection(comp);
    createFunctionSection(comp);
    createAssertOrderSection(comp);
    createLinkedEditSection(comp);
    createMarkMemFunsSection(comp);
    return comp;
  }

  private static Composite createContainer(Composite parent) {
    Composite comp = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    comp.setLayout(layout);
    return comp;
  }

  private void createFunctionSection(Composite comp) {
    Group functionGroup = createNamedGroup(comp, I18N.FunctionAnalyzeStrategyDesc);
    createFunctionButtons(functionGroup);
  }

  private void createFunctionButtons(Composite comp) {
    onlyTestFunctionsButton =
        createRadioButton(comp, FunctionsToAnalyze.OnlyTestFunctions.getDescription());
    allFunctionsButton = createRadioButton(comp, FunctionsToAnalyze.AllFunctions.getDescription());
    initFunctionStrategy();
  }

  private void createMarkMemFunsSection(Composite comp) {
    Group markMemFunsGroup = createNamedGroup(comp, I18N.MarkMemFunsDesc);
    createMarkMemFunsButtons(markMemFunsGroup);
  }

  private void createMarkMemFunsButtons(Group comp) {
    onlyMarkReferencedMemFunsButton =
        createRadioButton(comp, MarkMissingMemFuns.OnlyReferencedFromTest.getDescription());
    markAllMemFunsButton = createRadioButton(comp, MarkMissingMemFuns.AllMemFuns.getDescription());
    initMarkMemFuns();
  }

  private void createCppStdSection(Composite comp) {
    Group cppStdGroup = createNamedGroup(comp, I18N.CppStandardDesc);
    createCppStdButtons(cppStdGroup);
  }

  private void createCppStdButtons(Group comp) {
    cpp03Button = createRadioButton(comp, CppStandard.Cpp03Std.getDescription());
    cpp11Button = createRadioButton(comp, CppStandard.Cpp11Std.getDescription());
    initCppStd();
  }

  private void createAssertOrderSection(Composite comp) {
    Group assertGroup = createNamedGroup(comp, I18N.AssertStrategyDesc);
    createAssertButtons(assertGroup);
  }

  private void createLinkedEditSection(Composite comp) {
    Group linkedEditGroup = createNamedGroup(comp, I18N.LinkedEditStrategyDesc);
    createLinkedEditButtons(linkedEditGroup);
  }

  private void createAssertButtons(Composite comp) {
    orderDependentButton = createRadioButton(comp, AssertionOrder.OrderDependent.getDescription());
    orderIndependentButton =
        createRadioButton(comp, AssertionOrder.OrderIndependent.getDescription());
    initAssert();
  }

  private void createLinkedEditButtons(Composite comp) {
    linkedEditArgumentsButton =
        createRadioButton(comp, LinkedEditModeStrategy.ChooseArguments.getDescription());
    linkedEditFunctionsButton =
        createRadioButton(comp, LinkedEditModeStrategy.ChooseFunctions.getDescription());
    initLinkedEdit();
  }

  private FunctionsToAnalyze getSelectedFunctionsToAnalyze() {
    return allFunctionsButton.getSelection() ? FunctionsToAnalyze.AllFunctions
        : FunctionsToAnalyze.OnlyTestFunctions;
  }

  private CppStandard getSelectedCppStd() {
    return cpp03Button.getSelection() ? CppStandard.Cpp03Std : CppStandard.Cpp11Std;
  }

  private AssertionOrder getSelectedAssertionOrder() {
    return orderDependentButton.getSelection() ? AssertionOrder.OrderDependent
        : AssertionOrder.OrderIndependent;
  }

  private LinkedEditModeStrategy getSelectedLinkedEditStrategy() {
    return linkedEditArgumentsButton.getSelection() ? LinkedEditModeStrategy.ChooseArguments
        : LinkedEditModeStrategy.ChooseFunctions;
  }

  private MarkMissingMemFuns getSelectedMarkMemFuns() {
    return markAllMemFunsButton.getSelection() ? MarkMissingMemFuns.AllMemFuns
        : MarkMissingMemFuns.OnlyReferencedFromTest;
  }

  private IProject getProject() {
    return (IProject) getResource();
  }

  private CppStandard getCurrentCppStd() {
    return CppStandard.fromCompilerFlags(getProject());
  }

  private FunctionsToAnalyze getSavedFunctionsToAnalyze() {
    return FunctionsToAnalyze.fromProjectSettings(getProject());
  }

  private AssertionOrder getSavedAssertionOrder() {
    return AssertionOrder.fromProjectSettings(getProject());
  }

  private LinkedEditModeStrategy getSavedLinkedEditStrategy() {
    return LinkedEditModeStrategy.fromProjectSettings(getProject());
  }

  private MarkMissingMemFuns getSavedMarkMemFuns() {
    return MarkMissingMemFuns.fromProjectSettings(getProject());
  }

  private IResource getResource() {
    return (IResource) getElement().getAdapter(IResource.class);
  }

  private static Group createNamedGroup(Composite parent, String label) {
    Group group = new Group(parent, SWT.NONE);
    group.setText(label);
    GridLayout layout = new GridLayout();
    GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    gd.horizontalSpan = 2;
    layout.numColumns = 2;
    group.setLayout(layout);
    group.setLayoutData(gd);
    return group;
  }

  private static Button createRadioButton(Composite comp, String text) {
    Button button = new Button(comp, SWT.RADIO);
    button.setText(text);
    return button;
  }

  private void initCppStd() {
    if (getCurrentCppStd() == CppStandard.Cpp03Std) {
      cpp03Button.setSelection(true);
    } else {
      cpp11Button.setSelection(true);
    }
  }

  private void initFunctionStrategy() {
    if (getSavedFunctionsToAnalyze() == FunctionsToAnalyze.AllFunctions) {
      allFunctionsButton.setSelection(true);
    } else {
      onlyTestFunctionsButton.setSelection(true);
    }
  }

  private void initAssert() {
    if (getSavedAssertionOrder() == AssertionOrder.OrderDependent) {
      orderDependentButton.setSelection(true);
    } else {
      orderIndependentButton.setSelection(true);
    }
  }

  private void initLinkedEdit() {
    if (getSavedLinkedEditStrategy() == LinkedEditModeStrategy.ChooseFunctions) {
      linkedEditFunctionsButton.setSelection(true);
    } else {
      linkedEditArgumentsButton.setSelection(true);
    }
  }

  private void initMarkMemFuns() {
    if (getSavedMarkMemFuns() == MarkMissingMemFuns.AllMemFuns) {
      markAllMemFunsButton.setSelection(true);
    } else {
      onlyMarkReferencedMemFunsButton.setSelection(true);
    }
  }
}
