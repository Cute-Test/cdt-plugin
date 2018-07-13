/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil, Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any purpose without fee is hereby granted, provided that the above copyright notice
 * and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.mockator.plugin.mockobject.function.suite.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.core.resources.WorkspaceUtil;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;
import ch.hsr.ifs.mockator.plugin.mockobject.function.suite.refactoring.LinkSuiteToRunnerRefactoring;
import ch.hsr.ifs.mockator.plugin.mockobject.function.suite.refactoring.RunnerFinder;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoringRunner;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.FileEditorOpener;


// Copied and adapted from CUTE
@SuppressWarnings("restriction")
class NewSuiteFileCreationWizardPage extends WizardPage {

   private static final int                   SOURCE_FOLDER_ID = 1;
   private static final int                   NEW_FILE_ID      = 2;
   private static final int                   ALL_FIELDS       = SOURCE_FOLDER_ID | NEW_FILE_ID;
   private static final IStatus               STATUS_OK        = new StatusInfo();
   private final MockFunctionCommunication    mockFunction;
   private final LinkSuiteToRunnerRefactoring runnerRefactoring;
   private final ICProject                    mockatorCProject;
   private StringDialogField                  newFileDialogField;
   private SelectionButtonDialogField         linkToRunnerCheck;
   private StringButtonDialogField            sourceFolderDialogField;
   private IStatus                            fSourceFolderStatus;
   private IStatus                            fNewFileStatus;
   private int                                fLastFocusedField;
   private ComboDialogField                   runnerComboField;
   private List<IASTFunctionDefinition>       runners;

   public NewSuiteFileCreationWizardPage(final ICProject cProject, final MockFunctionCommunication mockFunction,
                                         final LinkSuiteToRunnerRefactoring runnerRefactoring) {
      super(I18N.NewSuiteWizardNewCuiteSuite);
      mockatorCProject = cProject;
      this.mockFunction = mockFunction;
      this.runnerRefactoring = runnerRefactoring;
      setDescription(I18N.NewSuiteWizardCreateNewSuite);
      initSourceFolderField();
      initNewFileDialogField();
      initLinkToRunner();
      initFields(cProject);
      doStatusUpdate();
   }

   private void initLinkToRunner() {
      linkToRunnerCheck = new SelectionButtonDialogField(SWT.CHECK);
      linkToRunnerCheck.setLabelText(I18N.NewSuiteWizardLinkToRunner);
      runnerComboField = new ComboDialogField(SWT.READ_ONLY);
      runnerComboField.setLabelText(I18N.NewSuiteWizardChooseRunMethod);
   }

   private void initNewFileDialogField() {
      newFileDialogField = new StringDialogField();
      newFileDialogField.setDialogFieldListener(new SourceFolderFieldAdapter() {

         @Override
         public void dialogFieldChanged() {
            handleFieldChanged(NEW_FILE_ID);
         }
      });
      newFileDialogField.setLabelText(I18N.NewSuiteWizardSuiteName);
   }

   private void initSourceFolderField() {
      final SourceFolderFieldAdapter sourceFolderAdapter = new SourceFolderFieldAdapter();
      sourceFolderDialogField = new StringButtonDialogField(sourceFolderAdapter);
      sourceFolderDialogField.setDialogFieldListener(sourceFolderAdapter);
      sourceFolderDialogField.setLabelText(I18N.NewSuiteWizardSourceFolder);
      sourceFolderDialogField.setButtonLabel(I18N.NewSuiteWizardBrowse);
   }

   IPath getSourceFolderFullPath() {
      final String text = sourceFolderDialogField.getText();
      if (text.length() > 0) { return new Path(text).makeAbsolute(); }
      return null;
   }

   void createNewSuiteLinkedToRunner(final IProgressMonitor pm) {
      try {
         final IPath sourcePath = getSourceFolderFullPath();

         if (sourcePath != null) {
            final String suiteName = newFileDialogField.getText();
            createMockSupportForFreeFunction(suiteName, sourcePath, pm);
            addSuiteToRunner(suiteName, pm);
         }
      } finally {
         pm.done();
      }
   }

