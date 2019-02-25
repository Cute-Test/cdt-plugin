/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.wizards.changeversion;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.Wizard;

import ch.hsr.ifs.cute.headers.ICuteHeaders;
import ch.hsr.ifs.cute.headers.manager.CuteHeadersManager;
import ch.hsr.ifs.cute.ui.CuteUIPlugin;


/**
 * @author egraf
 * @since 4.0
 *
 */
public class ChangeVersionWizard extends Wizard {

    private final IProject    project;
    private ChangeVersionPage page;
    private ICuteHeaders      activeHeaders;

    public ChangeVersionWizard(IProject project) {
        this.project = project;
        try {
            activeHeaders = ICuteHeaders.getForProject(project);
        } catch (CoreException e) {
            e.printStackTrace();
            //activeHeadersVersionName remains null
        }
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        page = new ChangeVersionPage(activeHeaders);
        addPage(page);
    }

    @Override
    public boolean performFinish() {
        try {
            ICuteHeaders selectedHeaders = getHeaders(page.getVersionString());
            if (selectedHeaders != activeHeaders) replaceHeaders(selectedHeaders);
        } catch (InvocationTargetException e) {
            CuteUIPlugin.log("Exception while performing version change", e);
        } catch (InterruptedException e) {
            CuteUIPlugin.log("Exception while performing version change", e);
        }
        return true;
    }

    private void replaceHeaders(final ICuteHeaders newHeaders) throws InvocationTargetException, InterruptedException {
        this.getContainer().run(true, false, monitor -> {
            CuteHeadersManager.replaceCuteHeaders(project, newHeaders, monitor);
        });
    }

    protected ICuteHeaders getHeaders(String cuteVersionString) {
        for (ICuteHeaders cuteHeaders : ICuteHeaders.loadedHeaders()) {
            if (cuteVersionString.equals(cuteHeaders.getVersionString())) return cuteHeaders;
        }
        return null;
    }

}
