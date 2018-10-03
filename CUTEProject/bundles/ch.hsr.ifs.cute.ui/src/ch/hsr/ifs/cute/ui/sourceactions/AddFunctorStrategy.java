/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MultiTextEdit;

import ch.hsr.ifs.cute.ui.CuteUIPlugin;


/**
 * @author Emanuel Graf IFS
 * @since 4.0
 *
 */
public class AddFunctorStrategy extends AddMemberFunctionBaseStrategy {

    private final IASTNode node;
    private final IASTName fname;

    public AddFunctorStrategy(IDocument doc, IASTTranslationUnit astTu, IASTNode node, IFile editorFile, SuitePushBackFinder suitPushBackFinder) {
        super(doc, astTu, editorFile, suitPushBackFinder);
        this.node = node;
        OperatorParenthesesFinder o = new OperatorParenthesesFinder();
        astTu.accept(o);
        fname = nameAtCursor(o.getAL(), node);
    }

    @Override
    public MultiTextEdit getEdit() {

        if (fname == null) {
            return new MultiTextEdit();// FIXME potential bug point
        }

        SuitePushBackFinder suitPushBackFinder = new SuitePushBackFinder();
        astTu.accept(suitPushBackFinder);
        IIndex index = astTu.getIndex();
        try {
            index.acquireReadLock();
            if (!checkPushback(astTu, fname.toString(), suitPushBackFinder)) {
                MultiTextEdit mEdit = new MultiTextEdit();
                mEdit.addChild(createPushBackEdit(editorFile, astTu, suitPushBackFinder));
                return mEdit;
            }
        } catch (InterruptedException e) {} finally {
            index.releaseReadLock();
        }
        return new MultiTextEdit();
    }

    private boolean checkClassForPublicOperatorParentesis(IASTNode node) {
        IASTNode tmp = node;
        while (!(tmp.getParent() instanceof ICPPASTCompositeTypeSpecifier) && !(tmp.getParent() instanceof ICPPASTTranslationUnit)) {
            tmp = tmp.getParent();
        }
        if (tmp.getParent() instanceof ICPPASTCompositeTypeSpecifier) tmp = tmp.getParent().getParent();

        boolean publicOperatorExist = false;
        if (tmp instanceof IASTSimpleDeclaration) {
            ArrayList<IASTDeclaration> al = ASTHelper.getPublicMethods((IASTSimpleDeclaration) tmp);
            for (IASTDeclaration i : al) {
                if (ASTHelper.getMethodName(i).equals(Messages.getString("AddTestFunctortoSuiteAction.Operator"))) {
                    publicOperatorExist = true;
                    break;
                }
            }
        }
        return publicOperatorExist;
    }

    private boolean isTemplateClass(IASTNode checkforTemplate) {
        while (!(checkforTemplate instanceof ICPPASTTranslationUnit)) {
            if (checkforTemplate instanceof ICPPASTTemplateDeclaration) {
                return true;
            }
            checkforTemplate = checkforTemplate.getParent();
        }
        return false;
    }

    private boolean isVirtualOperatorDeclared(ArrayList<IASTName> operatorParenthesesNode, IASTNode node, boolean operatorMatchFlag) {
        if (node instanceof IASTSimpleDeclaration || node instanceof IASTFunctionDefinition) {
            if (node.getParent() instanceof ICPPASTCompositeTypeSpecifier) {
                IASTNode tmp = node.getParent();
                for (IASTName i : operatorParenthesesNode) {
                    if (tmp.contains(i)) {
                        operatorMatchFlag = true;
                        break;
                    }
                }
            }
        }
        return operatorMatchFlag;
    }

    private IASTNode getWantedTypeParent(IASTNode node) {
        IASTNode parentNode = node, prevNode = node;
        while (!(parentNode instanceof IASTFunctionDefinition || parentNode instanceof IASTSimpleDeclaration ||
                 parentNode instanceof ICPPASTTranslationUnit)) {
            try {
                prevNode = parentNode;
                parentNode = parentNode.getParent();
            } catch (NullPointerException npe) {
                return prevNode;
            }
        }
        return parentNode;
    }

    protected IASTName nameAtCursor(ArrayList<IASTName> operatorParenthesesNode, IASTNode node) {
        if (node instanceof IASTDeclaration) {
            if (node instanceof ICPPASTVisibilityLabel) {
                // public: private: protected: for class
                node = node.getParent().getParent();
                // FIXME operator() is private,protected in a class/struct??
            }

            try {

                boolean flag = checkClassForPublicOperatorParentesis(node);
                if (!flag) {
                    return null;
                }

            } catch (NullPointerException npe) {
                CuteUIPlugin.log("Exception while finding name at cursor", npe);
            } catch (ClassCastException cce) {
                CuteUIPlugin.log("Exception while finding name at cursor", cce);
            }

            boolean flag = isTemplateClass(node);
            if (flag) {
                return null;
            }

            // check class, struct at cursor for operator()
            boolean operatorMatchFlag = false;
            for (IASTName i : operatorParenthesesNode) {
                if (node.contains(i)) {
                    operatorMatchFlag = true;
                    break;
                }
            }

            operatorMatchFlag = isVirtualOperatorDeclared(operatorParenthesesNode, node, operatorMatchFlag);

            if (!operatorMatchFlag) {
                return null;
            }

            // TODO check also operator() doesnt have parameters, or at least default binded
            // TODO check for function not virtual and has a method body
            if (node instanceof IASTSimpleDeclaration) {// simple class case
                /*
                 * class TFunctor{ private: public:**but cannot handle the methods }
                 */
                IASTDeclSpecifier aa = (((IASTSimpleDeclaration) node).getDeclSpecifier());
                if (null != aa && aa instanceof IASTCompositeTypeSpecifier) {
                    IASTName i = ((IASTCompositeTypeSpecifier) aa).getName();
                    return i;
                }
            }
        }

        // FIXME wouldnt be detected as preprocess statement NodeAtCursorFinder returns null
        /*
         * if(node instanceof IASTPreprocessorStatement){ stream.println("preprocessor statement selected, unable to add as functor."); return ""; }
         */

        IASTNode parentNode = getWantedTypeParent(node);
        if (parentNode instanceof IASTFunctionDefinition || parentNode instanceof IASTSimpleDeclaration) {
            // handle the simple class case, cursor at methods
            // if(!(parentNode.getParent() instanceof ICPPASTTranslationUnit))
            return ((ICPPASTCompositeTypeSpecifier) (parentNode.getParent())).getName();
        }
        return null;
    }

    @Override
    public String createPushBackContent() {
        StringBuilder builder = new StringBuilder();
        builder.append(fname).append("(");

        if (checkForConstructorWithParameters(astTu, node)) {
            builder.append(PARAMETERS_REQUIRED);
        }
        builder.append(")");
        return builder.toString();
    }

}
