/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Emanuel Graf - initial API and implementation
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;


/**
 * @author Emanuel Graf
 *
 */
public class NewTestFunctionAction extends AbstractFunctionAction{
	
	protected static final String TEST_STMT = "\tASSERTM(\"start writing tests\", false);";

	private final class SuitPushBackFinder extends ASTVisitor {
		private IASTName suiteDeclName = null;

		
		{
			shouldVisitStatements = true;
		}

		@Override
		public int leave(IASTStatement statement) {
			if (statement instanceof IASTDeclarationStatement) {
				IASTDeclarationStatement declStmt = (IASTDeclarationStatement) statement;
				IASTDeclaration decl = declStmt.getDeclaration();
				if (decl instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration sDecl = (IASTSimpleDeclaration) decl;
					IASTDeclSpecifier declSpec = sDecl.getDeclSpecifier();
					if (declSpec instanceof ICPPASTNamedTypeSpecifier) {
						ICPPASTNamedTypeSpecifier nDeclSpec = (ICPPASTNamedTypeSpecifier) declSpec;
						if(nDeclSpec.getName().toString().equals("cute::suite")) {
							suiteDeclName = sDecl.getDeclarators()[0].getName();
						}
						
					}
				}
			}
			return super.leave(statement);
		}
		
		public IASTName getSuiteDeclName() {
			return suiteDeclName;
		}
	}
	
	
	@Override
	public MultiTextEdit createEdit(TextEditor ceditor,
			IEditorInput editorInput, IDocument doc, String funcName)
			throws CoreException {
		MultiTextEdit mEdit = new MultiTextEdit();
		ISelection sel = ceditor.getSelectionProvider().getSelection();
		if (sel != null && sel instanceof TextSelection) {
			TextSelection selection = (TextSelection) sel;

			if (editorInput instanceof FileEditorInput) {
				IFile editorFile = ((FileEditorInput) editorInput).getFile();
				IASTTranslationUnit astTu = getASTTranslationUnit(editorFile);
				int insertFileOffset = getInsertOffset(astTu, selection);
				SuitPushBackFinder suitPushBackFinder = new SuitPushBackFinder();
				astTu.accept(suitPushBackFinder);

				mEdit.addChild(createdEdit(insertFileOffset, doc, funcName));
				mEdit.addChild(createPushBackEdit(editorFile, doc, astTu,
						funcName, suitPushBackFinder));

			}
		}
		return mEdit;
	}


	private TextEdit createPushBackEdit(IFile editorFile, IDocument doc, IASTTranslationUnit astTu, String funcName, SuitPushBackFinder suitPushBackFinder) {
		String newLine = TextUtilities.getDefaultLineDelimiter(doc);
		
		if(suitPushBackFinder.getSuiteDeclName() != null) {
			IASTName name = suitPushBackFinder.getSuiteDeclName();
			IBinding binding = name.resolveBinding();
			IASTName[] refs = astTu.getReferences(binding);
			IASTStatement lastPushBack = getLastPushBack(refs);
			if(lastPushBack != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(newLine);
				builder.append("\t");
				builder.append(name.toString());
				builder.append(".push_back(CUTE(");
				builder.append(funcName);
				builder.append("));");
				IASTFileLocation fileLocation = lastPushBack.getFileLocation();
				InsertEdit edit = new InsertEdit(fileLocation.getNodeOffset() + fileLocation.getNodeLength(), builder.toString());
				return edit;
			}else {
				//TODO Errorhandling
				return null;
			}
		}else {
			//TODO Errorhandling
			return null;
		}
		
	}


	private IASTStatement getLastPushBack(IASTName[] refs) {
		IASTName lastPushBack = null;
		for (IASTName name : refs) {
			if(name.getParent().getParent() instanceof ICPPASTFieldReference) {
				IASTFieldReference fRef = (ICPPASTFieldReference) name.getParent().getParent();
				if(fRef.getFieldName().toString().equals("push_back")) {
					lastPushBack = name;
				}
			}
		}
		return getParentStatement(lastPushBack);
	}


	private IASTStatement getParentStatement(IASTName lastPushBack) {
		IASTNode node = lastPushBack;
		while(node != null) {
			if (node instanceof IASTStatement) {
				return (IASTStatement) node;
			}
			node = node.getParent();
		}
		return null;
	}


	private TextEdit createdEdit(int insertTestFuncFileOffset, IDocument doc, String funcName) {
		String newLine = TextUtilities.getDefaultLineDelimiter(doc);
		StringBuilder builder = new StringBuilder();
		builder.append("void ");
		builder.append(funcName);
		builder.append("(){");
		builder.append(newLine);
		builder.append(TEST_STMT);
		builder.append(newLine);
		builder.append("}");
		builder.append(newLine);
		builder.append(newLine);
		TextEdit iedit = new InsertEdit(insertTestFuncFileOffset, builder.toString());
		return iedit;
	}


	
	

}

