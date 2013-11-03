/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat
 * @since 4.0
 * 
 */
public class AddMemberFunctionStrategy extends AddPushbackStatementStrategy {

	private static final String SCOPE = "::";
	private final IFile editorFile;
	private final IASTTranslationUnit astTu;
	private final IASTName name;
	private final SuitePushBackFinder suitPushBackFinder;

	public AddMemberFunctionStrategy(IDocument doc, IFile editorFile, IASTTranslationUnit astTu, IASTName name, SuitePushBackFinder suitPushBackFinder) {
		super(doc, astTu);
		this.editorFile = editorFile;
		this.astTu = astTu;
		this.name = name;
		this.suitPushBackFinder = suitPushBackFinder;
	}

	public MultiTextEdit addMemberToSuite() {
		final StringBuilder builder = getPushbackStatement(name, suitPushBackFinder.getSuiteDeclName());
		final MultiTextEdit edit = new MultiTextEdit();
		edit.addChild(createPushBackEdit(editorFile, astTu, suitPushBackFinder, builder.toString()));
		return edit;
	}

	private StringBuilder getPushbackStatement(IASTName testName, IASTName suiteName) {
		final StringBuilder sb = new StringBuilder(newLine + "\t");
		sb.append(suiteName.toString());
		sb.append(".push_back(");
		sb.append(createPushBackContent());
		sb.append(");");
		return sb;
	}

	@Override
	public String createPushBackContent() {
		StringBuilder builder = new StringBuilder();
		builder.append("CUTE_SMEMFUN(");
		final IBinding nameBinding = name.resolveBinding();
		if (nameBinding instanceof ICPPMethod) {
			final ICPPMethod methodBinding = (ICPPMethod) nameBinding;
			try {
				builder.append(assambleQualifier(methodBinding));
				builder.append(", ");
				builder.append(methodBinding.getName());
			} catch (DOMException e) {
			}
		}
		builder.append(")");
		return builder.toString();
	}

	private String assambleQualifier(final ICPPMethod methodBinding) throws DOMException {
		final String[] nameParts = methodBinding.getQualifiedName();
		final StringBuilder builder = new StringBuilder(nameParts[0]);
		for (int i = 1; i < nameParts.length - 1; i++) {
			builder.append(SCOPE).append(nameParts[i]);
		}
		return builder.toString();
	}

	public MultiTextEdit getEdit() {
		return addMemberToSuite();
	}

}
