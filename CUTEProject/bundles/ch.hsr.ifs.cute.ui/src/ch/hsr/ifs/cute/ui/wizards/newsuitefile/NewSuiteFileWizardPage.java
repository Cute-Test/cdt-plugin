/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.wizards.newsuitefile;

import static ch.hsr.ifs.iltis.core.core.functional.Functional.also;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;

import ch.hsr.ifs.iltis.core.core.collections.CollectionUtil;

import ch.hsr.ifs.cute.headers.ICuteHeaders;
import ch.hsr.ifs.cute.ui.CuteUIPlugin;
import ch.hsr.ifs.cute.ui.dialogs.SourceFolderSelectionDialog;
import ch.hsr.ifs.cute.ui.fields.DialogField;
import ch.hsr.ifs.cute.ui.fields.SelectionField;
import ch.hsr.ifs.cute.ui.fields.SeparatorField;
import ch.hsr.ifs.cute.ui.fields.StringButtonField;
import ch.hsr.ifs.cute.ui.fields.StringDialogField;
import ch.hsr.ifs.cute.ui.utilities.StatusInfo;
import ch.hsr.ifs.cute.ui.utilities.StatusUtil;


public class NewSuiteFileWizardPage extends WizardPage {

    //@formatter:off
    // NOTE: Keep the declaration order as to prevent UI error message flickering 
    private static enum FormField {
        SOURCE_FOLDER,
        SUITE_NAME, 
        LINK_TO_RUNNER, 
    }
    //@formatter:on