   private void addSuiteToRunner(final String suitename, final IProgressMonitor pm) {
      if (!linkToRunnerCheck.isSelected()) { return; }

      final IASTFunctionDefinition testRunner = runners.get(runnerComboField.getSelectionIndex());
      runnerRefactoring.setTestRunner(testRunner);
      runnerRefactoring.setSuiteName(suitename);
      runnerRefactoring.setDestinationPath(getSourceFolderFullPath());
      final MockatorRefactoringRunner executor = new MockatorRefactoringRunner(runnerRefactoring);
      executor.runInCurrentThread(pm);
   }

   private void createMockSupportForFreeFunction(final String suitename, final IPath folderPath, final IProgressMonitor pm)
         throws OperationCanceledException {
      mockFunction.setSuiteName(suitename);
      mockFunction.setDestinationFolder(folderPath);
      mockFunction.execute(pm);
      openEditorWithNewFile();
   }

   private void openEditorWithNewFile() {
      UiUtil.runInDisplayThread((ignored) -> new FileEditorOpener(mockFunction.getNewFile()).openInEditor(), null);
   }

   private void createFileControls(final Composite parent, final int nColumns) {
      newFileDialogField.doFillIntoGrid(parent, nColumns);
      final Text textControl = newFileDialogField.getTextControl(null);
      LayoutUtil.setWidthHint(textControl, convertWidthInCharsToPixels(50));
      textControl.addFocusListener(new StatusFocusListener(NEW_FILE_ID));
      createSeparator(parent, nColumns);
      final Button button = (Button) linkToRunnerCheck.doFillIntoGrid(parent, nColumns)[0];
      button.addSelectionListener(new SelectionAdapter() {

         @Override
         public void widgetSelected(final SelectionEvent e) {
            final boolean selection = button.getSelection();
            runnerComboField.setEnabled(selection);
            setErrorMessage(null);
            if (selection) {
               final String[] runners2 = getRunners();
               if (runners2.length == 0) {
                  setErrorMessage(I18N.NewSuiteWizardNoRunners);
                  runnerComboField.setEnabled(false);
               }
               runnerComboField.setItems(runners2);
               if (runners2.length == 1) {
                  runnerComboField.selectItem(0);
               }
            }
         }
      });
      linkToRunnerCheck.doFillIntoGrid(parent, nColumns);
      runnerComboField.doFillIntoGrid(parent, nColumns);
      final Combo comboControl = runnerComboField.getComboControl(null);
      LayoutUtil.setWidthHint(comboControl, convertWidthInCharsToPixels(50));
      runnerComboField.setEnabled(false);
   }

   private String[] getRunners() {
      try {
         if (runners == null) {
            getWizard().getContainer().run(true, false, monitor -> {
               try {
                  runners = new RunnerFinder(mockatorCProject).findTestRunners(monitor);
               } catch (final CoreException e) {
                  throw new InvocationTargetException(e);
               }

            });
         }
         final String[] runnerStrings = new String[runners.size()];
         int i = 0;
         for (final IASTFunctionDefinition func : runners) {
            runnerStrings[i++] = func.getDeclarator().getName().toString();
         }
         return runnerStrings;
      } catch (final InvocationTargetException e) {
         throw new ILTISException(e).rethrowUnchecked();
      } catch (final InterruptedException e) {
         Thread.currentThread().interrupt();
      }

      return new String[] { I18N.NewSuiteWizardNoRunners };
   }

   private void createSeparator(final Composite composite, final int nColumns) {
      new Separator(SWT.SEPARATOR | SWT.HORIZONTAL).doFillIntoGrid(composite, nColumns, convertHeightInCharsToPixels(1));
   }

   private IProject getCurrentProject() {
      final IPath folderPath = getSourceFolderFullPath();
      if (folderPath != null) { return PathUtil.getEnclosingProject(folderPath); }
      return null;
   }

