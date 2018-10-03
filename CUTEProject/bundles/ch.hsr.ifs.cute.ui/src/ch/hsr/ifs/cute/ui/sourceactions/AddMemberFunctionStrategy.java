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
public class AddMemberFunctionStrategy extends AddMemberFunctionBaseStrategy {

    private static final String SCOPE         = "::";
    private static final String INSTANCE_NAME = "instance";
    private final IASTName      name;
    private boolean             needsConstructorParam;

    public AddMemberFunctionStrategy(IDocument doc, IFile editorFile, IASTTranslationUnit astTu, IASTName name,
                                     SuitePushBackFinder suitPushBackFinder) {
        super(doc, astTu, editorFile, suitPushBackFinder);
        this.name = name;
    }

    public MultiTextEdit addMemberToSuite() {
        StringBuilder builder = getPushbackStatement(name, suitPushBackFinder.getSuiteDeclName());
        MultiTextEdit edit = new MultiTextEdit();
        edit.addChild(createPushBackEdit(editorFile, astTu, suitPushBackFinder, builder.toString()));
        return edit;
    }

    private StringBuilder getPushbackStatement(IASTName testName, IASTName suiteName) {
        needsConstructorParam = checkForConstructorWithParameters(astTu, name);
        StringBuilder sb = new StringBuilder(newLine + "\t");
        if (needsConstructorParam) {
            appendInstanceInitializationLine(sb);
        }
        sb.append(suiteName.toString());
        sb.append(".push_back(");
        sb.append(createPushBackContent());
        sb.append(");");
        return sb;
    }

    private void appendInstanceInitializationLine(StringBuilder sb) {
        ICPPMethod methodBinding = (ICPPMethod) name.resolveBinding();
        sb.append(assambleQualifier(methodBinding) + " " + INSTANCE_NAME + "(" + PARAMETERS_REQUIRED + ")");
        sb.append(";" + newLine + "\t");
    }

    @Override
    public String createPushBackContent() {
        if (needsConstructorParam) {
            return buildMEMFUNContent();
        } else {
            return buildSMEMFUNContent();
        }
    }

    private String buildMEMFUNContent() {
        StringBuilder builder = new StringBuilder();
        builder.append("CUTE_MEMFUN(");
        ICPPMethod methodBinding = (ICPPMethod) name.resolveBinding();
        builder.append(INSTANCE_NAME);
        builder.append(", ");
        builder.append(assambleQualifier(methodBinding));
        builder.append(", ");
        builder.append(methodBinding.getName());
        builder.append(")");
        return builder.toString();
    }

    private String buildSMEMFUNContent() {
        StringBuilder builder = new StringBuilder();
        builder.append("CUTE_SMEMFUN(");
        ICPPMethod methodBinding = (ICPPMethod) name.resolveBinding();
        builder.append(assambleQualifier(methodBinding));
        builder.append(", ");
        builder.append(methodBinding.getName());
        builder.append(")");
        return builder.toString();
    }

    private String assambleQualifier(ICPPMethod methodBinding) {
        try {
            String[] nameParts = methodBinding.getQualifiedName();
            StringBuilder builder = new StringBuilder(nameParts[0]);
            for (int i = 1; i < nameParts.length - 1; i++) {
                builder.append(SCOPE).append(nameParts[i]);
            }
            return builder.toString();
        } catch (DOMException e) {
            return "";
        }
    }

    @Override
    public MultiTextEdit getEdit() {
        return addMemberToSuite();
    }

}