    private final class LinkToRunnerFieldAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            handleFieldChanged(EnumSet.of(FormField.LINK_TO_RUNNER));
            //            linkToRunnerChanged((Button) e.widget);
        }
    }

    private final class SourceFolderFieldAdapter implements StringButtonField.Adapter, DialogField.Listener {

        @Override
        public void changeControlPressed(DialogField field) {
            IPath oldFolderPath = getSourceFolderFullPath();
            IPath newFolderPath = chooseSourceFolder(oldFolderPath);
            if (newFolderPath != null) {
                setSourceFolderFullPath(newFolderPath, false);
                handleFieldChanged(EnumSet.allOf(FormField.class));
            }
        }

        @Override
        public void dialogFieldChanged(DialogField field) {
            handleFieldChanged(EnumSet.allOf(FormField.class));
        }
    }

    private final class StatusFocusListener implements FocusListener {

        private final FormField fieldID;
        private boolean         isFirstTime;

        public StatusFocusListener(FormField fieldID) {
            this.fieldID = fieldID;
        }

        @Override
        public void focusGained(FocusEvent e) {
            fLastFocusedField = Optional.of(this.fieldID);
            if (isFirstTime) {
                isFirstTime = false;
                return;
            }
            doStatusUpdate();
        }

        @Override
        public void focusLost(FocusEvent e) {
            fLastFocusedField = Optional.empty();
            doStatusUpdate();
        }
    }

    private final static IStatus STATUS_OK         = new StatusInfo();
    private final static IStatus STATUS_NO_RUNNERS = new StatusInfo(IStatus.ERROR, NewSuiteFileMessages.NoTestRunnersFound);

    private ICProject                     fCProject;
    private final Map<FormField, IStatus> fFieldStatus      = new EnumMap<>(FormField.class);
    private Optional<FormField>           fLastFocusedField = Optional.empty();

    private final SelectionField    fLinkToRunner    = createLinkToRunnerControl();
    private final ComboDialogField  fRunnerSelection = createRunnerSelectionControl();
    private final StringButtonField fSourceFolder    = createSourceFolderControl();
    private final StringDialogField fSuiteName       = createSuiteNameControl();

    private RunnerFinder                 fRunnerFinder;
    private List<IASTFunctionDefinition> fRunners;
    private final IWorkspaceRoot         fWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

    public NewSuiteFileWizardPage() {
        super("newCuteSuiteFile");

        setDescription(NewSuiteFileMessages.Description);
        Arrays.stream(FormField.values()).forEach(f -> fFieldStatus.put(f, STATUS_OK));
        fFieldStatus.put(FormField.SUITE_NAME, suiteNameChanged());
    }

    @Override
    public void createControl(Composite parent) {
        final int COLUMN_COUNT = 3;

        initializeDialogUnits(parent);

        also(new Composite(parent, SWT.NONE), c -> {
            c.setLayout(also(new GridLayout(), l -> l.numColumns = COLUMN_COUNT));
            c.setLayoutData(new GridData(GridData.FILL_BOTH));
            c.setFont(parent.getFont());

            initializeSourceFolderControl(c, COLUMN_COUNT);
            initializeSuiteControl(c, COLUMN_COUNT);
            createSeparator(c, COLUMN_COUNT);
            initializeLinkToRunnerControl(c, COLUMN_COUNT);

            also(new Composite(c, SWT.NO_FOCUS), n -> n.setLayoutData(new GridData(1, 1)));
            c.layout();
            setControl(c);
        });
    }

    /**
     * Create the suite source files
     */
    public void createFiles(IProgressMonitor monitor) throws CoreException {
        IPath filePath = getFileFullPath();
        if (filePath != null) {
            if (monitor == null) monitor = new NullProgressMonitor();
            try {
                IPath folderPath = getSourceFolderFullPath();
                if (folderPath != null) {
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    IWorkspaceRoot root = workspace.getRoot();

                    String suitename = fSuiteName.getText();

                    if (folderPath.segmentCount() == 1) {
                        IProject project = root.getProject(folderPath.toPortableString());
                        ICuteHeaders headers = ICuteHeaders.getForProject(project);
                        headers.copySuiteFiles(project, monitor, suitename, true);
                    } else {
                        IProject project = root.getProject(folderPath.segments()[0]);
                        ICuteHeaders headers = ICuteHeaders.getForProject(project);
                        IFolder folder = root.getFolder(folderPath);
                        headers.copySuiteFiles(folder, monitor, suitename, false);
                    }
                    if (fLinkToRunner.isSelected()) {
                        addSuiteToRunner(suitename, fRunnerSelection.getSelectionIndex(), monitor);
                    }
                }
            } finally {
                monitor.done();
            }
        }
    }

    public void init(IStructuredSelection selection) {
        ICElement celem = getInitialCElement(selection);
        if (celem != null) {
            fCProject = celem.getCProject();
            fRunnerFinder = new RunnerFinder(fCProject);
            initFields(celem);
        } else {
            handleFieldChanged(EnumSet.of(FormField.SOURCE_FOLDER));
        }
        doStatusUpdate();
    }

    private void addSuiteToRunner(String suitename, int selectionIndex, IProgressMonitor monitor) throws CoreException {
        IASTFunctionDefinition testRunner = fRunners.get(selectionIndex);
        RunnerLinker processor = new RunnerLinker(testRunner, suitename);
        Change change = processor.getLinkSuiteToRunnerChange();
        change.perform(monitor);
    }

    private StatusInfo checkIfFileExists(IPath filePath) {
        StatusInfo status = new StatusInfo();
        IResource file = getWorkspaceRoot().findMember(filePath);
        if (file != null && file.exists()) {
            if (file.getType() == IResource.FILE) {
                status.setError(NLS.bind(NewSuiteFileMessages.FileAlreadyExists, file.getName()));
            } else if (file.getType() == IResource.FOLDER) {
                status.setError(NLS.bind(NewSuiteFileMessages.FolderAlreadyExists, file.getName()));
            } else {
                status.setError(NLS.bind(NewSuiteFileMessages.ResourceAlreadyExists, file.getName()));
            }
            return status;
        }
        return null;
    }

    private IPath chooseSourceFolder(IPath initialPath) {
        ICElement initElement = getSourceFolderFromPath(initialPath);
        if (initElement instanceof ISourceRoot) {
            ICProject cProject = initElement.getCProject();
            ISourceRoot projRoot = cProject.findSourceRoot(cProject.getProject());
            if (projRoot != null && projRoot.equals(initElement)) initElement = cProject;
        }

        SourceFolderSelectionDialog dialog = new SourceFolderSelectionDialog(getShell());
        dialog.setInput(CoreModel.create(fWorkspaceRoot));
        dialog.setInitialSelection(initElement);

        if (dialog.open() == Window.OK) {
            Object result = dialog.getFirstResult();
            if (result instanceof ICElement) {
                ICElement element = (ICElement) result;
                if (element instanceof ICProject) {
                    ICProject cproject = (ICProject) element;
                    ISourceRoot folder = cproject.findSourceRoot(cproject.getProject());
                    if (folder != null) return folder.getResource().getFullPath();
                }
                return element.getResource().getFullPath();
            }
        }
        return null;
    }

    private SelectionField createLinkToRunnerControl() {
        return also(new SelectionField(SelectionField.Style.CHECK), c -> {
            c.setDialogFieldListener(f -> handleFieldChanged(EnumSet.of(FormField.LINK_TO_RUNNER)));
            c.setLabelText(NewSuiteFileMessages.LinkToRunner);
        });
    }

    private ComboDialogField createRunnerSelectionControl() {
        return also(new ComboDialogField(SWT.READ_ONLY), c -> {
            c.setLabelText(NewSuiteFileMessages.ChooseExistingRunner);
        });
    }

    private void createSeparator(Composite composite, int nColumns) {
        (new SeparatorField(SWT.SEPARATOR | SWT.HORIZONTAL)).doFillIntoGrid(composite, nColumns, convertHeightInCharsToPixels(1));
    }

    private StringButtonField createSourceFolderControl() {
        SourceFolderFieldAdapter sourceFolderAdapter = new SourceFolderFieldAdapter();
        StringButtonField control = new StringButtonField(sourceFolderAdapter);
        control.setDialogFieldListener(sourceFolderAdapter);
        control.setLabelText(NewSuiteFileMessages.SourceFolder);
        control.setButtonLabel(NewSuiteFileMessages.Browse);
        return control;
    }

    private StringDialogField createSuiteNameControl() {
        return also(new StringDialogField(), c -> {
            c.setDialogFieldListener(f -> handleFieldChanged(EnumSet.of(FormField.SUITE_NAME)));
            c.setLabelText(NewSuiteFileMessages.SuiteName);
        });
    }

    private void doStatusUpdate() {
        also(CollectionUtil.list(getLastFocusedStatus()), l -> {
            l.addAll(fFieldStatus.values());
            updateStatus(StatusUtil.getMostSevere(l));
        });
    }

    private IProject getCurrentProject() {
        IPath folderPath = getSourceFolderFullPath();
        if (folderPath != null) {
            return PathUtil.getEnclosingProject(folderPath);
        }
        return null;
    }

    private IPath getFileFullPath() {
        String str = fSuiteName.getText();
        IPath path = null;
        if (str.length() > 0) {
            path = new Path(str);
            if (!path.isAbsolute()) {
                IPath folderPath = getSourceFolderFullPath();
                if (folderPath != null) path = folderPath.append(path);
            }
        }
        return path;
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

    private IStatus getLastFocusedStatus() {
        return fLastFocusedField.map(fFieldStatus::get).orElse(STATUS_OK);
    }

    private String[] getRunners() {
        try {
            if (fRunners == null) {
                getWizard().getContainer().run(true, false, monitor -> {
                    try {
                        fRunners = fRunnerFinder.findTestRunners(monitor);
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    }

                });

            }
            String[] runnerStrings = new String[fRunners.size()];
            int i = 0;
            for (IASTFunctionDefinition func : fRunners) {
                runnerStrings[i++] = func.getDeclarator().getName().toString();
            }
            return runnerStrings;
        } catch (InvocationTargetException e) {
            CuteUIPlugin.log("Exception while finding runners", e);
        } catch (InterruptedException e) {
            CuteUIPlugin.log("Exception while finding runners", e);
        }
        return new String[] { NewSuiteFileMessages.NoTestRunnersFound };
    }

    private ICElement getSourceFolderFromPath(IPath path) {
        if (path == null) return null;
        while (path.segmentCount() > 0) {
            IResource res = fWorkspaceRoot.findMember(path);
            if (res != null && res.exists()) {
                int resType = res.getType();
                if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
                    ICElement elem = CoreModel.getDefault().create(res.getFullPath());
                    ICContainer sourceFolder = CModelUtil.getSourceFolder(elem);
                    if (sourceFolder != null) return sourceFolder;
                    if (resType == IResource.PROJECT) {
                        return elem;
                    }
                }
            }
            path = path.removeLastSegments(1);
        }
        return null;
    }

    private IPath getSourceFolderFullPath() {
        String text = fSourceFolder.getText();
        if (text.length() > 0) return new Path(text).makeAbsolute();
        return null;
    }

    private IWorkspaceRoot getWorkspaceRoot() {
        return fWorkspaceRoot;
    }

    private void handleFieldChanged(EnumSet<FormField> fieldIds) {
        if (fieldIds.isEmpty()) {
            return;
        }

        if (fieldIds.contains(FormField.SOURCE_FOLDER)) {
            fFieldStatus.put(FormField.SOURCE_FOLDER, sourceFolderChanged());
        }

        if (fieldIds.contains(FormField.SUITE_NAME)) {
            fFieldStatus.put(FormField.SUITE_NAME, suiteNameChanged());
        }

        if (fieldIds.contains(FormField.LINK_TO_RUNNER)) {
            fFieldStatus.put(FormField.LINK_TO_RUNNER, linkToRunnerChanged());
        }

        doStatusUpdate();
    }

    private void initFields(ICElement elem) {
        initSourceFolder(elem);
        handleFieldChanged(EnumSet.allOf(FormField.class));
    }

    private void initializeLinkToRunnerControl(Composite parent, int columns) {
        also((Button) (fLinkToRunner.doFillIntoGrid(parent, columns)[0]), button -> {
            button.addSelectionListener(new LinkToRunnerFieldAdapter());
        });

        fRunnerSelection.doFillIntoGrid(parent, columns);
        also(fRunnerSelection.getComboControl(null), c -> {
            LayoutUtil.setWidthHint(c, convertWidthInCharsToPixels(50));
        });
        fRunnerSelection.setEnabled(false);
    }

    private void initializeSourceFolderControl(Composite parent, int columns) {
        fSourceFolder.doFillIntoGrid(parent, columns);
        also(fSourceFolder.getTextControl(null), t -> {
            LayoutUtil.setWidthHint(t, convertWidthInCharsToPixels(50));
            t.addFocusListener(new StatusFocusListener(FormField.SOURCE_FOLDER));
        });
    }

    private void initializeSuiteControl(Composite parent, int columns) {
        fSuiteName.doFillIntoGrid(parent, columns);
        also(fSuiteName.getTextControl(null), t -> {
            LayoutUtil.setWidthHint(t, convertWidthInCharsToPixels(50));
            t.addFocusListener(new StatusFocusListener(FormField.SUITE_NAME));
        });
    }

    private void initSourceFolder(ICElement elem) {
        IContainer resource = null;
        if (elem.getResource() instanceof IFile) {
            IFile file = (IFile) elem.getResource();
            resource = file.getParent();
        } else if (elem.getResource() instanceof IFolder) {
            resource = (IFolder) elem.getResource();
        } else if (elem.getResource() instanceof IProject) {
            IProject project = (IProject) elem.getResource();
            IResource src = project.findMember("src");
            if (src.exists() && src.getType() == IResource.FOLDER) {
                resource = (IContainer) src;
            }
        }
        setSourceFolderFullPath(resource != null ? resource.getFullPath() : null, false);
    }

    private void setSourceFolderFullPath(IPath folderPath, boolean update) {
        String str = (folderPath != null) ? folderPath.makeRelative().toString() : "";
        fSourceFolder.setTextWithoutUpdate(str);
        if (update) {
            fSourceFolder.dialogFieldChanged();
        }
    }

    private IStatus linkToRunnerChanged() {
        final boolean isSelected = fLinkToRunner.isSelected();
        if (!isSelected) {
            return STATUS_OK;
        }

        final String[] runners = getRunners();
        final int currentSelection = fRunnerSelection.getSelectionIndex();

        fRunnerSelection.setEnabled(runners.length > 0);
        fRunnerSelection.setItems(runners);
        fRunnerSelection.selectItem(currentSelection > -1 ? currentSelection : 0);

        return runners.length == 0 ? STATUS_NO_RUNNERS : STATUS_OK;
    }

    private IStatus sourceFolderChanged() {
        StatusInfo status = new StatusInfo();

        IPath folderPath = getSourceFolderFullPath();
        if (folderPath == null) {
            status.setError(NewSuiteFileMessages.EmptySourceFolderName);
            return status;
        }

        IResource res = fWorkspaceRoot.findMember(folderPath);
        if (res != null && res.exists()) {
            int resType = res.getType();
            if (resType == IResource.PROJECT || resType == IResource.FOLDER) {
                IProject proj = res.getProject();
                if (!proj.isOpen()) {
                    status.setError(NLS.bind(NewSuiteFileMessages.NotAProjectFolder, folderPath));
                    return status;
                }
                if (!CoreModel.hasCCNature(proj) && !CoreModel.hasCNature(proj)) {
                    if (resType == IResource.PROJECT) {
                        status.setError(NLS.bind(NewSuiteFileMessages.NotACXXProject, proj.getName()));
                        return status;
                    }
                    status.setWarning(NLS.bind(NewSuiteFileMessages.NotInACXXProject, res.getName()));
                }
                ICElement e = CoreModel.getDefault().create(res.getFullPath());
                if (CModelUtil.getSourceFolder(e) == null) {
                    status.setError(NLS.bind(NewSuiteFileMessages.NotASourceFolder, folderPath));
                    return status;
                }
            } else {
                status.setError(NLS.bind(NewSuiteFileMessages.NotAProjectOrFolder, folderPath));
                return status;
            }
        } else {
            status.setError(NLS.bind(NewSuiteFileMessages.DoesNotExist, folderPath));
            return status;
        }

        return status;
    }

    private IStatus suiteNameChanged() {
        StatusInfo status = new StatusInfo();

        IPath filePath = getFileFullPath();
        if (filePath == null) {
            status.setError(NewSuiteFileMessages.EnterSuiteName);
            return status;
        }

        IPath sourceFolderPath = getSourceFolderFullPath();
        if (sourceFolderPath == null || !sourceFolderPath.isPrefixOf(filePath)) {
            status.setError(NewSuiteFileMessages.FileMustBeInsideSourceFolder);
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
            status.setError(NLS.bind(NewSuiteFileMessages.DoesNotExist, folderPath));
            return status;
        }

        IStatus convStatus = CConventions.validateSourceFileName(getCurrentProject(), filePath.lastSegment());
        if (convStatus.getSeverity() == IStatus.ERROR) {
            status.setError(NLS.bind(NewSuiteFileMessages.InvalidFileName, filePath.lastSegment(), convStatus.getMessage()));
            return status;
        }
        if (!fSuiteName.getText().matches("\\w+")) {
            status.setError("Invalid identifier. Only letters, digits and underscore are accepted.");
            return status;
        }
        return status;
    }

    private void updateStatus(IStatus status) {
        setPageComplete(!status.matches(IStatus.ERROR));
        StatusUtil.applyToStatusLine(this, status);
    }

}
