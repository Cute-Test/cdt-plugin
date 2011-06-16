/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.ui.tests;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * Imitates a change from CRefactoring.createChange().
 */
public class FakeChange extends CompositeChange {
	private IDocument document;
	private String insertedText;
	private boolean fail;

	public FakeChange(String contents, int offset, String insertedText) {
		this(new Document(contents), offset, insertedText);
		this.insertedText = insertedText;
	}

	public FakeChange(String contents, int offset, int length, String insertedText) {
		this(new Document(contents), offset, length, insertedText);
		this.insertedText = insertedText;
	}

	public FakeChange(IDocument contents, int offset, String insertedText) {
		super("test");
		init(contents, insertedText, new InsertEdit(offset, insertedText));
	}

	public FakeChange(IDocument contents, int offset, int length, String insertedText) {
		super("test");
		init(contents, insertedText, new ReplaceEdit(offset, length, insertedText));
	}

	private void init(IDocument contents, String insertedText, TextEdit edit) {
		this.document = contents;
		this.insertedText = insertedText;
		CompositeChange change = new CompositeChange("test");
		TextChange txt = new DocumentChange("test", document);
		MultiTextEdit multiTextEdit = new MultiTextEdit();
		multiTextEdit.addChild(edit);
		txt.setEdit(multiTextEdit);
		change.add(txt);
		add(change);
	}

	public String getInsertedText() {
		return insertedText;
	}

	public void setFailing() {
		fail = true;
	}

	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		if (fail)
			throw new CModelException(new Exception(), 0);
		return super.perform(pm);
	}
}