   private IStatus fileNameChanged() {
      final StatusInfo status = new StatusInfo();
      final IPath filePath = getFileFullPath();

      if (filePath == null) {
         status.setError(I18N.NewSuiteWizardEnterSuiteName);
         return status;
      }

      final IPath sourceFolderPath = getSourceFolderFullPath();
      if (sourceFolderPath == null || !sourceFolderPath.isPrefixOf(filePath)) {
         status.setError(I18N.NewSuiteWizardFileMustBeInsideSourceFolder);
         return status;
      }

      final StatusInfo headerStatus = headerFileAlreadyExists(filePath);
      if (headerStatus != null) { return headerStatus; }

      final StatusInfo sourceStatus = sourceFileAlreadyExists(filePath);
      if (sourceStatus != null) { return sourceStatus; }

      final IPath folderPath = filePath.removeLastSegments(1).makeRelative();
      final IResource folder = WorkspaceUtil.getWorkspaceRoot().findMember(folderPath);

      if (folder == null || !folder.exists() || folder.getType() != IResource.PROJECT && folder.getType() != IResource.FOLDER) {
         status.setError(I18N.NewSuiteWizardFolder + folderPath + I18N.NewSuiteWizardNotExisting);
         return status;
      }

      final IStatus convStatus = CConventions.validateSourceFileName(getCurrentProject(), filePath.lastSegment());
      if (convStatus.getSeverity() == IStatus.ERROR) {
         status.setError(I18N.NewSuiteWizardFileNameInvalid + convStatus.getMessage() + ".");
         return status;
      }

      if (!newFileDialogField.getText().matches("\\w+")) {
         status.setError(I18N.NewSuiteWizardInvalidIdentifier);
         return status;
      }

      return status;
   }

   private static StatusInfo sourceFileAlreadyExists(final IPath filePath) {
      final IPath sourcePath = new Path(filePath.toPortableString().concat(MockatorConstants.SOURCE_SUFFIX));
      return checkIfFileExists(sourcePath);
   }

   private static StatusInfo headerFileAlreadyExists(final IPath filePath) {
      final IPath headerPath = new Path(filePath.toPortableString().concat(MockatorConstants.HEADER_SUFFIX));
      return checkIfFileExists(headerPath);
   }

   private static StatusInfo checkIfFileExists(final IPath filePath) {
      final StatusInfo status = new StatusInfo();
      final IResource file = WorkspaceUtil.getWorkspaceRoot().findMember(filePath);

      if (file != null && file.exists()) {
         if (file.getType() == IResource.FILE) {
            status.setError(I18N.NewSuiteWizardFileAlreadyExist.concat(": ").concat(file.getName()));
         } else if (file.getType() == IResource.FOLDER) {
            status.setError(I18N.NewSuiteWizardFolderAlreadyExists.concat(": ").concat(file.getName()));
         } else {
            status.setError(I18N.NewSuiteWizardResourceAlreadyExists.concat(": ").concat(file.getName()));
         }
         return status;
      }

      return null;
   }

   private IPath getFileFullPath() {
      final String str = newFileDialogField.getText();
      IPath path = null;

      if (str.length() > 0) {
         path = new Path(str);
         if (!path.isAbsolute()) {
            final IPath folderPath = getSourceFolderFullPath();
            if (folderPath != null) {
               path = folderPath.append(path);
            }
         }
      }

      return path;
   }

   @Override
   public void createControl(final Composite parent) {
      initializeDialogUnits(parent);
      final Composite composite = new Composite(parent, SWT.NONE);
      final int nColumns = 3;
      final GridLayout layout = new GridLayout();
      layout.numColumns = nColumns;
      composite.setLayout(layout);
      composite.setLayoutData(new GridData(GridData.FILL_BOTH));
      composite.setFont(parent.getFont());
      createSourceFolderControls(composite, nColumns);
      createFileControls(composite, nColumns);
      new Composite(composite, SWT.NO_FOCUS).setLayoutData(new GridData(1, 1));
      composite.layout();
      setErrorMessage(null);
      setMessage(null);
      setControl(composite);
   }

