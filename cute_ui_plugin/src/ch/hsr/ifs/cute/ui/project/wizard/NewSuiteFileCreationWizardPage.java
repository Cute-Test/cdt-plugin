package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.cdt.internal.ui.wizards.filewizard.AbstractFileCreationWizardPage;
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
import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import ch.hsr.ifs.cute.ui.SuiteTemplateCopyUtil;


@SuppressWarnings("restriction")
public class NewSuiteFileCreationWizardPage extends
		AbstractFileCreationWizardPage {

	private ITranslationUnit fNewFileTU = null;
	private final StringDialogField fNewFileDialogField;
	private final SelectionButtonDialogField fSelection;
	private String lastUsedTemplate = ""; //$NON-NLS-1$
	
	@SuppressWarnings("restriction")
	public NewSuiteFileCreationWizardPage(){
		super(Messages.getString("NewSuiteFileCreationWizardPage.CustomSuite")); //$NON-NLS-1$
		
		setDescription(Messages.getString("NewSuiteFileCreationWizardPage.CreateNewSuite")); //$NON-NLS-1$
		
		fNewFileDialogField = new StringDialogField();
		fNewFileDialogField.setDialogFieldListener(new IDialogFieldListener() {
			public void dialogFieldChanged(DialogField field) {
				handleFieldChanged(NEW_FILE_ID);
			}
		});
		fNewFileDialogField.setLabelText(Messages.getString("NewSuiteFileCreationWizardPage.SuiteName")); //$NON-NLS-1$
		
		fSelection=new SelectionButtonDialogField(SWT.CHECK);
		fSelection.setLabelText(Messages.getString("NewSuiteFileCreationWizardPage.LinkToRunner")); //$NON-NLS-1$
		//generate list of runners
		//prompt selection
	}
	@SuppressWarnings("restriction")
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
	            	
	            	IFile cppFile;
	            	if(folderPath.segmentCount()==1){
	            		IProject folder=root.getProject(folderPath.toPortableString());
	            		SuiteTemplateCopyUtil.copyFile(folder, monitor, "$suitename$.cpp", suitename+".cpp", suitename);		 //$NON-NLS-1$ //$NON-NLS-2$
	            		SuiteTemplateCopyUtil.copyFile(folder, monitor, "$suitename$.h", suitename+".h", suitename); //$NON-NLS-1$ //$NON-NLS-2$
		            	cppFile=folder.getFile(suitename+".cpp"); //$NON-NLS-1$
		            	if(cppFile!=null)fNewFileTU =CoreModelUtil.findTranslationUnit(cppFile);
	            	}else{
	            		IFolder folder=root.getFolder(folderPath);	
	            		SuiteTemplateCopyUtil.copyFile(folder, monitor, "$suitename$.cpp", suitename+".cpp", suitename);		 //$NON-NLS-1$ //$NON-NLS-2$
	            		SuiteTemplateCopyUtil.copyFile(folder, monitor, "$suitename$.h", suitename+".h", suitename); //$NON-NLS-1$ //$NON-NLS-2$
		            	cppFile=folder.getFile(suitename+".cpp"); //$NON-NLS-1$
		            	if(cppFile!=null)fNewFileTU =CoreModelUtil.findTranslationUnit(cppFile);
	            	}
	            	
	            }
	        } finally {
	            monitor.done();
	        }
        }
	}
	
	public static void waitUntilFileIsIndexed(IIndex index, IFile file, int maxmillis,IProgressMonitor p) throws Exception {
		long endTime= System.currentTimeMillis() + maxmillis;
		int timeLeft= maxmillis;
		while (timeLeft >= 0) {
			index.acquireReadLock();
			try {
				IIndexFile pfile= index.getFile(ILinkage.CPP_LINKAGE_ID, IndexLocationFactory.getWorkspaceIFL(file));
				if (pfile != null && pfile.getTimestamp() >= file.getLocalTimeStamp()) {
					return;
				}
			}
			finally {
				index.releaseReadLock();
			}
			
			Thread.sleep(50);
			timeLeft= (int) (endTime-System.currentTimeMillis());
		}
	}
	
	
	@SuppressWarnings("restriction")
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

	@SuppressWarnings("restriction")
	@Override
	protected IStatus fileNameChanged() {
		StatusInfo status = new StatusInfo();
		
		IPath filePath = getFileFullPath();
		if (filePath == null) {
			status.setError(Messages.getString("NewSuiteFileCreationWizardPage.EnterSuiteName"));  //$NON-NLS-1$
			return status;
		}
		
		IPath sourceFolderPath = getSourceFolderFullPath();
		if (sourceFolderPath == null || !sourceFolderPath.isPrefixOf(filePath)) {
			status.setError(Messages.getString("NewSuiteFileCreationWizardPage.FileMustBeInsideSourceFolder")); //$NON-NLS-1$
			return status;
		}
		
		// check if file already exists
		IResource file = getWorkspaceRoot().findMember(filePath);
		if (file != null && file.exists()) {
	    	if (file.getType() == IResource.FILE) {
	    		status.setError(Messages.getString("NewSuiteFileCreationWizardPage.FileAlreadyExist")); //$NON-NLS-1$
	    	} else if (file.getType() == IResource.FOLDER) {
	    		status.setError(Messages.getString("NewSuiteFileCreationWizardPage.FolderAlreadyExists")); //$NON-NLS-1$
	    	} else {
	    		status.setError(Messages.getString("NewSuiteFileCreationWizardPage.ResourceAlreadyExists")); //$NON-NLS-1$
	    	}
			return status;
		}
		
		// check if folder exists
		IPath folderPath = filePath.removeLastSegments(1).makeRelative();
		IResource folder = getWorkspaceRoot().findMember(folderPath);
		if (folder == null || !folder.exists() || (folder.getType() != IResource.PROJECT && folder.getType() != IResource.FOLDER)) {
		    status.setError(Messages.getString("NewSuiteFileCreationWizardPage.Folder") + folderPath + Messages.getString("NewSuiteFileCreationWizardPage.DoesNotExist") ); //$NON-NLS-1$ //$NON-NLS-2$
			return status;
		}

		IStatus convStatus = CConventions.validateSourceFileName(getCurrentProject(), filePath.lastSegment());
		if (convStatus.getSeverity() == IStatus.ERROR) {
			status.setError(Messages.getString("NewSuiteFileCreationWizardPage.0") + convStatus.getMessage() + "."); //$NON-NLS-1$ //$NON-NLS-2$
			return status;
		} /*else if (convStatus.getSeverity() == IStatus.WARNING) {
			status.setWarning(NewFileWizardMessages.getFormattedString("NewSourceFileCreationWizardPage.warning.FileNameDiscouraged", convStatus.getMessage())); //$NON-NLS-1$
		}*/
		if(!fNewFileDialogField.getText().matches("\\w+")){ //$NON-NLS-1$
			status.setError("Invalid identifier. Only letters, digits and underscore are accepted."); //$NON-NLS-1$
			return status;
		}
		return status;
	}

	@Override
	public ITranslationUnit getCreatedFileTU() {
		return fNewFileTU; //used to create an editor window to show to the user
	}

	@SuppressWarnings("restriction")
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

	@SuppressWarnings("restriction")
	@Override
	protected void setFocus() {
		fNewFileDialogField.setFocus();
	}
	@SuppressWarnings("restriction")
	@Override
	protected Template[] getApplicableTemplates() {
		return StubUtility.getFileTemplatesForContentTypes(
				new String[] { CCorePlugin.CONTENT_TYPE_CXXHEADER, CCorePlugin.CONTENT_TYPE_CHEADER, CCorePlugin.CONTENT_TYPE_CXXSOURCE }, null);
	}
	
	@Override
	public String getLastUsedTemplateName() {
		return lastUsedTemplate;
	}
	@Override
	public void saveLastUsedTemplateName(String name) {
		lastUsedTemplate = name;
	}

}
