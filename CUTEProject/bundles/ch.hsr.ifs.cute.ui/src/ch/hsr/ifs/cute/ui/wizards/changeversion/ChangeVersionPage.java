/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.wizards.changeversion;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cute.headers.ICuteHeaders;
import ch.hsr.ifs.cute.ui.CuteUIPlugin;
import ch.hsr.ifs.cute.ui.wizards.VersionSelectionComposite;


/**
 * @author egraf
 * @since 4.0
 *
 */
public class ChangeVersionPage extends WizardPage {

    private Composite                 composite;
    private VersionSelectionComposite cuteVersionComp;
    private final ImageDescriptor     imageDesc;
    private final ICuteHeaders        currentCuteHeaders;

    protected ChangeVersionPage(ICuteHeaders currentCuteHeaders) {
        super("changeCuteVersionPage");
        this.currentCuteHeaders = currentCuteHeaders;
        imageDesc = CuteUIPlugin.getImageDescriptor("cute_logo.png");
    }

    @Override
    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        setTitle(ChangeVersionMessages.Title);

        cuteVersionComp = new VersionSelectionComposite(composite, currentCuteHeaders);
        setControl(composite);
    }

    @Override
    public Image getImage() {
        return imageDesc.createImage();
    }

    public String getVersionString() {
        return cuteVersionComp.getVersionString();
    }
}
