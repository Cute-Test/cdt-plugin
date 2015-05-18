/*******************************************************************************
 * Copyright (c) 2007-2015, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewCuteSuiteProjectWizardPage extends NewCuteProjectWizardPage {

	private String errmsg;
	private Text suitenameText;

	public NewCuteSuiteProjectWizardPage(IWizardPage nextPage, IWizardPage previousPage) {
		super(nextPage, previousPage, "ch.hsr.ifs.cutelauncher.ui.NewCuteSuiteWizardCustomPage");
	}

	@Override
	protected boolean isCustomPageComplete() {
		if (suitenameText == null) {
			errmsg = Messages.getString("NewCuteSuiteWizardCustomPage.EnterSuiteName");
			return false;
		}
		String suiteName = suitenameText.getText();
		if (suiteName.isEmpty()) {
			errmsg = String.format(Messages.getString("NewCuteSuiteWizardCustomPage.EnterSuiteName"), suiteName);
			return false;
		}
		if (suiteName.toLowerCase().equals("test")) {
			errmsg = Messages.getString("NewCuteSuiteWizardCustomPage.MustNotBeTest");
			return false;
		}
		if (!suiteName.matches("\\w+")) {
			errmsg = Messages.getString("NewCuteSuiteWizardCustomPage.InvalidSuiteName");
			return false;
		}
		errmsg = null;
		return super.isCustomPageComplete();
	}

	@Override
	public String getName() {
		return Messages.getString("NewCuteSuiteWizardCustomPage.SetSuiteName");
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		addSuiteNamePart();
	}

	private void addSuiteNamePart() {
		Label suitenameLabel = new Label(composite, SWT.NONE);
		suitenameLabel.setText(Messages.getString("NewCuteSuiteWizardCustomPage.TestSuiteName"));

		suitenameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		suitenameText.setFont(composite.getFont());
		suitenameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addSuiteNameListener();
	}

	private void addSuiteNameListener() {
		suitenameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				IWizardContainer iwc = getWizard().getContainer();
				// have to call one by one as
				// org.eclipse.jface.wizard.Wizard.Dialog.update() is protected
				iwc.updateButtons();
				iwc.updateMessage();
				iwc.updateTitleBar();
				iwc.updateWindowTitle();
			}
		});
	}

	public String getSuiteName() {
		return isCustomPageComplete() ? suitenameText.getText() : Messages.getString("NewCuteSuiteWizardCustomPage.suite");
	}

	@Override
	public String getDescription() {
		return Messages.getString("NewCuteSuiteWizardCustomPage.CustomSuiteName");
	}

	@Override
	public String getErrorMessage() {
		return errmsg != null ? errmsg : super.getErrorMessage();
	}

	@Override
	public String getMessage() {
		return Messages.getString("NewCuteSuiteWizardCustomPage.NewTestSuiteName");
	}

	@Override
	public String getTitle() {
		return Messages.getString("NewCuteSuiteWizardCustomPage.SuiteName");
	}
}
