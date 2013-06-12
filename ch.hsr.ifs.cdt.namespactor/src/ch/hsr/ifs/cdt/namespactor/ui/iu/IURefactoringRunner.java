/******************************************************************************
* Copyright (c) 2012 Institute for Software, HSR Hochschule fuer Technik 
* Rapperswil, University of applied sciences and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html 
*
* Contributors:
* 	Ueli Kunz <kunz@ideadapt.net>, Jules Weder <julesweder@gmail.com> - initial API and implementation
******************************************************************************/
package ch.hsr.ifs.cdt.namespactor.ui.iu;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringRunner;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringSaveHelper;
import org.eclipse.cdt.internal.ui.refactoring.utils.SelectionHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;

import ch.hsr.ifs.cdt.namespactor.astutil.NSSelectionHelper;
import ch.hsr.ifs.cdt.namespactor.refactoring.NSRefactoringWizard;
import ch.hsr.ifs.cdt.namespactor.refactoring.iudec.IUDECRefactoring;
import ch.hsr.ifs.cdt.namespactor.refactoring.iudir.IUDIRRefactoring;
import ch.hsr.ifs.cdt.namespactor.resources.Labels;

/**
 * @author kunz@ideadapt.net
 * */
@SuppressWarnings("restriction")
public class IURefactoringRunner extends RefactoringRunner {

	private CRefactoringContext cRefactoringContext;

	public IURefactoringRunner(ICElement element, ISelection selection, IShellProvider shellProvider, ICProject cProject) {
		super(element, selection, shellProvider, cProject);
	}

	@Override
	public void run() {
		Region region = getSelectedRegion();
		RefactoringWizard wizard = null;
		CRefactoring refactoring = null;
		IASTTranslationUnit ast = getAST();
		if(NSSelectionHelper.getSelectedUsingDirective(region, ast) != null){
			refactoring = new IUDIRRefactoring(element, selection, project);
			wizard = new NSRefactoringWizard(refactoring);
			
		}else if(NSSelectionHelper.getSelectedUsingDeclaration(region, ast) != null){
			refactoring = new IUDECRefactoring(element, selection, project);
			wizard = new NSRefactoringWizard(refactoring);
		}else{
			MessageDialog.openError(shellProvider.getShell(), "Inline Refactoring Startup Error", Labels.IU_NoUsingSelected);
		}
		
		if(wizard != null){
			run(wizard, refactoring, RefactoringSaveHelper.SAVE_REFACTORING);
		}
		if (cRefactoringContext != null)
			cRefactoringContext.dispose();
	}

	private IASTTranslationUnit getAST() {
		if (!(element instanceof ISourceReference)) {
			return null;
		}
		ISourceReference sourceRef = (ISourceReference) element;
		CRefactoring refactoring = new CRefactoring(element, selection, project) {

			@Override
			protected RefactoringDescriptor getRefactoringDescriptor() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			protected void collectModifications(IProgressMonitor pm, ModificationCollector collector) throws CoreException, OperationCanceledException {
				// TODO Auto-generated method stub

			}
		};
		cRefactoringContext = new CRefactoringContext(refactoring);
		ITranslationUnit tu = CModelUtil.toWorkingCopy(sourceRef.getTranslationUnit());
		try {
			return cRefactoringContext.getAST(tu, new NullProgressMonitor());
		} catch (OperationCanceledException e) {
			// TODO Auto-generated catch block
		} catch (CoreException e) {
			// TODO Auto-generated catch block
		} finally {
		}
		return null;
	}

	private Region getSelectedRegion() {
		Region region = null;
		if (selection instanceof ITextSelection) {
			region = SelectionHelper.getRegion(selection);
		} else {
			try {
				ISourceRange sourceRange = ((ISourceReference) element).getSourceRange();
				region = new Region(sourceRange.getIdStartPos(), sourceRange.getIdLength());
			} catch (CModelException e) {
				CUIPlugin.log(e);
			}
		}
		return region;
	}

}
