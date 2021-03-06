/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.wizards.newproject;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import ch.hsr.ifs.cute.headers.ICuteHeaders;
import ch.hsr.ifs.cute.ui.CuteUIPlugin;
import ch.hsr.ifs.cute.ui.ICuteWizardAddition;
import ch.hsr.ifs.cute.ui.ProjectTools;
import ch.hsr.ifs.cute.ui.wizards.VersionSelectionComposite;


/**
 * @author Emanuel Graf
 *
 */
public class NewProjectWizardPage extends MBSCustomPage implements ICheckStateListener {

    protected static final int             GRID_WIDTH = 2;
    protected Composite                    composite;
    private final IWizardPage              nextPage;
    private final IWizardPage              previousPage;
    private final ImageDescriptor          imageDesc;
    private VersionSelectionComposite      cuteVersionComp;
    private ArrayList<ICuteWizardAddition> additions;

    private CheckboxTableViewer    listViewer;
    private List<IProject>         libProjects;
    private final IWizardContainer wizardDialog;

    public boolean isLibrarySelectionActive = false;

    boolean errorMessageFlag = false;

    public NewProjectWizardPage(IWizardPage nextPage, IWizardPage previousPage, String pageId, IWizardContainer wc) {
        super(pageId);
        this.nextPage = nextPage;
        this.previousPage = previousPage;
        imageDesc = CuteUIPlugin.getImageDescriptor("cute_logo.png");
        wizardDialog = wc;
    }

    public NewProjectWizardPage(IWizardPage nextPage, IWizardPage previousPage, IWizardContainer wc) {
        this(nextPage, previousPage, "ch.hsr.ifs.cutelauncher.ui.CuteVersionPage", wc);
    }

    @Override
    protected boolean isCustomPageComplete() {
        if (isLibrarySelectionActive && getCheckedProjects().size() < 1) {
            return false;
        }
        return cuteVersionComp != null ? cuteVersionComp.isComplete() : !ICuteHeaders.loadedHeaders().isEmpty();
    }

    @Override
    public String getName() {
        return "referenceToLibraryPage";
    }

    @Override
    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.FILL);
        composite.setLayout(new GridLayout(GRID_WIDTH, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addCuteHeaderVersionSelectionDropdown();
        addWizardPageAdditions();
        addLibraryDependencyCheckmark();
        addLibSelectionList();
    }

    private void addCuteHeaderVersionSelectionDropdown() {
        cuteVersionComp = new VersionSelectionComposite(composite);
        GridData gridData = new GridData();
        gridData.horizontalSpan = GRID_WIDTH;
        cuteVersionComp.setLayoutData(gridData);
    }

    private void addWizardPageAdditions() {
        for (ICuteWizardAddition addition : getAdditions()) {
            Control newChild = addition.createComposite(composite);
            GridData gridData = new GridData();
            gridData.horizontalSpan = GRID_WIDTH;
            newChild.setLayoutData(gridData);
        }
    }

    private void addLibraryDependencyCheckmark() {
        final Button check = new Button(composite, SWT.CHECK);
        check.setText(NewProjectWizardMessages.AddLibraryDependency);
        check.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean checked = check.getSelection();
                listViewer.getControl().setEnabled(checked);
                isLibrarySelectionActive = checked;
                List<IProject> list = getCheckedProjects();
                errorMessageFlag = isLibrarySelectionActive && list.isEmpty();
                wizardDialog.updateMessage();
                wizardDialog.updateButtons();
            }
        });
    }

    private void addLibSelectionList() {
        libProjects = getLibProjects();
        listViewer = CheckboxTableViewer.newCheckList(composite, SWT.TOP | SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.grabExcessHorizontalSpace = true;
        data.verticalIndent = 20;
        data.horizontalSpan = GRID_WIDTH;
        listViewer.getTable().setLayoutData(data);
        listViewer.setLabelProvider(WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider());
        listViewer.setContentProvider(getContentProvider());
        listViewer.setComparator(new ViewerComparator());
        listViewer.setInput(libProjects);
        listViewer.addCheckStateListener(this);
        listViewer.getControl().setEnabled(false);
    }

    private IContentProvider getContentProvider() {
        return new IStructuredContentProvider() {

            @Override
            public void dispose() {}

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

            @Override
            @SuppressWarnings({ "rawtypes" })
            public Object[] getElements(Object inputElement) {
                if (inputElement instanceof List) {
                    List vec = (List) inputElement;
                    return vec.toArray();
                }
                return null;
            }
        };
    }

    @Override
    public String getDescription() {
        return NewProjectWizardMessages.SetCuteOptions;
    }

    @Override
    public String getErrorMessage() {
        return errorMessageFlag ? NewProjectWizardMessages.SelectLibraryToTest : cuteVersionComp.getErrorMessage();
    }

    @Override
    public String getTitle() {
        return NewProjectWizardMessages.CuteVersion;
    }

    public List<IProject> getCheckedProjects() {
        List<IProject> checkedProjects = new ArrayList<>();
        if (listViewer == null) {
            return checkedProjects;
        }
        for (Object obj : listViewer.getCheckedElements()) {
            if (obj instanceof IProject) {
                checkedProjects.add((IProject) obj);
            }
        }
        return checkedProjects;
    }

    @Override
    public void checkStateChanged(CheckStateChangedEvent event) {
        List<IProject> list = getCheckedProjects();
        errorMessageFlag = isLibrarySelectionActive && list.isEmpty();
        wizardDialog.updateMessage();
        wizardDialog.updateButtons();
    }

    private List<IProject> getLibProjects() {
        List<IProject> libProjects = new ArrayList<>();
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject project : projects) {
            if (ProjectTools.isLibraryProject(project)) {
                libProjects.add(project);
            }
        }
        return libProjects;
    }

    @Override
    public void setVisible(boolean visible) {
        composite.setVisible(visible);
    }

    @Override
    public Image getImage() {
        return imageDesc.createImage();
    }

    @Override
    public IWizardPage getNextPage() {
        return nextPage;
    }

    @Override
    public IWizardPage getPreviousPage() {
        return previousPage;
    }

    public String getCuteVersionString() {
        if (cuteVersionComp != null) {
            return cuteVersionComp.getVersionString();
        } else {
            try {
                return ICuteHeaders.loadedHeaders().last().getVersionString();
            } catch (NoSuchElementException e) {
                return null;
            }
        }
    }

    public List<ICuteWizardAddition> getAdditions() {
        if (additions == null) {
            additions = new ArrayList<>();
            try {
                IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(CuteUIPlugin.PLUGIN_ID, "wizardAddition");
                if (extension != null) {
                    IExtension[] extensions = extension.getExtensions();
                    for (IExtension extension2 : extensions) {
                        IConfigurationElement[] configElements = extension2.getConfigurationElements();
                        String className = configElements[0].getAttribute("compositeProvider");
                        Object newInstance = ((Class<?>) Platform.getBundle(extension2.getContributor().getName()).loadClass(className))
                                .getDeclaredConstructor().newInstance();
                        additions.add((ICuteWizardAddition) newInstance);
                    }
                }
            } catch (Exception ignored) {}
        }
        return additions;
    }

    @Override
    public void dispose() {
        composite.dispose();
    }

    @Override
    public Control getControl() {
        return composite;
    }

    @Override
    public String getMessage() {
        return null;
    }

    @Override
    public void performHelp() {
        // do nothing
    }

    @Override
    public void setDescription(String description) {
        // do nothing
    }

    @Override
    public void setTitle(String title) {
        // do nothing
    }

    @Override
    public void setImageDescriptor(ImageDescriptor image) {
        // do nothing
    }

}
