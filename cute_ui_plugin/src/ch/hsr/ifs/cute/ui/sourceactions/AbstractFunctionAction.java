/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

public abstract class AbstractFunctionAction {
	
	/**
	 * @since 4.0
	 */
	public abstract MultiTextEdit createEdit(ITextEditor ceditor,
			IEditorInput editorInput, IDocument doc, ISelection selection)
			throws CoreException;
	
	//return the CDT representation of the file under modification 
	protected IASTTranslationUnit getASTTranslationUnit(IFile editorFile)
			throws CoreException {
		ITranslationUnit tu = CoreModelUtil.findTranslationUnit(editorFile);
		IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());	
		IASTTranslationUnit astTu = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
		return astTu;
	}

	
	public void createProblemMarker(FileEditorInput editorInput,String message,int lineNo){
		
		try {
			IFile editorFile = (editorInput).getFile();
			IMarker marker = editorFile.createMarker("org.eclipse.cdt.core.problem"); //$NON-NLS-1$
		    marker.setAttribute(IMarker.MESSAGE, "cute:"+message); //$NON-NLS-1$
		    marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
		    marker.setAttribute(IMarker.TRANSIENT, true);
		    if(lineNo!=0)marker.setAttribute(IMarker.LINE_NUMBER, lineNo);
		    marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	   } catch (CoreException e) {
	      // You need to handle the cases where attribute value is rejected
	   }
	}
	
}
//http://www.ibm.com/developerworks/library/os-ecl-cdt3/index.html?S_TACT=105AGX44&S_CMP=EDU
//Building a CDT-based editor