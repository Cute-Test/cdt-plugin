/*******************************************************************************
 * Copyright (c) 2009, 2012 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *     Alex Ruiz (Google)
 *     Sergey Prigogin (Google) 
 *******************************************************************************/
package ch.hsr.ifs.cute.charwars.ui;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.ICheckersRegistry;
import org.eclipse.cdt.codan.core.model.ICodanProblemMarker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.internal.core.CheckersRegistry;
import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.cdt.codan.internal.ui.preferences.FieldEditorOverlayPage;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

@SuppressWarnings("restriction")
public class CharWarsPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage {
	private IProblemProfile profile;
	private ProblemFieldEditor cstringProblem;
	private ProblemFieldEditor cstringAliasProblem;

	public CharWarsPreferencePage() {
		super(GRID);
		setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, CodanCorePlugin.PLUGIN_ID));
	}
	
    @Override
	public Point computeSize() {
    	System.out.println("compute size");
    	cstringProblem.load();
    	cstringAliasProblem.load();
        return super.computeSize();
    }

	@Override
	protected String getPageId() {
		return "ch.hsr.ifs.cute.charwars.ui.CharWarsPreferencePage";
	}

	@Override
	public void createFieldEditors() {
		cstringProblem = new ProblemFieldEditor(getFieldEditorParent(), profile, "ch.hsr.ifs.cute.charwars.problems.CStringProblem");
		addField(cstringProblem);
		cstringAliasProblem = new ProblemFieldEditor(getFieldEditorParent(), profile, "ch.hsr.ifs.cute.charwars.problems.CStringAliasProblem");
		addField(cstringAliasProblem);
	}

	@Override
	protected Control createContents(Composite parent) {
		if (isPropertyPage()) {
			profile = getRegistry().getResourceProfileWorkingCopy((IResource) getElement());
		} else {
			profile = getRegistry().getWorkspaceProfile();
		}
		Composite comp = (Composite) super.createContents(parent);
		return comp;
	}

	protected ICheckersRegistry getRegistry() {
		return CodanRuntime.getInstance().getCheckersRegistry();
	}

	@Override
	public boolean performOk() {
		System.out.println("performing ok");
		IResource resource = (IResource) getElement();
		getRegistry().updateProfile(resource, null);
		boolean success = super.performOk();
		if (success) {
			if (resource == null) {
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
			asynchronouslyUpdateMarkers(resource);
		}
		return success;
	}

	protected String getWidgetId() {
		return getPageId() + ".selection"; //$NON-NLS-1$
	}

	private static void asynchronouslyUpdateMarkers(final IResource resource) {
		final Set<IFile> filesToUpdate = new HashSet<IFile>();
		final IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow active = workbench.getActiveWorkbenchWindow();
		final IWorkbenchPage page = active.getActivePage();
		// Get the files open C/C++ editors.
//		for (IEditorReference partRef : page.getEditorReferences()) {
//			IEditorPart editor = partRef.getEditor(false);
//			if (editor instanceof ICEditor) {
//				IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
//				if (file != null && resource.getFullPath().isPrefixOf(file.getFullPath())) {
//					filesToUpdate.add(file);
//				}
//			}
//		}

		Job job = new Job(CodanUIMessages.CodanPreferencePage_Update_markers) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				final SubMonitor submonitor = SubMonitor.convert(monitor, 1 + 2 * filesToUpdate.size());
				removeMarkersForDisabledProblems(resource, submonitor.newChild(1));
				if (filesToUpdate.isEmpty())
					return Status.OK_STATUS;

				// Run checkers on the currently open files to update the problem markers.
//				for (final IFile file : filesToUpdate) {
//					ITranslationUnit tu = CoreModelUtil.findTranslationUnit(file);
//					if (tu != null) {
//						tu = CModelUtil.toWorkingCopy(tu);
//						ASTProvider.getASTProvider().runOnAST(
//								tu, ASTProvider.WAIT_ACTIVE_ONLY, submonitor.newChild(1),
//								new ASTRunnable() {
//									@Override
//									public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) {
//										if (ast != null) {
//											CodanRunner.runInEditor(ast, file, submonitor.newChild(1));
//										} else {
//											CodanRunner.processResource(file, CheckerLaunchMode.RUN_ON_FILE_OPEN,
//													submonitor.newChild(1));
//										}
//										return Status.OK_STATUS;
//									}
//								});
//					}
//				}
				return Status.OK_STATUS;
			}
		};
        IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		job.setRule(ruleFactory.markerRule(resource));
		job.setSystem(true);
		job.schedule();
	}

	private static void removeMarkersForDisabledProblems(IResource resource, IProgressMonitor monitor) {
		CheckersRegistry chegistry = CheckersRegistry.getInstance();
		Set<String> markerTypes = new HashSet<String>();
		for (IChecker checker : chegistry) {
			Collection<IProblem> problems = chegistry.getRefProblems(checker);
			for (IProblem problem : problems) {
				markerTypes.add(problem.getMarkerType());
			}
		}
		try {
			removeMarkersForDisabledProblems(chegistry, markerTypes, resource, monitor);
		} catch (CoreException e) {
			CodanUIActivator.log(e);
		}
	}

	private static void removeMarkersForDisabledProblems(CheckersRegistry chegistry,
			Set<String> markerTypes, IResource resource, IProgressMonitor monitor) throws CoreException {
		if (!resource.isAccessible()) {
			return;
		}
		IResource[] children = null;
		if (resource instanceof IContainer) {
			children = ((IContainer) resource).members();
		}
		int numChildren = children == null ? 0 : children.length;
		int childWeight = 10;
        SubMonitor progress = SubMonitor.convert(monitor, 1 + numChildren * childWeight);
		IProblemProfile resourceProfile = null;
		for (String markerType : markerTypes) {
			IMarker[] markers = resource.findMarkers(markerType, false, IResource.DEPTH_ZERO);
			for (IMarker marker : markers) {
				String problemId = (String) marker.getAttribute(ICodanProblemMarker.ID);
				if (resourceProfile == null) {
					resourceProfile = chegistry.getResourceProfile(resource);
				}
				IProblem problem = resourceProfile.findProblem(problemId);
				if (problem != null && !problem.isEnabled()) {
					marker.delete();
				}
			}
		}
		progress.worked(1);
		if (children != null) {
			for (IResource child : children) {
				if (monitor.isCanceled())
					return;
				removeMarkersForDisabledProblems(chegistry, markerTypes, child,
						progress.newChild(childWeight));
			}
		}
	}

	@Override
	public void init(IWorkbench workbench) {
	}
}