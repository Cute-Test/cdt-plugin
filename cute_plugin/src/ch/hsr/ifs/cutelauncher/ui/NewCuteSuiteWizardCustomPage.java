package ch.hsr.ifs.cutelauncher.ui;

import org.eclipse.cdt.managedbuilder.ui.wizards.CDTConfigWizardPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
public class NewCuteSuiteWizardCustomPage extends MBSCustomPage {

	private final CDTConfigWizardPage configPage;
	private final IWizardPage startingWizardPage;
	
	public NewCuteSuiteWizardCustomPage(CDTConfigWizardPage configWizardPage, IWizardPage startingWizardPage){
		pageID="ch.hsr.ifs.cutelauncher.ui.NewCuteSuiteWizardCustomPage";
		this.configPage = configWizardPage;
		this.startingWizardPage = startingWizardPage;
	}

	@Override
	public IWizardPage getNextPage() {
		return configPage;
	}

	@Override
	public IWizardPage getPreviousPage() {
		return startingWizardPage;
	}
	
	@Override
	protected boolean isCustomPageComplete() {
		if(suitenameText.getText().equals("")){
			//since canFinish cannot be overwritten from this class, 
			//we will need to accept a default name, which is better design 
			errmsg="Please enter a suite name.";
			return false;
		}
		errmsg=null;
		return true;
	}

	public String getName() {
		return "Set Suite Name";
	}
	
	private Composite composite=null;
	private Label suitenameLabel=null; 
	private Text suitenameText=null;
	
	public void createControl(Composite parent) {
		//descriptionLabel=new Label(n_comp,SWT.NONE);
	    //descriptionLabel.setText("This is for unmanaged project to set the source folder for Cute Plug-in to scan.");
		composite = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout(3, true);
		//layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 5;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		GridData gd;
		gd = new GridData();
		gd.horizontalSpan =1;
		suitenameLabel=new Label(composite,SWT.NONE);
		suitenameLabel.setText("Test Suite Name:");
		suitenameLabel.setLayoutData(gd);
		
	    gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan =2;
		suitenameText = SWTFactory.createSingleText(composite, 2);
		suitenameText.setLayoutData(gd); 
		suitenameText.setText("suite");
	    suitenameText.addModifyListener(new ModifyListener() {
	    	public void modifyText(ModifyEvent e){
			IWizardContainer iwc=getWizard().getContainer();
			//have to call one by one as 
			//org.eclipse.jface.wizard.Wizard.Dialog.update() is protected 
			iwc.updateButtons();
			iwc.updateMessage();
			iwc.updateTitleBar();
			iwc.updateWindowTitle();}
		});	
	    //no access to canFinish() so have to use default values instead 
	}

	public String getSuiteName(){
		if(suitenameText.getText().equals(""))return "suite";
		return suitenameText.getText();
	}
	public void dispose() {
		composite.dispose();
	}

	public Control getControl() {
		return composite==null?null:composite;
	}

	public String getDescription() {
		return "for the user to specify a custom suite name.";
	}

	String errmsg=null;
	public String getErrorMessage() {
		return errmsg;
	}

	public Image getImage() {
		return wizard.getDefaultPageImage();
	}

	public String getMessage() {
		return "New Test Suite Name";
	}

	public String getTitle() {
		return "Suite Name";
	}

	public void performHelp() {
	}

	public void setDescription(String description) {
	}

	public void setImageDescriptor(ImageDescriptor image) {
	}

	public void setTitle(String title) {
	}

	public void setVisible(boolean visible) {
		composite.setVisible(visible);
	}

}