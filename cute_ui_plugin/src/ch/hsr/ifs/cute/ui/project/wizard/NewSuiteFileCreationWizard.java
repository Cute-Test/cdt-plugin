package ch.hsr.ifs.cute.ui.project.wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.internal.ui.CPluginImages;
import org.eclipse.cdt.internal.ui.actions.WorkbenchRunnableAdapter;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
//based on @see org.eclipse.cdt.ui/org.eclipse.cdt.internal.ui.wizards.filewizard.NewSourceFileCreationWizard
@SuppressWarnings("restriction")
public class NewSuiteFileCreationWizard extends Wizard implements INewWizard {
	
	private NewSuiteFileCreationWizardPage page = null;
	private IStructuredSelection selection;
	
    public NewSuiteFileCreationWizard() {
        super();
        setDefaultPageImageDescriptor(CPluginImages.DESC_WIZBAN_NEW_SOURCEFILE);
        setDialogSettings(CUIPlugin.getDefault().getDialogSettings());
        setWindowTitle("New Cute Suite File"); //$NON-NLS-1$
    }
    
    /*
     * @see Wizard#createPages
     */
    @Override
	public void addPages() {
        super.addPages();
        page = new NewSuiteFileCreationWizardPage();
        addPage(page);
        page.init(getSelection());
    }

    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
		selection= currentSelection;
	}
	
	private IStructuredSelection getSelection() {
		return selection;
	}
	
	protected ISchedulingRule getSchedulingRule() {
		return ResourcesPlugin.getWorkspace().getRoot(); // look all by default
	}

    @Override
	public boolean performFinish() {
    	IWorkspaceRunnable op= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
					page.createFile(monitor);
			}
		};
		try {
			getContainer().run(true, true, new WorkbenchRunnableAdapter(op, getSchedulingRule()));
		} catch (InvocationTargetException e) {
			return false;
		} catch  (InterruptedException e) {
			return false;
		}

//            //TODO need prefs option for opening editor
//            boolean openInEditor = true;
//            
//			ITranslationUnit headerTU = fPage.getCreatedFileTU();
//			if (headerTU != null) {
//				IResource resource= headerTU.getResource();
//				selectAndReveal(resource);
//				if (openInEditor) {
//					openResource((IFile) resource);
//				}
//			}
        return true;
    }
}
