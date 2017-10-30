package ch.hsr.ifs.mockator.plugin.extractinterface.ui;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.checkedCast;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.ui.refactoring.dialogs.LabeledTextField;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.IdentifierResult;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.misc.CastHelper;
import ch.hsr.ifs.mockator.plugin.extractinterface.ExtractInterfaceRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionSignatureFormatter;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;


@SuppressWarnings("restriction")
class ExtractInterfaceWizardPage extends UserInputWizardPage {

   private Button              selectAllButton;
   private Button              deselectAllButton;
   private Button              replaceAllCheckbox;
   private CheckboxTableViewer memFunsTableViewer;
   private LabeledTextField    interfaceNameField;

   public ExtractInterfaceWizardPage(String pageName) {
      super(pageName);
   }

   @Override
   public void createControl(Composite parent) {
      setMessage(I18N.ExtractInterfacePageTitle);
      setControl(createPageComposite(parent));
   }

   private Composite createPageComposite(Composite parent) {
      Composite content = createMainComposite(parent);
      createTopArea(content);
      createSeparator(content);
      createMainArea(content);
      performInitializations();
      return content;
   }

   private void createTopArea(Composite parent) {
      createInterfaceNameField(parent);
      createReplaceAllCheckbox(parent);
   }

   private void createMainArea(Composite parent) {
      createChooseMemFunsLabel(parent);
      createMemFunTableArea(parent);
   }

   private void performInitializations() {
      toogleSelectionButtons();
      checkInterfaceName();
      selectUsedMemFuns();
      updateChosenMemFuns();
   }

   private void createInterfaceNameField(Composite parent) {
      GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
      gridData.horizontalAlignment = GridData.FILL;
      Composite interfaceNameContent = new Composite(parent, SWT.NONE);
      interfaceNameContent.setLayoutData(gridData);
      FillLayout compositeLayout = new FillLayout(SWT.HORIZONTAL);
      interfaceNameContent.setLayout(compositeLayout);
      interfaceNameField = new LabeledTextField(interfaceNameContent, I18N.ExtractInterfaceName, getNewInterfaceClassProposal());
      interfaceNameField.getText().addModifyListener(new ModifyListener() {

         @Override
         public void modifyText(ModifyEvent e) {
            checkInterfaceName();
         }
      });
   }

   private Composite createMainComposite(Composite parent) {
      initializeDialogUnits(parent);
      Composite result = new Composite(parent, SWT.NONE);
      GridLayout layout = new GridLayout();
      layout.numColumns = 2;
      result.setLayout(layout);
      Dialog.applyDialogFont(result);
      return result;
   }

   private String getNewInterfaceClassProposal() {
      String newNameProposal = getMyRefactoring().getContext().getNewInterfaceNameProposal();
      return newNameProposal == null ? "" : newNameProposal;
   }

   private void checkInterfaceName() {
      String interfaceName = interfaceNameField.getFieldContent();
      IdentifierResult checkResult = IdentifierHelper.checkIdentifierName(interfaceName);

      if (checkResult.isCorrect()) {
         setNewInterfaceName(interfaceName);
      } else {
         refuseNewInterfaceName(checkResult);
      }
   }

   private void refuseNewInterfaceName(IdentifierResult result) {
      setErrorMessage(NLS.bind(I18N.ExtractInterfaceNameInvalid, result.getMessage()));
      setPageComplete(false);
   }

   private void setNewInterfaceName(String interfaceName) {
      setErrorMessage(null);
      setPageComplete(true);
      getMyRefactoring().getContext().setNewInterfaceName(interfaceName);
   }

   private void createChooseMemFunsLabel(Composite parent) {
      Label chooseMemFunsLabel = new Label(parent, SWT.NONE);
      chooseMemFunsLabel.setText(I18N.ExtractInterfaceChooseMemFuns);
      chooseMemFunsLabel.setEnabled(areMemFunsAvailableForNewInterface());
      GridData gd = new GridData();
      gd.horizontalSpan = 2;
      chooseMemFunsLabel.setLayoutData(gd);
   }

