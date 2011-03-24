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
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * @author Emanuel Graf IFS
 * @since 4.0
 *
 */
public class AddFunctionToSuiteStrategy extends AddStrategy {
	
	

	private SuitePushBackFinder suitPushBackFinder;
	private IASTName name;
	private IASTTranslationUnit astTu;
	private IFile file;
	private IDocument doc;
	

	public AddFunctionToSuiteStrategy(IDocument doc, IFile file, IASTTranslationUnit tu, IASTName iastName, SuitePushBackFinder finder) {
		super(doc);
		this.doc = doc;
		this.file = file;
		this.astTu = tu;
		this.name = iastName;
		this.suitPushBackFinder = finder;
		
	}

	@Override
	public MultiTextEdit getEdit() {
		//TODO do not add the function holding the suite
			MultiTextEdit mEdit = new MultiTextEdit();
			String name = this.name.toString();
			if(!checkPushback(astTu,name,suitPushBackFinder))
			{
				mEdit.addChild(createPushBackEdit(file, doc, astTu,
						name, suitPushBackFinder));
			}
		return mEdit;
	}

}
