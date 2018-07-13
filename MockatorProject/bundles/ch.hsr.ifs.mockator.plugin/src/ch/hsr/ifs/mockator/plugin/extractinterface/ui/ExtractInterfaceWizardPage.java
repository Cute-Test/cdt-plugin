package ch.hsr.ifs.mockator.plugin.extractinterface.ui;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.checkedCast;
import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.list;
import static ch.hsr.ifs.iltis.core.core.functional.Functional.as;

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
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.extractinterface.ExtractInterfaceRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionSignatureFormatter;


@SuppressWarnings("restriction")
class ExtractInterfaceWizardPage extends UserInputWizardPage {

   private Button              selectAllButton;
   private Button              deselectAllButton;
   private Button              replaceAllCheckbox;
   private CheckboxTableViewer memFunsTableViewer;
   private LabeledTextField    interfaceNameField;

   public ExtractInterfaceWizardPage(final String pageName) {
      super(pageName);
   }

   @Override
   public void createControl(final Composite parent) {
      setMessage(I18N.ExtractInterfacePageTitle);
      setControl(createPageComposite(parent));
   }

   private Composite createPageComposite(final Composite parent) {
      final Composite content = createMainComposite(parent);
      createTopArea(content);
      createSeparator(content);
      createMainArea(content);
      performInitializations();
      return content;
   }

   private void createTopArea(final Composite parent) {
      createInterfaceNameField(parent);
      createReplaceAllCheckbox(parent);
   }

   private void createMainArea(final Composite parent) {
      createChooseMemFunsLabel(parent);
      createMemFunTableArea(parent);
   }

   private void performInitializations() {
      toogleSelectionButtons();
      checkInterfaceName();
      selectUsedMemFuns();
      updateChosenMemFuns();
   }

   private void createInterfaceNameField(final Composite parent) {
      final GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
      gridData.horizontalAlignment = GridData.FILL;
      final Composite interfaceNameContent = new Composite(parent, SWT.NONE);
      interfaceNameContent.setLayoutData(gridData);
      final FillLayout compositeLayout = new FillLayout(SWT.HORIZONTAL);
      interfaceNameContent.setLayout(compositeLayout);
      interfaceNameField = new LabeledTextField(interfaceNameContent, I18N.ExtractInterfaceName, getNewInterfaceClassProposal());
      interfaceNameField.getText().addModifyListener(e -> checkInterfaceName());
   }

   private Composite createMainComposite(final Composite parent) {
      initializeDialogUnits(parent);
      final Composite result = new Composite(parent, SWT.NONE);
      final GridLayout layout = new GridLayout();
      layout.numColumns = 2;
      result.setLayout(layout);
      Dialog.applyDialogFont(result);
      return result;
   }

   private String getNewInterfaceClassProposal() {
      final String newNameProposal = getMyRefactoring().getContext().getNewInterfaceNameProposal();
      return newNameProposal == null ? "" : newNameProposal;
   }

   private void checkInterfaceName() {
      final String interfaceName = interfaceNameField.getFieldContent();
      final IdentifierResult checkResult = IdentifierHelper.checkIdentifierName(interfaceName);

      if (checkResult.isCorrect()) {
         setNewInterfaceName(interfaceName);
      } else {
         refuseNewInterfaceName(checkResult);
      }
   }

   private void refuseNewInterfaceName(final IdentifierResult result) {
      setErrorMessage(NLS.bind(I18N.ExtractInterfaceNameInvalid, result.getMessage()));
      setPageComplete(false);
   }

   private void setNewInterfaceName(final String interfaceName) {
      setErrorMessage(null);
      setPageComplete(true);
      getMyRefactoring().getContext().setNewInterfaceName(interfaceName);
   }

   private void createChooseMemFunsLabel(final Composite parent) {
      final Label chooseMemFunsLabel = new Label(parent, SWT.NONE);
      chooseMemFunsLabel.setText(I18N.ExtractInterfaceChooseMemFuns);
      chooseMemFunsLabel.setEnabled(areMemFunsAvailableForNewInterface());
      final GridData gd = new GridData();
      gd.horizontalSpan = 2;
      chooseMemFunsLabel.setLayoutData(gd);
   }

