/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * @author Emanuel Graf IFS
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
