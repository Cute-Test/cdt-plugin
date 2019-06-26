/*******************************************************************************
 * Copyright (c) 2007-2015, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.wizards.newproject.newsuiteproject;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.hsr.ifs.cute.ui.wizards.newproject.NewProjectWizardPage;


public class NewSuiteProjectWizardPage extends NewProjectWizardPage {

    private static final String DEFAULT_SUITE_NAME = "suite";

    private String errmsg;
    private Text   suitenameText;

    public NewSuiteProjectWizardPage(IWizardPage nextPage, IWizardPage previousPage, IWizardContainer wc) {
        super(nextPage, previousPage, "ch.hsr.ifs.cutelauncher.ui.NewCuteSuiteWizardCustomPage", wc);
    }

    @Override
    protected boolean isCustomPageComplete() {
        if (suitenameText == null) {
            errmsg = NewSuiteProjectMessages.EnterSuiteName;
            return false;
        }
        String suiteName = suitenameText.getText();
        if (suiteName.isEmpty()) {
            errmsg = NewSuiteProjectMessages.EnterSuiteName;
            return false;
        }
        if (suiteName.toLowerCase().equals("test")) {
            errmsg = NewSuiteProjectMessages.MustNotBeTest;
            return false;
        }
        if (!suiteName.matches("\\w+")) {
            errmsg = NewSuiteProjectMessages.InvalidSuiteName;
            return false;
        }
        errmsg = null;
        return super.isCustomPageComplete();
    }

    @Override
    public String getName() {
        return NewSuiteProjectMessages.SetSuiteName;
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        addSuiteNamePart();
    }

    private void addSuiteNamePart() {
        Label suitenameLabel = new Label(composite, SWT.NONE);
        suitenameLabel.setText(NewSuiteProjectMessages.TestSuiteName);
        suitenameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        suitenameText.setFont(composite.getFont());
        suitenameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        suitenameText.moveAbove(composite.getChildren()[0]);
        suitenameLabel.moveAbove(suitenameText);
        addSuiteNameListener();
    }

    private void addSuiteNameListener() {
        suitenameText.addModifyListener(e -> {
            IWizardContainer iwc = getWizard().getContainer();
            // have to call one by one as
            // org.eclipse.jface.wizard.Wizard.Dialog.update() is protected
            iwc.updateButtons();
            iwc.updateMessage();
            iwc.updateTitleBar();
            iwc.updateWindowTitle();
        });
    }

    public String getSuiteName() {
        return isCustomPageComplete() ? suitenameText.getText() : DEFAULT_SUITE_NAME;
    }

    @Override
    public String getDescription() {
        return NewSuiteProjectMessages.CustomSuiteName;
    }

    @Override
    public String getErrorMessage() {
        return errmsg != null ? errmsg : super.getErrorMessage();
    }

    @Override
    public String getMessage() {
        return NewSuiteProjectMessages.NewTestSuiteName;
    }

    @Override
    public String getTitle() {
        return NewSuiteProjectMessages.SuiteName;
    }
}
