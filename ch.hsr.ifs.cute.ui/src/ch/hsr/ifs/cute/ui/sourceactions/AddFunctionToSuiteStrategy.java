/*******************************************************************************
 * Copyright (c) 2007-2012, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;

import ch.hsr.ifs.cute.ui.CuteUIPlugin;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 * @since 4.0
 * 
 */
public class AddFunctionToSuiteStrategy extends AddPushbackStatementStrategy {

	protected final SuitePushBackFinder suitPushBackFinder;
	protected final String testName;
	protected final IFile file;

	public AddFunctionToSuiteStrategy(IDocument doc, IFile file, IASTTranslationUnit tu, String testName, SuitePushBackFinder finder) {
		super(doc, tu);
		this.file = file;
		this.testName = testName;
		this.suitPushBackFinder = finder;

	}

	public MultiTextEdit getEdit() {
		// TODO do not add the function holding the suite
		IIndex index = astTu.getIndex();
		try {
			index.acquireReadLock();
			MultiTextEdit mEdit = new MultiTextEdit();
			if (!checkPushback(astTu, testName, suitPushBackFinder)) {
				mEdit.addChild(createPushBackEdit(file, astTu, suitPushBackFinder));
			}
			return mEdit;
		} catch (InterruptedException e) {
			CuteUIPlugin.log(e);
		} finally {
			index.releaseReadLock();
		}
		return new MultiTextEdit();
	}

	@Override
	public String createPushBackContent() {
		StringBuilder builder = new StringBuilder();
		builder.append("CUTE(").append(testName).append(")");
		return builder.toString();
	}
}
