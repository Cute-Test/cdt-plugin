/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

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
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.viewsupport.IViewPartInputProvider;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.core.refactoring.Change;
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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;

import ch.hsr.ifs.cute.core.headers.CuteHeaders;
import ch.hsr.ifs.cute.ui.CuteUIPlugin;

public class NewSuiteFileCreationWizardPage extends WizardPage {

	private static final int SOURCE_FOLDER_ID = 1;
	private static final int NEW_FILE_ID = 2;
	private static final int ALL_FIELDS = SOURCE_FOLDER_ID | NEW_FILE_ID;

	private final StringDialogField newFileDialogField;
	private final SelectionButtonDialogField linkToRunnerCheck;
	private final IStatus STATUS_OK = new StatusInfo();
	private final StringButtonDialogField sourceFolderDialogField;
	private final IWorkspaceRoot workspaceRoot;
	private IStatus fSourceFolderStatus;
	private IStatus fNewFileStatus;
	private int fLastFocusedField;
	private ICProject cProject;
	private RunnerFinder runnerFinder;
	private final ComboDialogField runnerComboField;
	private List<IASTFunctionDefinition> runners;

	public NewSuiteFileCreationWizardPage() {
		super(Messages.getString("NewSuiteFileCreationWizardPage.1"));

		setDescription(Messages.getString("NewSuiteFileCreationWizardPage.2"));

		workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		SourceFolderFieldAdapter sourceFolderAdapter = new SourceFolderFieldAdapter();
		sourceFolderDialogField = new StringButtonDialogField(sourceFolderAdapter);
		sourceFolderDialogField.setDialogFieldListener(sourceFolderAdapter);
		sourceFolderDialogField.setLabelText(Messages.getString("NewSuiteFileCreationWizardPage.3"));
		sourceFolderDialogField.setButtonLabel(Messages.getString("NewSuiteFileCreationWizardPage.4"));

		newFileDialogField = new StringDialogField();
		newFileDialogField.setDialogFieldListener(new IDialogFieldListener() {
			@Override
			public void dialogFieldChanged(DialogField field) {
				handleFieldChanged(NEW_FILE_ID);
			}
		});
		newFileDialogField.setLabelText(Messages.getString("NewSuiteFileCreationWizardPage.SuiteName"));

		linkToRunnerCheck = new SelectionButtonDialogField(SWT.CHECK);
		linkToRunnerCheck.setLabelText(Messages.getString("NewSuiteFileCreationWizardPage.LinkToRunner"));
		// generate list of runners
		// prompt selection
		runnerComboField = new ComboDialogField(SWT.READ_ONLY);
		runnerComboField.setLabelText(Messages.getString("NewSuiteFileCreationWizardPage.chooseRunMethod"));

		fSourceFolderStatus = STATUS_OK;
		fNewFileStatus = fileNameChanged();
		fLastFocusedField = 0;
	}

	/**
	 * @since 4.0
	 */
	public IPath getSourceFolderFullPath() {
		String text = sourceFolderDialogField.getText();
		if (text.length() > 0)
			return new Path(text).makeAbsolute();
		return null;
	}

	public void createFile(IProgressMonitor monitor) throws CoreException {
		IPath filePath = getFileFullPath();
		if (filePath != null) {
			if (monitor == null)
				monitor = new NullProgressMonitor();
			try {
				IPath folderPath = getSourceFolderFullPath();
				if (folderPath != null) {
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					IWorkspaceRoot root = workspace.getRoot();

					String suitename = newFileDialogField.getText();

					if (folderPath.segmentCount() == 1) {
						IProject project = root.getProject(folderPath.toPortableString());
						CuteHeaders headers = CuteUIPlugin.getCuteVersion(project);
						headers.copySuiteFiles(project, monitor, suitename, true);
					} else {
						IProject project = root.getProject(folderPath.segments()[0]);
						CuteHeaders headers = CuteUIPlugin.getCuteVersion(project);
						IFolder folder = root.getFolder(folderPath);
						headers.copySuiteFiles(folder, monitor, suitename, false);
					}
					if (linkToRunnerCheck.isSelected()) {
						addSuiteToRunner(suitename, runnerComboField.getSelectionIndex(), monitor);
					}
				}
			} finally {
				monitor.done();
			}
		}
	}

