package ch.hsr.ifs.cutelauncher.ui;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.cdt.internal.ui.wizards.filewizard.AbstractFileCreationWizardPage;
import org.eclipse.cdt.internal.ui.wizards.filewizard.NewFileWizardMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class NewSuiteFileCreationWizardPage extends
		AbstractFileCreationWizardPage {

	private ITranslationUnit fNewFileTU = null;
	private final StringDialogField fNewFileDialogField;
	private final SelectionButtonDialogField fSelection;
	
	public NewSuiteFileCreationWizardPage(){
		super("Custom Suite");
		
		setDescription("Create a new Suite");
		
		fNewFileDialogField = new StringDialogField();
		fNewFileDialogField.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				handleFieldChanged(NEW_FILE_ID);
			}
		});
		fNewFileDialogField.setLabelText("Suite name:");
		
		fSelection=new SelectionButtonDialogField(SWT.CHECK);
		fSelection.setLabelText("Link to runner ");
		//generate list of runners
		//prompt selection
	}
	@Override
	public void createFile(IProgressMonitor monitor) throws CoreException {
        IPath filePath = getFileFullPath();
        if (filePath != null) {
            if (monitor == null)
	            monitor = new NullProgressMonitor();
            try {
	            fNewFileTU = null;
	            IPath folderPath = getSourceFolderFullPath();
	            if(folderPath!=null){
//	            	IProject project=getCurrentProject();
	            	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	            	IWorkspaceRoot root = workspace.getRoot();

	            	String suitename=fNewFileDialogField.getText();
	            	
	            	if(folderPath.segmentCount()==1){
	            		IProject folder=root.getProject(folderPath.toPortableString());
	            		CuteSuiteWizardHandler.copyFile(folder, monitor, "$suitename$.cpp", suitename+".cpp", suitename);		
		            	CuteSuiteWizardHandler.copyFile(folder, monitor, "$suitename$.h", suitename+".h", suitename);
		            	IFile cppFile=folder.getFile(suitename+".cpp");
		            	if(cppFile!=null)fNewFileTU =CoreModelUtil.findTranslationUnit(cppFile);
	            	}else{
	            		IFolder folder=root.getFolder(folderPath);	
	            		CuteSuiteWizardHandler.copyFile(folder, monitor, "$suitename$.cpp", suitename+".cpp", suitename);		
		            	CuteSuiteWizardHandler.copyFile(folder, monitor, "$suitename$.h", suitename+".h", suitename);
		            	IFile cppFile=folder.getFile(suitename+".cpp");
		            	if(cppFile!=null)fNewFileTU =CoreModelUtil.findTranslationUnit(cppFile);
	            	}
	            	
	            	/*
	            	IFile cppFile=folder.getFile(suitename+".cpp");
	            	if(cppFile!=null){// && fSelection.isSelected()){
	            		fNewFileTU =CoreModelUtil.findTranslationUnit(cppFile);
	            		
	            		IIndex index = CCorePlugin.getIndexManager().getIndex(fNewFileTU.getCProject());
	            		
	            		IProgressMonitor sub = new SubProgressMonitor(monitor,1);

	            		IIndexBinding[] bindings= index.findBindings("runner".toCharArray(),IndexFilter.ALL,sub);
	            		sub.done();
//	            		IName name=
//	            		IIndexBinding binding=index.findBinding(name);
//	            		
	            		
	            		NewSuiteFileGenerator nsfg=new NewSuiteFileGenerator(cppFile);
		            	nsfg.parse();	
	            	}*/
	            }
	        } finally {
	            monitor.done();
	        }
        }
	}
	
	
	
	@Override
	protected void createFileControls(Composite parent, int nColumns) {
		fNewFileDialogField.doFillIntoGrid(parent, nColumns);
		Text textControl = fNewFileDialogField.getTextControl(null);
		LayoutUtil.setWidthHint(textControl, getMaxFieldWidth());
		textControl.addFocusListener(new StatusFocusListener(NEW_FILE_ID));
		/*
		Composite p=new Composite(parent,SWT.NO_FOCUS);
		GridData gd= new GridData(GridData.BEGINNING);
		gd.horizontalSpan= 1;
		p.setLayoutData(gd);*/
		createSeparator(parent,nColumns);
		fSelection.doFillIntoGrid(parent, nColumns);
		
	}

	@Override
	protected IStatus fileNameChanged() {
		StatusInfo status = new StatusInfo();
		
		IPath filePath = getFileFullPath();
		if (filePath == null) {
			status.setError("Enter Suite Name"); 
			return status;
		}
		
		IPath sourceFolderPath = getSourceFolderFullPath();
		if (sourceFolderPath == null || !sourceFolderPath.isPrefixOf(filePath)) {
			status.setError(NewFileWizardMessages.getString("NewSourceFileCreationWizardPage.error.FileNotInSourceFolder")); //$NON-NLS-1$
			return status;
		}
		
		// check if file already exists
		IResource file = getWorkspaceRoot().findMember(filePath);
		if (file != null && file.exists()) {
	    	if (file.getType() == IResource.FILE) {
	    		status.setError(NewFileWizardMessages.getString("NewSourceFileCreationWizardPage.error.FileExists")); //$NON-NLS-1$
	    	} else if (file.getType() == IResource.FOLDER) {
	    		status.setError(NewFileWizardMessages.getString("NewSourceFileCreationWizardPage.error.MatchingFolderExists")); //$NON-NLS-1$
	    	} else {
	    		status.setError(NewFileWizardMessages.getString("NewSourceFileCreationWizardPage.error.MatchingResourceExists")); //$NON-NLS-1$
	    	}
			return status;
		}
		
		// check if folder exists
		IPath folderPath = filePath.removeLastSegments(1).makeRelative();
		IResource folder = getWorkspaceRoot().findMember(folderPath);
		if (folder == null || !folder.exists() || (folder.getType() != IResource.PROJECT && folder.getType() != IResource.FOLDER)) {
		    status.setError(NewFileWizardMessages.getFormattedString("NewSourceFileCreationWizardPage.error.FolderDoesNotExist", folderPath)); //$NON-NLS-1$
			return status;
		}

		IStatus convStatus = CConventions.validateSourceFileName(getCurrentProject(), filePath.lastSegment());
		if (convStatus.getSeverity() == IStatus.ERROR) {
			status.setError(NewFileWizardMessages.getFormattedString("NewSourceFileCreationWizardPage.error.InvalidFileName", convStatus.getMessage())); //$NON-NLS-1$
			return status;
		} /*else if (convStatus.getSeverity() == IStatus.WARNING) {
			status.setWarning(NewFileWizardMessages.getFormattedString("NewSourceFileCreationWizardPage.warning.FileNameDiscouraged", convStatus.getMessage())); //$NON-NLS-1$
		}*/
		if(!fNewFileDialogField.getText().matches("^\\w+$")){
			status.setError("Invalid identifier. Only letters, digits and underscore are accepted."); //$NON-NLS-1$
			return status;
		}
		return status;
	}

	@Override
	public ITranslationUnit getCreatedFileTU() {
		return fNewFileTU; //used to create an editor window to show to the user
	}

	@Override
	public IPath getFileFullPath() {
		String str = fNewFileDialogField.getText();
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

	@Override
	protected void setFocus() {
		fNewFileDialogField.setFocus();
	}

}
