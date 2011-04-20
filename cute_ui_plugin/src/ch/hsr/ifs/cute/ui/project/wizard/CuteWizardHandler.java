/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import ch.hsr.ifs.cute.ui.GetOptionsStrategy;
import ch.hsr.ifs.cute.ui.ICuteWizardAddition;
import ch.hsr.ifs.cute.ui.IIncludeStrategyProvider;
import ch.hsr.ifs.cute.ui.IncludePathStrategy;
import ch.hsr.ifs.cute.ui.ProjectTools;
import ch.hsr.ifs.cute.ui.UiPlugin;
import ch.hsr.ifs.cute.ui.project.CuteNature;
import ch.hsr.ifs.cute.ui.project.headers.ICuteHeaders;

/**
 * @author Emanuel Graf
 *
 */
public class CuteWizardHandler extends MBSWizardHandler implements IIncludeStrategyProvider {

	private final CuteVersionWizardPage cuteVersionWizardPage;

	@Override
	public IWizardPage getSpecificPage() {
		return cuteVersionWizardPage;
	}

	public CuteWizardHandler(Composite p, IWizard w) {
		super(new CuteBuildPropertyValue(), p, w);
		cuteVersionWizardPage = new CuteVersionWizardPage( getConfigPage(), getStartingPage());
		cuteVersionWizardPage.setWizard(w);

		MBSCustomPageManager.init();
		MBSCustomPageManager.addStockPage(cuteVersionWizardPage, cuteVersionWizardPage.getPageID());
	}

	@Override
	public boolean canFinish() {
		return cuteVersionWizardPage != null ? cuteVersionWizardPage.isCustomPageComplete() : false;
	}

	@Override
	public void postProcess(IProject newProject, boolean created) {
		if (created) {
			doTemplatesPostProcess(newProject);
			doCustom(newProject);
		}
	}

	@Override
	protected void doCustom(final IProject newProject) {
		super.doCustom(newProject);
		IRunnableWithProgress op = new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				createCuteProjectSettings(newProject, monitor);
			}
		};
		try {
			getWizard().getContainer().run(false, true, op);
		} catch (InvocationTargetException e) {
			UiPlugin.log(e);
		} catch (InterruptedException e) {
			UiPlugin.log(e);
		}

	}

	/**
	 * @since 4.0
	 */
	protected void createCuteProjectSettings(IProject newProject, IProgressMonitor monitor) {
		try {
			createCuteProject(newProject, monitor);
		} catch (CoreException e) {
			UiPlugin.log(e);
		}
	}


	/**
	 * @since 4.0
	 */
	protected void createCuteProject(IProject project, IProgressMonitor pm) throws CoreException {
		CuteNature.addCuteNature(project, new NullProgressMonitor());
		QualifiedName key = new QualifiedName(UiPlugin.PLUGIN_ID, UiPlugin.CUTE_VERSION_PROPERTY_NAME);
		project.setPersistentProperty(key, getCuteVersion().getVersionString());
		createCuteProjectFolders(project);
		callAdditionalHandlers(project, pm);
		ManagedBuildManager.saveBuildInfo(project, true);

	}

	private void callAdditionalHandlers(IProject project, IProgressMonitor pm) throws CoreException {
		List<ICuteWizardAddition> adds = getAdditions();
		SubMonitor mon = SubMonitor.convert(pm, adds.size());
		for (ICuteWizardAddition addition : adds) {
			addition.getHandler().configureProject(project, mon);
			mon.worked(1);
		}
		mon.done();

	}

	/**
	 * @since 4.0
	 */
	protected List<ICuteWizardAddition> getAdditions() {
		return cuteVersionWizardPage.getAdditions();
	}


	protected void createCuteProjectFolders(IProject project)
			throws CoreException {
		IFolder srcFolder = ProjectTools.createFolder(project, "src", true); //$NON-NLS-1$
		ICuteHeaders cuteVersion = getCuteVersion();


		IFolder cuteFolder = ProjectTools.createFolder(project, "cute", true); //$NON-NLS-1$


		copyFiles(srcFolder, cuteVersion, cuteFolder);

		ProjectTools.setIncludePaths(cuteFolder.getFullPath(), project, this);
		IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				getTestMainFile(project), true);
	}

	/**
	 * @since 4.0
	 */
	protected ICuteHeaders getCuteVersion() {
		return UiPlugin.getCuteVersion(cuteVersionWizardPage.getCuteVersionString());
	}

	/**
	 * @since 4.0
	 */
	public void copyFiles(IFolder srcFolder, ICuteHeaders cuteVersion,
			IFolder cuteFolder) throws CoreException {
		cuteVersion.copyTestFiles(srcFolder, new NullProgressMonitor());
		cuteVersion.copyHeaderFiles(cuteFolder, new NullProgressMonitor());
	}

	protected IFile getTestMainFile(IProject project) {
		return project.getFile("src/Test.cpp"); //$NON-NLS-1$
	}

	/**
	 * @since 4.0
	 */
	public GetOptionsStrategy getStrategy(int optionType) {
		switch (optionType) {
		case IOption.INCLUDE_PATH:
			return new IncludePathStrategy();

		default:
			throw new IllegalArgumentException("Illegal Argument: "+optionType); //$NON-NLS-1$
		}
	}


}
