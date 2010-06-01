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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * @author Emanuel Graf IFS
 *
 */
public class AddMemberFunctionStrategy extends AddStrategy{
	
	private IFile editorFile;
	private IASTTranslationUnit astTu;
	private IASTName name;
	private SuitePushBackFinder suitPushBackFinder;
	
	public AddMemberFunctionStrategy(IDocument doc, IFile editorFile, IASTTranslationUnit astTu, IASTName name,
			SuitePushBackFinder suitPushBackFinder) {
		super(doc);
		this.editorFile = editorFile;
		this.astTu = astTu;
		this.name = name;
		this.suitPushBackFinder = suitPushBackFinder;
	}

	public MultiTextEdit addMemberToSuite() {
		StringBuilder builder = getString((ICPPASTQualifiedName) name, suitPushBackFinder);
		MultiTextEdit edit = new MultiTextEdit();
		edit.addChild(createPushBackEdit(editorFile, astTu, suitPushBackFinder, builder));
		return edit;
	}

	private StringBuilder getString(ICPPASTQualifiedName qName, SuitePushBackFinder suitPushBackFinder) {
		StringBuilder sb = new StringBuilder(newLine + "\t"); // s.push_back(CUTE_SMEMFUN(TestClass,test2);");
		sb.append(suitPushBackFinder.getSuiteDeclName().toString());
		sb.append(".push_back(CUTE_SMEMFUN(");
		IASTName[] names = qName.getNames();
		if (names.length >= 2) {
			sb.append(names[names.length -2]);
			sb.append(", ");
			sb.append(names[names.length-1]);
			sb.append("));");
		}
		return sb;
	}

	@Override
	public MultiTextEdit getEdit() {
		return addMemberToSuite();
	}

}