   private void createSourceFolderControls(final Composite parent, final int nColumns) {
      sourceFolderDialogField.doFillIntoGrid(parent, nColumns);
      final Text textControl = sourceFolderDialogField.getTextControl(null);
      LayoutUtil.setWidthHint(textControl, convertWidthInCharsToPixels(50));
      textControl.addFocusListener(new StatusFocusListener(SOURCE_FOLDER_ID));
   }

   private void handleFieldChanged(final int fields) {
      if (fields == 0) { return; }

      if (fieldChanged(fields, SOURCE_FOLDER_ID)) {
         fSourceFolderStatus = sourceFolderChanged();
      }

      if (fieldChanged(fields, NEW_FILE_ID)) {
         fNewFileStatus = fileNameChanged();
      }

      doStatusUpdate();
   }

   private void doStatusUpdate() {
      final IStatus lastStatus = getLastFocusedStatus();
      final IStatus[] status = new IStatus[] { lastStatus, fSourceFolderStatus != lastStatus ? fSourceFolderStatus : STATUS_OK,
                                               fNewFileStatus != lastStatus ? fNewFileStatus : STATUS_OK, };
      updateStatus(status);
   }

   private void updateStatus(final IStatus[] status) {
      updateStatus(StatusUtil.getMostSevere(status));
   }

   private void updateStatus(final IStatus status) {
      setPageComplete(!status.matches(IStatus.ERROR));
      StatusUtil.applyToStatusLine(this, status);
   }

   private IStatus getLastFocusedStatus() {
      switch (fLastFocusedField) {
      case SOURCE_FOLDER_ID:
         return fSourceFolderStatus;
      case NEW_FILE_ID:
         return fNewFileStatus;
      default:
         return STATUS_OK;
      }
   }

   private static boolean fieldChanged(final int fields, final int fieldID) {
      return (fields & fieldID) != 0;
   }