	private void addSuiteToRunner(String suitename, int selectionIndex, IProgressMonitor monitor) throws CoreException {
		IASTFunctionDefinition testRunner = runners.get(selectionIndex);
		LinkSuiteToRunnerProcessor processor = new LinkSuiteToRunnerProcessor(testRunner, suitename);
		Change change = processor.getLinkSuiteToRunnerChange();
		change.perform(monitor);
	}

	private void createFileControls(Composite parent, int nColumns) {
		newFileDialogField.doFillIntoGrid(parent, nColumns);
		Text textControl = newFileDialogField.getTextControl(null);
		LayoutUtil.setWidthHint(textControl, convertWidthInCharsToPixels(50));
		textControl.addFocusListener(new StatusFocusListener(NEW_FILE_ID));
		createSeparator(parent, nColumns);
		final Button button = (Button) linkToRunnerCheck.doFillIntoGrid(parent, nColumns)[0];
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean selection = button.getSelection();
				runnerComboField.setEnabled(selection);
				if (selection) {
					String[] runners2 = getRunners();
					if (runners2.length == 0) {
						setErrorMessage(Messages.getString("NewSuiteFileCreationWizardPage.noTestRunners"));
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
		Combo comboControl = runnerComboField.getComboControl(null);
		LayoutUtil.setWidthHint(comboControl, convertWidthInCharsToPixels(50));
		runnerComboField.setEnabled(false);
	}

	private String[] getRunners() {
		try {
			if (runners == null) {
				getWizard().getContainer().run(true, false, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							runners = runnerFinder.findTestRunners(monitor);
						} catch (CoreException e) {
							throw new InvocationTargetException(e);
						}

					}
				});

			}
			String[] runnerStrings = new String[runners.size()];
			int i = 0;
			for (IASTFunctionDefinition func : runners) {
				runnerStrings[i++] = func.getDeclarator().getName().toString();
			}
			return runnerStrings;
		} catch (InvocationTargetException e) {
			CuteUIPlugin.log("Exception while finding runners", e);
		} catch (InterruptedException e) {
			CuteUIPlugin.log("Exception while finding runners", e);
		}
		return new String[] { Messages.getString("NewSuiteFileCreationWizardPage.noRunners") };
	}

	private void createSeparator(Composite composite, int nColumns) {
		(new Separator(SWT.SEPARATOR | SWT.HORIZONTAL)).doFillIntoGrid(composite, nColumns, convertHeightInCharsToPixels(1));
	}

	private IWorkspaceRoot getWorkspaceRoot() {
		return workspaceRoot;
	}

	private IProject getCurrentProject() {
		IPath folderPath = getSourceFolderFullPath();
		if (folderPath != null) {
			return PathUtil.getEnclosingProject(folderPath);
		}
		return null;
	}

	private IStatus fileNameChanged() {
		StatusInfo status = new StatusInfo();

		IPath filePath = getFileFullPath();
		if (filePath == null) {
			status.setError(Messages.getString("NewSuiteFileCreationWizardPage.EnterSuiteName"));
			return status;
		}

		IPath sourceFolderPath = getSourceFolderFullPath();
		if (sourceFolderPath == null || !sourceFolderPath.isPrefixOf(filePath)) {
			status.setError(Messages.getString("NewSuiteFileCreationWizardPage.FileMustBeInsideSourceFolder"));
			return status;
		}

		// check if header file already exists
		IPath headerPath = new Path(filePath.toPortableString().concat(".h"));
		StatusInfo headerStatus = checkIfFileExists(headerPath);
		if (headerStatus != null) {
			return headerStatus;
		}

		// check if source file already exists
		IPath sourcePath = new Path(filePath.toPortableString().concat(".cpp"));
		StatusInfo sourceStatus = checkIfFileExists(sourcePath);
		if (sourceStatus != null) {
			return sourceStatus;
		}

		// check if folder exists
		IPath folderPath = filePath.removeLastSegments(1).makeRelative();
		IResource folder = getWorkspaceRoot().findMember(folderPath);
		if (folder == null || !folder.exists() || (folder.getType() != IResource.PROJECT && folder.getType() != IResource.FOLDER)) {
			status.setError(Messages.getString("NewSuiteFileCreationWizardPage.Folder") + folderPath
					+ Messages.getString("NewSuiteFileCreationWizardPage.DoesNotExist"));
			return status;
		}

		IStatus convStatus = CConventions.validateSourceFileName(getCurrentProject(), filePath.lastSegment());
		if (convStatus.getSeverity() == IStatus.ERROR) {
			status.setError(Messages.getString("NewSuiteFileCreationWizardPage.0") + convStatus.getMessage() + ".");
			return status;
		}
		if (!newFileDialogField.getText().matches("\\w+")) {
			status.setError("Invalid identifier. Only letters, digits and underscore are accepted.");
			return status;
		}
		return status;
	}

	private StatusInfo checkIfFileExists(IPath filePath) {
		StatusInfo status = new StatusInfo();
		IResource file = getWorkspaceRoot().findMember(filePath);
		if (file != null && file.exists()) {
			if (file.getType() == IResource.FILE) {
				status.setError(Messages.getString("NewSuiteFileCreationWizardPage.FileAlreadyExist").concat(": ").concat(file.getName()));
			} else if (file.getType() == IResource.FOLDER) {
				status.setError(Messages.getString("NewSuiteFileCreationWizardPage.FolderAlreadyExists").concat(": ").concat(file.getName()));
			} else {
				status.setError(Messages.getString("NewSuiteFileCreationWizardPage.ResourceAlreadyExists").concat(": ").concat(file.getName()));
			}
			return status;
		}
		return null;
	}

	private IPath getFileFullPath() {
		String str = newFileDialogField.getText();
		IPath path = null;
		if (str.length() > 0) {
			path = new Path(str);
			if (!path.isAbsolute()) {
				IPath folderPath = getSourceFolderFullPath();
				if (folderPath != null)
					path = folderPath.append(path);
			}
		}
		return path;
	}

	/**
	 * @since 4.0
	 */
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NONE);
		int nColumns = 3;

		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());

		createSourceFolderControls(composite, nColumns);

		createFileControls(composite, nColumns);
		(new Composite(composite, SWT.NO_FOCUS)).setLayoutData(new GridData(1, 1));

		composite.layout();
		setControl(composite);
	}

	private void createSourceFolderControls(Composite parent, int nColumns) {
		sourceFolderDialogField.doFillIntoGrid(parent, nColumns);
		Text textControl = sourceFolderDialogField.getTextControl(null);
		LayoutUtil.setWidthHint(textControl, convertWidthInCharsToPixels(50));
		textControl.addFocusListener(new StatusFocusListener(SOURCE_FOLDER_ID));
	}

	private void handleFieldChanged(int fields) {
		if (fields == 0)
			return; // no change

		if (fieldChanged(fields, SOURCE_FOLDER_ID)) {
			fSourceFolderStatus = sourceFolderChanged();
		}
		if (fieldChanged(fields, NEW_FILE_ID)) {
			fNewFileStatus = fileNameChanged();
		}
		doStatusUpdate();
	}

	private void doStatusUpdate() {
		updateStatus(new IStatus[] { getLastFocusedStatus(), fSourceFolderStatus, fNewFileStatus, });
	}

	private void updateStatus(IStatus[] status) {
		updateStatus(StatusUtil.getMostSevere(status));
	}

	private void updateStatus(IStatus status) {
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

	private boolean fieldChanged(int fields, int fieldID) {
		return ((fields & fieldID) != 0);
	}

	private IStatus sourceFolderChanged() {
		StatusInfo status = new StatusInfo();

		IPath folderPath = getSourceFolderFullPath();
		if (folderPath == null) {
			status.setError(Messages.getString("NewSuiteFileCreationWizardPage.5"));
			return status;
		}

		IResource res = workspaceRoot.findMember(folderPath);
		if (res != null && res.exists()) {
			int resType = res.getType();
			if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
				IProject proj = res.getProject();
				if (!proj.isOpen()) {
					status.setError(folderPath + Messages.getString("NewSuiteFileCreationWizardPage.6"));
					return status;
				}
				if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
					if (resType == IResource.PROJECT) {
						status.setError(Messages.getString("NewSuiteFileCreationWizardPage.7"));
						return status;
					}
					status.setWarning(Messages.getString("NewSuiteFileCreationWizardPage.8"));
				}
				ICElement e = CoreModel.getDefault().create(res.getFullPath());
				if (CModelUtil.getSourceFolder(e) == null) {
					status.setError(Messages.getString("NewSuiteFileCreationWizardPage.9") + folderPath
							+ Messages.getString("NewSuiteFileCreationWizardPage.10"));
					return status;
				}
			} else {
				status.setError(folderPath + Messages.getString("NewSuiteFileCreationWizardPage.11"));
				return status;
			}
		} else {
			status.setError(Messages.getString("NewSuiteFileCreationWizardPage.12") + folderPath + Messages.getString("NewSuiteFileCreationWizardPage.13"));
			return status;
		}

		return status;
	}

	private final class StatusFocusListener implements FocusListener {
		private final int fieldID;
		private boolean isFirstTime;

		public StatusFocusListener(int fieldID) {
			this.fieldID = fieldID;
		}

		@Override
		public void focusGained(FocusEvent e) {
			fLastFocusedField = this.fieldID;
			if (isFirstTime) {
				isFirstTime = false;
				return;
			}
			doStatusUpdate();
		}

		@Override
		public void focusLost(FocusEvent e) {
			fLastFocusedField = 0;
			doStatusUpdate();
		}
	}

	private IPath chooseSourceFolder(IPath initialPath) {
		ICElement initElement = getSourceFolderFromPath(initialPath);
		if (initElement instanceof ISourceRoot) {
			ICProject cProject = initElement.getCProject();
			ISourceRoot projRoot = cProject.findSourceRoot(cProject.getProject());
			if (projRoot != null && projRoot.equals(initElement))
				initElement = cProject;
		}

		SourceFolderSelectionDialog dialog = new SourceFolderSelectionDialog(getShell());
		dialog.setInput(CoreModel.create(workspaceRoot));
		dialog.setInitialSelection(initElement);

		if (dialog.open() == Window.OK) {
			Object result = dialog.getFirstResult();
			if (result instanceof ICElement) {
				ICElement element = (ICElement) result;
				if (element instanceof ICProject) {
					ICProject cproject = (ICProject) element;
					ISourceRoot folder = cproject.findSourceRoot(cproject.getProject());
					if (folder != null)
						return folder.getResource().getFullPath();
				}
				return element.getResource().getFullPath();
			}
		}
		return null;
	}

	private ICElement getSourceFolderFromPath(IPath path) {
		if (path == null)
			return null;
		while (path.segmentCount() > 0) {
			IResource res = workspaceRoot.findMember(path);
			if (res != null && res.exists()) {
				int resType = res.getType();
				if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
					ICElement elem = CoreModel.getDefault().create(res.getFullPath());
					ICContainer sourceFolder = CModelUtil.getSourceFolder(elem);
					if (sourceFolder != null)
						return sourceFolder;
					if (resType == IResource.PROJECT) {
						return elem;
					}
				}
			}
			path = path.removeLastSegments(1);
		}
		return null;
	}

	/**
	 * @since 4.0
	 */
	public void setSourceFolderFullPath(IPath folderPath, boolean update) {
		String str = (folderPath != null) ? folderPath.makeRelative().toString() : "";
		sourceFolderDialogField.setTextWithoutUpdate(str);
		if (update) {
			sourceFolderDialogField.dialogFieldChanged();
		}
	}

	private class SourceFolderFieldAdapter implements IStringButtonAdapter, IDialogFieldListener {
		@Override
		public void changeControlPressed(DialogField field) {
			IPath oldFolderPath = getSourceFolderFullPath();
			IPath newFolderPath = chooseSourceFolder(oldFolderPath);
			if (newFolderPath != null) {
				setSourceFolderFullPath(newFolderPath, false);
				handleFieldChanged(ALL_FIELDS);
			}
		}

		@Override
		public void dialogFieldChanged(DialogField field) {
			handleFieldChanged(ALL_FIELDS);
		}
	}

	public void init(IStructuredSelection selection) {
		ICElement celem = getInitialCElement(selection);
		if (celem != null) {
			cProject = celem.getCProject();
			runnerFinder = new RunnerFinder(cProject);
			initFields(celem);
		} else {
			handleFieldChanged(SOURCE_FOLDER_ID);
		}
		doStatusUpdate();
	}

	private ICElement getInitialCElement(IStructuredSelection selection) {
		ICElement celem = null;
		if (selection != null && !selection.isEmpty()) {
			Object selectedElement = selection.getFirstElement();
			if (selectedElement instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) selectedElement;

				celem = adaptable.getAdapter(ICElement.class);
				if (celem == null) {
					IResource resource = adaptable.getAdapter(IResource.class);
					if (resource != null && resource.getType() != IResource.ROOT) {
						while (celem == null && resource.getType() != IResource.PROJECT) {
							celem = resource.getAdapter(ICElement.class);
							resource = resource.getParent();
						}
						if (celem == null) {
							celem = CoreModel.getDefault().create(resource);
						}
					}
				}
			}
		}
		if (celem == null) {
			IWorkbenchPart part = CUIPlugin.getActivePage().getActivePart();
			if (part instanceof ContentOutline) {
				part = CUIPlugin.getActivePage().getActiveEditor();
			}

			if (part instanceof IViewPartInputProvider) {
				Object elem = ((IViewPartInputProvider) part).getViewPartInput();
				if (elem instanceof ICElement) {
					celem = (ICElement) elem;
				}
			}

			if (celem == null && part instanceof CEditor) {
				IEditorInput input = ((IEditorPart) part).getEditorInput();
				if (input != null) {
					final IResource res = input.getAdapter(IResource.class);
					if (res != null && res instanceof IFile) {
						celem = CoreModel.getDefault().create((IFile) res);
					}
				}
			}
		}

		if (celem == null || celem.getElementType() == ICElement.C_MODEL) {
			try {
				ICProject[] projects = CoreModel.create(getWorkspaceRoot()).getCProjects();
				if (projects.length == 1) {
					celem = projects[0];
				}
			} catch (CModelException e) {
				CUIPlugin.log(e);
			}
		}
		return celem;
	}

	private void initFields(ICElement elem) {
		initSourceFolder(elem);
		handleFieldChanged(ALL_FIELDS);
	}

	private void initSourceFolder(ICElement elem) {
		IContainer resource = null;
		if(elem.getResource() instanceof IFile) {
			IFile file = (IFile) elem.getResource();
			resource = file.getParent();
		} else if(elem.getResource() instanceof IFolder) {
			resource = (IFolder) elem.getResource();
		} else if(elem.getResource() instanceof IProject){
            IProject project = (IProject) elem.getResource();
            IResource src = project.findMember("src");
            if(src.exists() && src.getType() == IResource.FOLDER) {
                resource = (IContainer) src;
            }
		}
		setSourceFolderFullPath(resource != null ? resource.getFullPath() : null, false);
	}

}
