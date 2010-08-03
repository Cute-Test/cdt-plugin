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
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * @author Emanuel Graf IFS
 * @since 4.0
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
		StringBuilder builder;
		if (name instanceof ICPPASTQualifiedName) {
			ICPPASTQualifiedName qName = (ICPPASTQualifiedName) name;
			builder = getString(qName, suitPushBackFinder);
		}else {
			builder = getString(name, suitPushBackFinder);
		}
		MultiTextEdit edit = new MultiTextEdit();
		edit.addChild(createPushBackEdit(editorFile, astTu, suitPushBackFinder, builder));
		return edit;
	}

	private StringBuilder getString(ICPPASTQualifiedName qName, SuitePushBackFinder suitPushBackFinder) {
		StringBuilder sb = new StringBuilder(newLine + "\t"); // s.push_back(CUTE_SMEMFUN(TestClass,test2);"); //$NON-NLS-1$
		sb.append(suitPushBackFinder.getSuiteDeclName().toString());
		sb.append(".push_back(CUTE_SMEMFUN("); //$NON-NLS-1$
		IASTName[] names = qName.getNames();
		if (names.length >= 2) {
			sb.append(names[names.length -2]);
			sb.append(", "); //$NON-NLS-1$
			sb.append(names[names.length-1]);
			sb.append("));"); //$NON-NLS-1$
		}
		return sb;
	}
	
	private StringBuilder getString(IASTName name, SuitePushBackFinder suitPushBackFinder) {
		StringBuilder sb = new StringBuilder(newLine + "\t"); // s.push_back(CUTE_SMEMFUN(TestClass,test2);"); //$NON-NLS-1$
		sb.append(suitPushBackFinder.getSuiteDeclName().toString());
		sb.append(".push_back(CUTE_SMEMFUN("); //$NON-NLS-1$
		String className  = getClassName(name);
		sb.append(className);
		sb.append(", "); //$NON-NLS-1$
		sb.append(name.toString());
		sb.append("));"); //$NON-NLS-1$
		return sb;
	}

	private String getClassName(IASTName name2) {
		IASTNode n = null;
		for(n = name; n != null && !(n instanceof ICPPASTCompositeTypeSpecifier); n = n.getParent()) {}
		if(n != null) {
			ICPPASTCompositeTypeSpecifier comType = (ICPPASTCompositeTypeSpecifier)n;
			return comType.getName().toString();
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public MultiTextEdit getEdit() {
		return addMemberToSuite();
	}

}