   private IStatus sourceFolderChanged() {
      final StatusInfo status = new StatusInfo();

      final IPath folderPath = getSourceFolderFullPath();
      if (folderPath == null) {
         status.setError(I18N.NewSuiteWizardBrowseFolderNameEmpty);
         return status;
      }

      final IResource res = WorkspaceUtil.getWorkspaceRoot().findMember(folderPath);

      if (res != null && res.exists()) {
         final int resType = res.getType();
         if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
            final IProject proj = res.getProject();
            if (!proj.isOpen()) {
               status.setError(folderPath + I18N.NewSuiteWizardIsNotProjectOrFolder);
               return status;
            }
            if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
               if (resType == IResource.PROJECT) {
                  status.setError(I18N.NewSuiteWizardNotaCppProject);
                  return status;
               }
               status.setWarning(I18N.NewSuiteWizardIsNotInCppProject);
            }
            final ICElement e = CoreModel.getDefault().create(res.getFullPath());
            if (CModelUtil.getSourceFolder(e) == null) {
               status.setError(I18N.NewSuiteWizardFolder + folderPath + I18N.NewSuiteWizardIsNotSourceFolder);
               return status;
            }
         } else {
            status.setError(folderPath + I18N.NewSuiteWizardIsNotProjectOrFolder);
            return status;
         }
      } else {
         status.setError(I18N.NewSuiteWizardFolder + folderPath + I18N.NewSuiteWizardNotExisting);
         return status;
      }

      return status;
   }

   private final class StatusFocusListener implements FocusListener {

      private final int fieldID;
      private boolean   isFirstTime;

      public StatusFocusListener(final int fieldID) {
         this.fieldID = fieldID;
      }

      @Override
      public void focusGained(final FocusEvent e) {
         fLastFocusedField = fieldID;
         if (isFirstTime) {
            isFirstTime = false;
            return;
         }
         doStatusUpdate();
      }

      @Override
      public void focusLost(final FocusEvent e) {
         fLastFocusedField = 0;
         doStatusUpdate();
      }
   }

   private IPath chooseSourceFolder(final IPath initialPath) {
      ICElement initElement = getSourceFolderFromPath(initialPath);

      if (initElement instanceof ISourceRoot) {
         final ICProject cProject = initElement.getCProject();
         final ISourceRoot projRoot = cProject.findSourceRoot(cProject.getProject());

         if (projRoot != null && projRoot.equals(initElement)) {
            initElement = cProject;
         }
      }

      final SourceFolderSelectionDialog dialog = new SourceFolderSelectionDialog(getShell());
      dialog.setInput(CoreModel.create(WorkspaceUtil.getWorkspaceRoot()));
      dialog.setInitialSelection(initElement);

      if (dialog.open() != Window.OK) { return null; }

      final Object result = dialog.getFirstResult();

      if (!(result instanceof ICElement)) { return null; }

      final ICElement element = (ICElement) result;

      if (element instanceof ICProject) {
         final ICProject cProject = (ICProject) element;
         final ISourceRoot folder = cProject.findSourceRoot(cProject.getProject());

         if (folder != null) { return folder.getResource().getFullPath(); }
      }

      return element.getResource().getFullPath();
   }

   private static ICElement getSourceFolderFromPath(IPath path) {
      if (path == null) { return null; }

      while (path.segmentCount() > 0) {
         final IResource res = WorkspaceUtil.getWorkspaceRoot().findMember(path);

         if (res != null && res.exists()) {
            final int resType = res.getType();
            if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
               final ICElement elem = CoreModel.getDefault().create(res.getFullPath());
               final ICContainer sourceFolder = CModelUtil.getSourceFolder(elem);
               if (sourceFolder != null) { return sourceFolder; }
               if (resType == IResource.PROJECT) { return elem; }
            }
         }

         path = path.removeLastSegments(1);
      }

      return null;
   }

   void setSourceFolderFullPath(final IPath folderPath, final boolean update) {
      final String str = folderPath != null ? folderPath.makeRelative().toString() : ""; // .makeRelative().toString();
      sourceFolderDialogField.setTextWithoutUpdate(str);
      if (update) {
         sourceFolderDialogField.dialogFieldChanged();
      }
   }

   class SourceFolderFieldAdapter {

      public void changeControlPressed() {
         final IPath oldFolderPath = getSourceFolderFullPath();
         final IPath newFolderPath = chooseSourceFolder(oldFolderPath);

         if (newFolderPath != null) {
            setSourceFolderFullPath(newFolderPath, false);
            handleFieldChanged(ALL_FIELDS);
         }
      }

      public void dialogFieldChanged() {
         handleFieldChanged(ALL_FIELDS);
      }
   }

   private void initFields(final ICElement elem) {
      fSourceFolderStatus = STATUS_OK;
      fNewFileStatus = STATUS_OK;
      fLastFocusedField = 0;
      initSourceFolder(elem);
      handleFieldChanged(ALL_FIELDS);
   }

   private void initSourceFolder(final ICElement elem) {
      ICContainer folder = null;

      if (elem != null) {
         folder = CModelUtil.getSourceFolder(elem);

         if (folder == null) {
            final ICProject cproject = elem.getCProject();

            if (cproject != null) {
               try {
                  if (cproject.exists()) {
                     final ISourceRoot[] roots = cproject.getSourceRoots();
                     if (roots != null && roots.length > 0) {
                        folder = roots[0];
                     }
                  }
               } catch (final CModelException e) {
                  throw new ILTISException(e).rethrowUnchecked();
               }

               if (folder == null) {
                  folder = cproject.findSourceRoot(cproject.getResource());
               }
            }
         }
      }

      setSourceFolderFullPath(folder != null ? folder.getResource().getFullPath() : null, false);
   }
}