   private void createReplaceAllCheckbox(final Composite parent) {
      replaceAllCheckbox = createCheckbox(parent, I18N.ExtractInterfaceUseTypeWherePossible, true);
      replaceAllCheckbox.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(final SelectionEvent e) {
            getMyRefactoring().getContext().setShouldReplaceAllOccurences(replaceAllCheckbox.getSelection());
         }
      });
   }

   private static void createSeparator(final Composite parent) {
      final Label separator = new Label(parent, SWT.NONE);
      final GridData gd = new GridData();
      gd.horizontalSpan = 2;
      separator.setLayoutData(gd);
   }

   private void createMemFunTableArea(final Composite parent) {
      final Composite tableContent = createTableContent(parent);
      createMemFunTable(tableContent);
      createSelectButtons(tableContent);
   }

   private Composite createTableContent(final Composite parent) {
      final GridLayout layout = new GridLayout();
      layout.numColumns = 2;
      layout.marginWidth = 0;
      layout.marginHeight = 0;
      final Composite tableContent = new Composite(parent, SWT.NONE);
      tableContent.setLayout(layout);
      final GridData gd = new GridData(GridData.FILL_BOTH);
      gd.heightHint = convertHeightInCharsToPixels(12);
      gd.horizontalSpan = 2;
      tableContent.setLayoutData(gd);
      return tableContent;
   }

   private void createMemFunTable(final Composite parent) {
      memFunsTableViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
      memFunsTableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
      memFunsTableViewer.setLabelProvider(createMemFunLabelProvider());
      memFunsTableViewer.setContentProvider(new ArrayContentProvider());
      memFunsTableViewer.setInput(getAvailableMemFuns());
      memFunsTableViewer.addCheckStateListener(event -> {
         updateChosenMemFuns();
         toogleSelectionButtons();
      });
      memFunsTableViewer.getControl().setEnabled(areMemFunsAvailableForNewInterface());
   }

   private void selectUsedMemFuns() {
      final Collection<IASTDeclaration> usedMemFuns = getMyRefactoring().getContext().getUsedPublicMemFuns();

      for (final IASTDeclaration memFun : getMemFunsInTable()) {
         memFunsTableViewer.setChecked(memFun, usedMemFuns.contains(memFun));
      }
   }

   private Collection<IASTDeclaration> getMemFunsInTable() {
      return as(memFunsTableViewer.getInput());
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
      final Collection<IASTDeclaration> checkedMemFuns = getCheckedMemFuns();
      final Collection<IASTDeclaration> availableMemFuns = getAvailableMemFuns();
      selectAllButton.setEnabled(availableMemFuns != null && checkedMemFuns.size() < availableMemFuns.size());
      deselectAllButton.setEnabled(!checkedMemFuns.isEmpty());
   }

   private static ILabelProvider createMemFunLabelProvider() {
      return new LabelProvider() {

         @Override
         public Image getImage(final Object element) {
            return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_PUBLIC_METHOD);
         }

         @Override
         public String getText(final Object element) {
            final ICPPASTFunctionDeclarator funDecl = CPPVisitor.findChildWithType((IASTNode) element, ICPPASTFunctionDeclarator.class).orElse(null);
            return getFunSignatureFor(funDecl);
         }
      };
   }

   private static String getFunSignatureFor(final ICPPASTFunctionDeclarator funDecl) {
      return new FunctionSignatureFormatter(funDecl).getFunctionSignature();
   }

   private void createSelectButtons(final Composite parent) {
      final Composite buttonComposite = getButtonCompositeFor(parent);
      selectAllButton = createSelectionButton(buttonComposite, I18N.ExtractInterfaceSelectAll, true);
      deselectAllButton = createSelectionButton(buttonComposite, I18N.ExtractInterfaceDeselectAll, false);
   }

   private Button createSelectionButton(final Composite parent, final String text, final boolean isChecked) {
      final Button button = new Button(parent, SWT.PUSH);
      button.setText(text);
      button.setEnabled(areMemFunsAvailableForNewInterface());
      button.setLayoutData(new GridData());
      SWTUtil.setButtonDimensionHint(button);
      button.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(final SelectionEvent e) {
            memFunsTableViewer.setAllChecked(isChecked);
            toogleSelectionButtons();
         }
      });
      return button;
   }

   private static Composite getButtonCompositeFor(final Composite parent) {
      final GridLayout gl = new GridLayout();
      gl.marginHeight = 0;
      gl.marginWidth = 0;
      final Composite buttonComposite = new Composite(parent, SWT.NONE);
      buttonComposite.setLayout(gl);
      final GridData gd = new GridData(GridData.FILL_VERTICAL);
      buttonComposite.setLayoutData(gd);
      return buttonComposite;
   }

   private boolean areMemFunsAvailableForNewInterface() {
      final Collection<IASTDeclaration> extractableMemFuns = getAvailableMemFuns();
      return extractableMemFuns != null && !extractableMemFuns.isEmpty();
   }

   private static Button createCheckbox(final Composite parent, final String title, final boolean value) {
      final Button checkBox = new Button(parent, SWT.CHECK);
      checkBox.setText(title);
      checkBox.setSelection(value);
      final GridData layoutData = new GridData();
      layoutData.horizontalSpan = 2;
      checkBox.setLayoutData(layoutData);
      return checkBox;
   }

   private ExtractInterfaceRefactoring getMyRefactoring() {
      return (ExtractInterfaceRefactoring) getRefactoring();
   }
}
