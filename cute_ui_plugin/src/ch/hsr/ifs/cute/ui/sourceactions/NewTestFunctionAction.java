/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Emanuel Graf
 * @author Thomas Corbat IFS
 * 
 */
public class NewTestFunctionAction extends AbstractFunctionAction {
	//TODO create Strategy or new Superclass

	protected static final String TEST_STMT = "\tASSERTM(\"start writing tests\", false);"; //$NON-NLS-1$
	int problemMarkerErrorLineNumber = 0;
	/**
	 * @since 4.0
	 */
	protected int insertFileOffset;
	/**
	 * @since 4.0
	 */
	protected int pushbackOffset;
	/**
	 * @since 4.0
	 */
	protected int pushbackLength;

	private final String funcName;

	/**
	 * @since 4.0
	 */
	public NewTestFunctionAction(String funcName) {
		this.funcName = funcName;
	}

	@Override
	public MultiTextEdit createEdit(IFile file, IDocument doc, ISelection sel) throws CoreException {
		IAddStrategy strategy = new NullStrategy(doc);
		if (sel != null && sel instanceof TextSelection) {
			TextSelection selection = (TextSelection) sel;

			IASTTranslationUnit astTu = getASTTranslationUnit(file);

			SuitePushBackFinder suitPushBackFinder = new SuitePushBackFinder();
			astTu.accept(suitPushBackFinder);
			final AddNewTestStrategy newTestStrategy = new AddNewTestStrategy(doc, file, astTu, funcName, suitPushBackFinder, selection);

			if (suitPushBackFinder.getSuiteNode() == null) {
				int insertOffset = newTestStrategy.getInsertOffset(astTu, selection, doc);
				final AddSuiteStrategy newSuiteStrategy = new AddSuiteStrategy(newTestStrategy, insertOffset);
				final MultiTextEdit compositeEdit = new MultiTextEdit();
				compositeEdit.addChild(newTestStrategy.createInsertTestFunctionEdit(insertOffset, doc, funcName));
				TextEdit[] suiteChildren = newSuiteStrategy.getEdit().removeChildren();
				compositeEdit.addChildren(suiteChildren);
				return compositeEdit;
			} else {
				return newTestStrategy.getEdit();
			}
		}
		return strategy.getEdit();
	}

}