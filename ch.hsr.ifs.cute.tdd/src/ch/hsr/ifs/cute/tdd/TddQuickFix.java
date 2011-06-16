/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.cdt.internal.ui.viewsupport.LinkedProposalModelPresenter;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution2;

import ch.hsr.ifs.cute.tdd.LinkedMode.ChangeRecorder;
import ch.hsr.ifs.cute.tdd.LinkedMode.Position;
import ch.hsr.ifs.cute.tdd.createfunction.LinkedModeInformation;

@SuppressWarnings("restriction")
public abstract class TddQuickFix extends AbstractCodanCMarkerResolution implements
		IMarkerResolution2 {

	protected IMarker marker;
	protected ICProject project;
	protected CodanArguments ca;

	@Override
	public boolean isApplicable(IMarker marker) {
		this.marker = marker;
		ca = new CodanArguments(marker);
		return super.isApplicable(marker);
	}

	public abstract String getLabel();

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public Image getImage() {
		return null;
	}

	protected IDocument getDocument() {
		return getEditor().getDocumentProvider().getDocument(getEditor().getEditorInput());
	}

	public CEditor getEditor() {
		IEditorPart texteditor = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		return (CEditor) texteditor;
	}

	protected ICProject getProject() {
		return getEditor().getInputCElement().getCProject();
	}

	protected ITextSelection getSelection() {
		ITextSelection selection = (ITextSelection) getEditor().getSelectionProvider().getSelection();
		return selection;
	}

	protected String getMarkedText(IMarker marker) {
		try {
			int begin = marker.getAttribute(IMarker.CHAR_START, 0);
			int end = marker.getAttribute(IMarker.CHAR_END, 0);
			return getDocument().get(begin, end - begin);
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
			return "";
		} catch (NullPointerException e) { // only for tests
			return getProblemArgument(marker, 2);
		}
	}

	@Override
	public void apply(IMarker marker, IDocument document) {
		RefactoringASTCache astCache = new RefactoringASTCache();
		int markerOffset = marker.getAttribute(IMarker.CHAR_START, 0);
		int markerLength = marker.getAttribute(IMarker.CHAR_END, 0)-markerOffset;
		Change change = null;
		LinkedModeInformation lmi = null;
		try {
			CRefactoring3 refactoring = getRefactoring(astCache, new TextSelection(markerOffset, markerLength));
			lmi = refactoring.getLinkedModeInformation();
			change = refactoring.createChange(new NullProgressMonitor());
		} catch (CoreException e) {
			CUIPlugin.log(e);
		} finally {
			astCache.dispose();
		}
		ChangeRecorder rec = new ChangeRecorder(markerOffset, document, change, ca.getName());
		startLinkedMode(lmi, rec);
	}

	private void startLinkedMode(LinkedModeInformation lmi, ChangeRecorder rec) {
		try {
			configureLinkedMode(rec, lmi);
			LinkedProposalModel model = new LinkedProposalModel();
			if (lmi.getGroups().isEmpty()) {
				return;
			}
			for (LinkedProposalPositionGroup group : lmi.getGroups()) {
				model.addPositionGroup(group);
			}
			if (lmi.getExitOffset() > 0) {
				model.setEndPosition(new Position(lmi.getExitOffset(), 0));
			}
			new LinkedProposalModelPresenter().enterLinkedMode(getEditor().getViewer(), getEditor(), model);
		} catch (BadLocationException e) {
			TddHelper.showErrorOnStatusLine("Assisted text editing not available");
		}
	}

	protected abstract CRefactoring3 getRefactoring(RefactoringASTCache astCache, ITextSelection selection);
	protected void configureLinkedMode(ChangeRecorder rec, LinkedModeInformation lmi) throws BadLocationException {};
}