   private void createReplaceAllCheckbox(Composite parent) {
      replaceAllCheckbox = createCheckbox(parent, I18N.ExtractInterfaceUseTypeWherePossible, true);
      replaceAllCheckbox.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            getMyRefactoring().getContext().setShouldReplaceAllOccurences(replaceAllCheckbox.getSelection());
         }
      });
   }

   private static void createSeparator(Composite parent) {
      Label separator = new Label(parent, SWT.NONE);
      GridData gd = new GridData();
      gd.horizontalSpan = 2;
      separator.setLayoutData(gd);
   }

   private void createMemFunTableArea(Composite parent) {
      Composite tableContent = createTableContent(parent);
      createMemFunTable(tableContent);
      createSelectButtons(tableContent);
   }

   private Composite createTableContent(Composite parent) {
      GridLayout layout = new GridLayout();
      layout.numColumns = 2;
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      Composite tableContent = new Composite(parent, SWT.NONE);
      tableContent.setLayout(layout);
      GridData gd = new GridData(GridData.FILL_BOTH);
      gd.heightHint = convertHeightInCharsToPixels(12);
      gd.horizontalSpan = 2;
      tableContent.setLayoutData(gd);
      return tableContent;
   }

   private void createMemFunTable(Composite parent) {
      memFunsTableViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
      memFunsTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
      memFunsTableViewer.setLabelProvider(createMemFunLabelProvider());
      memFunsTableViewer.setContentProvider(new ArrayContentProvider());
      memFunsTableViewer.setInput(getAvailableMemFuns());
      memFunsTableViewer.addCheckStateListener(new ICheckStateListener() {

         @Override
         public void checkStateChanged(CheckStateChangedEvent event) {
            updateChosenMemFuns();
            toogleSelectionButtons();
         }
      });
      memFunsTableViewer.getControl().setEnabled(areMemFunsAvailableForNewInterface());
   }

   private void selectUsedMemFuns() {
      Collection<IASTDeclaration> usedMemFuns = getMyRefactoring().getContext().getUsedPublicMemFuns();

      for (IASTDeclaration memFun : getMemFunsInTable()) {
         memFunsTableViewer.setChecked(memFun, usedMemFuns.contains(memFun));
      }
   }

   private Collection<IASTDeclaration> getMemFunsInTable() {
      return CastHelper.unsecureCast(memFunsTableViewer.getInput());
   }

   private void updateChosenMemFuns() {
      getMyRefactoring().getContext().setChosenMemFuns(getCheckedMemFuns());
   }

   private Collection<IASTDeclaration> getAvailableMemFuns() {
      return getMyRefactoring().getContext().getAvailablePupMemFuns();
   }

   private Collection<IASTDeclaration> getCheckedMemFuns() {
      return checkedCast(list(memFunsTableViewer.getCheckedElements()), IASTDeclaration.class);
   }

   private void toogleSelectionButtons() {
      Collection<IASTDeclaration> checkedMemFuns = getCheckedMemFuns();
      Collection<IASTDeclaration> availableMemFuns = getAvailableMemFuns();
      selectAllButton.setEnabled(availableMemFuns != null && checkedMemFuns.size() < availableMemFuns.size());
      deselectAllButton.setEnabled(!checkedMemFuns.isEmpty());
   }

   private static ILabelProvider createMemFunLabelProvider() {
      return new LabelProvider() {

         @Override
         public Image getImage(Object element) {
            return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_PUBLIC_METHOD);
         }

         @Override
         public String getText(Object element) {
            ICPPASTFunctionDeclarator funDecl = AstUtil.getChildOfType((IASTNode) element, ICPPASTFunctionDeclarator.class);
            return getFunSignatureFor(funDecl);
         }
      };
   }

   private static String getFunSignatureFor(ICPPASTFunctionDeclarator funDecl) {
      return new FunctionSignatureFormatter(funDecl).getFunctionSignature();
   }

   private void createSelectButtons(Composite parent) {
      Composite buttonComposite = getButtonCompositeFor(parent);
      selectAllButton = createSelectionButton(buttonComposite, I18N.ExtractInterfaceSelectAll, true);
      deselectAllButton = createSelectionButton(buttonComposite, I18N.ExtractInterfaceDeselectAll, false);
   }

   private Button createSelectionButton(Composite parent, String text, final boolean isChecked) {
      final Button button = new Button(parent, SWT.PUSH);
      button.setText(text);
      button.setEnabled(areMemFunsAvailableForNewInterface());
      button.setLayoutData(new GridData());
      SWTUtil.setButtonDimensionHint(button);
      button.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(SelectionEvent e) {
            memFunsTableViewer.setAllChecked(isChecked);
            toogleSelectionButtons();
         }
      });
      return button;
   }

   private static Composite getButtonCompositeFor(Composite parent) {
      GridLayout gl = new GridLayout();
      gl.marginHeight = 0;
      gl.marginWidth = 0;
      Composite buttonComposite = new Composite(parent, SWT.NONE);
      buttonComposite.setLayout(gl);
      GridData gd = new GridData(GridData.FILL_VERTICAL);
      buttonComposite.setLayoutData(gd);
      return buttonComposite;
   }

   private boolean areMemFunsAvailableForNewInterface() {
      Collection<IASTDeclaration> extractableMemFuns = getAvailableMemFuns();
      return extractableMemFuns != null && !extractableMemFuns.isEmpty();
   }

   private static Button createCheckbox(Composite parent, String title, boolean value) {
      Button checkBox = new Button(parent, SWT.CHECK);
      checkBox.setText(title);
      checkBox.setSelection(value);
      GridData layoutData = new GridData();
      layoutData.horizontalSpan = 2;
      checkBox.setLayoutData(layoutData);
      return checkBox;
   }

   private ExtractInterfaceRefactoring getMyRefactoring() {
      return (ExtractInterfaceRefactoring) getRefactoring();
   }
}
