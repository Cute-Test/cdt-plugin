/*******************************************************************************
 * Copyright (c) 2007-2012, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 * @since 4.0
 * 
 */
public class AddNewTestStrategy extends AddFunctionToSuiteStrategy {

	protected static final String TEST_STMT = "\tASSERTM(\"start writing tests\", false);"; //$NON-NLS-1$

	protected int insertFileOffset = -1;
	protected int pushbackLength = -1;

	int problemMarkerErrorLineNumber = 0;
	private final IDocument document;
	private final TextSelection selection;

	public AddNewTestStrategy(IDocument document, IFile file, IASTTranslationUnit tu, String iastName, SuitePushBackFinder finder, TextSelection selection) {
		super(document, file, tu, iastName, finder);
		this.document = document;
		this.selection = selection;
	}

	@Override
	public MultiTextEdit getEdit() {
		MultiTextEdit mEdit = new MultiTextEdit();
		if (selection != null) {

			insertFileOffset = getInsertOffset(astTu, selection, document);

			SuitePushBackFinder suitPushBackFinder = new SuitePushBackFinder();
			astTu.accept(suitPushBackFinder);

			mEdit.addChild(createInsertTestFunctionEdit(getInsertOffset(suitPushBackFinder, astTu), document, testName.toString()));

			if (!checkPushback(astTu, testName.toString(), suitPushBackFinder))
				mEdit.addChild(createPushBackEdit(file, astTu, suitPushBackFinder));
			else {
				createProblemMarker(file, Messages.getString("NewTestFunctionAction.DuplicatedPushback"), problemMarkerErrorLineNumber); //$NON-NLS-1$
			}

		}
		return mEdit;
	}

	@Override
	protected TextEdit createPushBackEdit(IFile editorFile, IASTTranslationUnit astTu, SuitePushBackFinder suitPushBackFinder, String insertion) {
		pushbackLength = insertion.length();
		return super.createPushBackEdit(editorFile, astTu, suitPushBackFinder, insertion);
	}

	//adding the new test function
	protected TextEdit createInsertTestFunctionEdit(int insertTestFuncFileOffset, IDocument doc, String funcName) {
		StringBuilder builder = new StringBuilder();
		builder.append("void "); //$NON-NLS-1$
		builder.append(funcName);
		builder.append("(){"); //$NON-NLS-1$
		builder.append(newLine);
		builder.append(TEST_STMT);
		builder.append(newLine);
		builder.append("}"); //$NON-NLS-1$
		builder.append(newLine);
		builder.append(newLine);
		TextEdit iedit = new InsertEdit(insertTestFuncFileOffset, builder.toString());
		return iedit;
	}

	protected int getInsertOffset(SuitePushBackFinder suitePushBackFinder, IASTTranslationUnit astTu) {
		IASTName name = suitePushBackFinder.getSuiteDeclName();
		if (name != null) {
			IBinding binding = name.resolveBinding();
			IASTName[] refs = astTu.getReferences(binding);
			IASTStatement lastPushBackStmt = getLastPushBack(refs);
			if (lastPushBackStmt != null) {
				IASTFunctionDefinition funDef = getFunctionDefinition(lastPushBackStmt);
				int offset = funDef.getFileLocation().getNodeOffset();
				return insertFileOffset < offset ? insertFileOffset : offset;
			}
		}
		return insertFileOffset;
	}

	private IASTFunctionDefinition getFunctionDefinition(IASTStatement lastPushBackStmt) {
		IASTNode node = lastPushBackStmt;
		while (!(node instanceof IASTFunctionDefinition)) {
			node = node.getParent();
		}
		return (IASTFunctionDefinition) node;
	}

	//shift the insertion point out syntactical block, relative to user(selection point/current cursor)location
	protected int getInsertOffset(IASTTranslationUnit astTu, TextSelection selection, IDocument doc) {
		int selOffset = selection.getOffset();
		IASTDeclaration[] decls = astTu.getDeclarations();
		for (IASTDeclaration declaration : decls) {
			int nodeOffset = declaration.getFileLocation().getNodeOffset();
			int nodeLength = declaration.getFileLocation().asFileLocation().getNodeLength();
			if (selOffset > nodeOffset && selOffset < (nodeOffset + nodeLength)) {
				return (nodeOffset);
			}
		}

		//Shift out of preprocessor statements
		// >#include "cute.h<"
		IASTPreprocessorStatement[] listPreprocessor = astTu.getAllPreprocessorStatements();
		for (int x = 0; x < listPreprocessor.length; x++) {
			int nodeOffset = listPreprocessor[x].getFileLocation().getNodeOffset();
			int nodeLength = listPreprocessor[x].getFileLocation().asFileLocation().getNodeLength();
			if (selOffset > nodeOffset && selOffset < (nodeOffset + nodeLength)) {
				return nodeOffset;
			}
		}

		try {
			int selectedLineNo = selection.getStartLine();
			IRegion iregion = doc.getLineInformation(selectedLineNo);
			String text = doc.get(iregion.getOffset(), iregion.getLength());
			if (text.startsWith("#include")) { //$NON-NLS-1$
				return iregion.getOffset();
			}

		} catch (org.eclipse.jface.text.BadLocationException be) {
		}

		//just use the user selection if no match, it could possibly mean that the cursor at the 
		//very end of the source file
		return selOffset;
	}

	public void createProblemMarker(IFile file, String message, int lineNo) {

		try {
			IMarker marker = file.createMarker("org.eclipse.cdt.core.problem"); //$NON-NLS-1$
			marker.setAttribute(IMarker.MESSAGE, "cute:" + message); //$NON-NLS-1$
			marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
			marker.setAttribute(IMarker.TRANSIENT, true);
			if (lineNo != 0) {
				marker.setAttribute(IMarker.LINE_NUMBER, lineNo);
			}
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		} catch (CoreException e) {
			// You need to handle the cases where attribute value is rejected
		}
	}
}
