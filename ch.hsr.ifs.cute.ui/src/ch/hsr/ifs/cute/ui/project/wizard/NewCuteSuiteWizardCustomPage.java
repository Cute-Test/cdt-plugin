/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.ui.wizards.CDTConfigWizardPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.hsr.ifs.cute.ui.UiPlugin;

public class NewCuteSuiteWizardCustomPage extends MBSCustomPage {

	private final CDTConfigWizardPage configPage;
	private final IWizardPage startingWizardPage;
	private CuteVersionComposite cuteVersionComp;
	private final ImageDescriptor imageDesc;
	/**
	 * @since 4.0
	 */
	protected boolean enableGcov;
	private final CuteSuiteWizardHandler handler;

	/**
	 * @since 4.0
	 */
	public NewCuteSuiteWizardCustomPage(CDTConfigWizardPage configWizardPage, IWizardPage startingWizardPage, CuteSuiteWizardHandler cuteSuiteWizardHandler) {
		pageID = "ch.hsr.ifs.cutelauncher.ui.NewCuteSuiteWizardCustomPage"; //$NON-NLS-1$
		this.configPage = configWizardPage;
		this.startingWizardPage = startingWizardPage;
		this.handler = cuteSuiteWizardHandler;
		imageDesc = UiPlugin.getImageDescriptor("cute_logo.png"); //$NON-NLS-1$
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
		if (suitenameText == null && cuteVersionComp == null)
			return false;
		if (suitenameText.getText().isEmpty()) { //$NON-NLS-1$
			//since IWizard#canFinish() cannot be overwritten from this class, thus unable to disable the finish button, 
			//we will need to use a default name for empty textfield as a work around
			errmsg = Messages.getString("NewCuteSuiteWizardCustomPage.EnterSuiteName"); //$NON-NLS-1$
			return false;
		}
		if (!suitenameText.getText().matches("\\w+")) { //$NON-NLS-1$
			errmsg = Messages.getString("NewCuteSuiteWizardCustomPage.InvalidSuiteName"); //$NON-NLS-1$
			return false;
		}
		if (!cuteVersionComp.isComplete()) {
			errmsg = cuteVersionComp.getErrorMessage();
			return false;
		}
		errmsg = null;
		return true;
	}

	public String getName() {
		return Messages.getString("NewCuteSuiteWizardCustomPage.SetSuiteName"); //$NON-NLS-1$
	}

	private Composite composite = null;
	private Label suitenameLabel = null;
	private Text suitenameText = null;

	public void createControl(Composite parent) {
		createControl(parent, true);
	}

	public void createControl(Composite parent, final boolean flag) {

		composite = new Composite(parent, SWT.NULL);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = true;
		layout.marginHeight = 0;
		layout.marginWidth = 5;
		composite.setLayout(layout);

		GridData gd;

		gd = new GridData();
		gd.horizontalAlignment = SWT.BEGINNING | SWT.FILL;
		gd.horizontalSpan = 3;
		gd.grabExcessHorizontalSpace = true;

		Composite tempComposite = new Composite(composite, SWT.NULL);
		GridLayout tempLayout = new GridLayout();
		tempLayout.marginTop = 0;
		tempLayout.marginWidth = 0;
		tempLayout.marginBottom = 5;
		tempComposite.setLayout(tempLayout);
		tempComposite.setLayoutData(gd);
		cuteVersionComp = new CuteVersionComposite(tempComposite);
		cuteVersionComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		IToolChain[] tcs = handler.getSelectedToolChains();
		if (tcs[0].getBaseId().contains("gnu")) { //$NON-NLS-1$

			final Button check = new Button(tempComposite, SWT.CHECK);
			check.setText(Messages.getString("NewCuteSuiteWizardCustomPage.enableGcov")); //$NON-NLS-1$
			check.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					enableGcov = check.getSelection();
				}

			});
		}

		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.verticalIndent = 20;
		suitenameLabel = new Label(composite, SWT.NONE);
		suitenameLabel.setText(Messages.getString("NewCuteSuiteWizardCustomPage.TestSuiteName")); //$NON-NLS-1$
		suitenameLabel.setLayoutData(gd);

		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalIndent = 20;
		suitenameText = createSingleText(composite, 2);
		suitenameText.setLayoutData(gd);
		suitenameText.setText(Messages.getString("NewCuteSuiteWizardCustomPage.DefaultSuiteName")); //$NON-NLS-1$
		suitenameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (flag) {
					IWizardContainer iwc = getWizard().getContainer();
					//have to call one by one as 
					//org.eclipse.jface.wizard.Wizard.Dialog.update() is protected 
					iwc.updateButtons();
					iwc.updateMessage();
					iwc.updateTitleBar();
					iwc.updateWindowTitle();
				}
			}
		});
	}

	private Text createSingleText(Composite parent, int hspan) {
		Text t = new Text(parent, SWT.SINGLE | SWT.BORDER);
		t.setFont(parent.getFont());
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = hspan;
		t.setLayoutData(gd);
		return t;
	}

	public String getSuiteName() {
		if (suitenameText == null || suitenameText.getText().isEmpty() || !suitenameText.getText().matches("\\w+"))return Messages.getString("NewCuteSuiteWizardCustomPage.suite"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return suitenameText.getText();
	}

	/**
	 * @since 4.0
	 */
	public String getCuteVersion() {
		return cuteVersionComp.getVersionString();
	}

	//for unit testing
	public void setSuiteName(String s) {
		suitenameText.setText(s);
	}

	public void dispose() {
		composite.dispose();
	}

	public Control getControl() {
		return composite == null ? null : composite;
	}

	public String getDescription() {
		return Messages.getString("NewCuteSuiteWizardCustomPage.CustomSuiteName"); //$NON-NLS-1$
	}

	String errmsg = null;

	public String getErrorMessage() {
		return errmsg;
	}

	public Image getImage() {
		return imageDesc.createImage();
	}

	public String getMessage() {
		return Messages.getString("NewCuteSuiteWizardCustomPage.NewTestSuiteName"); //$NON-NLS-1$
	}

	public String getTitle() {
		return Messages.getString("NewCuteSuiteWizardCustomPage.SuiteName"); //$NON-NLS-1$
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