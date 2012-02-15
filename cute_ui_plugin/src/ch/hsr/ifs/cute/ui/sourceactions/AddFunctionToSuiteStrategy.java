/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;

import ch.hsr.ifs.cute.ui.UiPlugin;

/**
 * @author Emanuel Graf IFS
 * @since 4.0
 * 
 */
public class AddFunctionToSuiteStrategy extends AddPushbackStatementStrategy {

	private final SuitePushBackFinder suitPushBackFinder;
	private final IASTName name;
	private final IFile file;

	public AddFunctionToSuiteStrategy(IDocument doc, IFile file, IASTTranslationUnit tu, IASTName iastName, SuitePushBackFinder finder) {
		super(doc, tu);
		this.file = file;
		this.name = iastName;
		this.suitPushBackFinder = finder;

	}

	@Override
	public MultiTextEdit getEdit() {
		//TODO do not add the function holding the suite
		IIndex index = astTu.getIndex();
		try {
			index.acquireReadLock();
			MultiTextEdit mEdit = new MultiTextEdit();
			if (!checkPushback(astTu, name.toString(), suitPushBackFinder)) {
				mEdit.addChild(createPushBackEdit(file, astTu, name, suitPushBackFinder));
			}
			return mEdit;
		} catch (InterruptedException e) {
			UiPlugin.log(e);
		} finally {
			index.releaseReadLock();
		}
		return new MultiTextEdit();
	}

	@Override
	public String createPushBackContent() {
		StringBuilder builder = new StringBuilder();
		builder.append("CUTE(").append(name).append(")");
		return builder.toString();
	}
}
