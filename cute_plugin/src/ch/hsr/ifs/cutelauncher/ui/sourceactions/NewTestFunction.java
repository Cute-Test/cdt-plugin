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

import org.eclipse.cdt.core.CCorePlugin;
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
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * @author Emanuel Graf
 *
 */
public class NewTestFunction implements IEditorActionDelegate {
	
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

	private IEditorPart editor;
	
	public void dispose() {
		// TODO Auto-generated method stub
		
	}


	public void run(IAction action) {
		try {	
			if (editor != null && editor instanceof TextEditor) {
				TextEditor ceditor = (TextEditor) editor;
				ISelection sel = ceditor.getSelectionProvider().getSelection();
				if(sel != null && sel instanceof TextSelection) {
					TextSelection selection = (TextSelection)sel;
					IEditorInput editorInput = ceditor.getEditorInput();
					if (editorInput instanceof FileEditorInput) {
						IFile editorFile = ((FileEditorInput) editorInput).getFile();
						IASTTranslationUnit astTu = getASTTranslationUnit(editorFile);
						int insertFileOffset = getInsertOffset(astTu, selection);
						IDocumentProvider prov = ceditor.getDocumentProvider();
						IDocument doc = prov.getDocument(editorInput);
						MultiTextEdit mEdit = new MultiTextEdit();
						
						mEdit.addChild(createdEdit(insertFileOffset, doc, "thisIsATestFunc"));
						mEdit.addChild(createPushBackEdit(editorFile, doc, astTu, "thisIsATestFunc"));
						mEdit.apply(doc);
					}
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedTreeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}


	private TextEdit createPushBackEdit(IFile editorFile, IDocument doc, IASTTranslationUnit astTu, String funcName) {
		String newLine = TextUtilities.getDefaultLineDelimiter(doc);
		SuitPushBackFinder suitPushBackFinder = new SuitPushBackFinder();
		astTu.accept(suitPushBackFinder);
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
		builder.append(newLine);
		builder.append(newLine);
		builder.append("void ");
		builder.append(funcName);
		builder.append("(){");
		builder.append(newLine);
		builder.append("\tASSERTM(\"start writing tests\", false);");
		builder.append(newLine);
		builder.append("}");
		TextEdit iedit = new InsertEdit(insertTestFuncFileOffset, builder.toString());
		return iedit;
	}


	private IASTTranslationUnit getASTTranslationUnit(IFile editorFile)
			throws CoreException {
		ITranslationUnit tu = CoreModelUtil.findTranslationUnit(editorFile);
		IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());	
		IASTTranslationUnit astTu = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
		return astTu;
	}

	private int getInsertOffset(IASTTranslationUnit astTu, TextSelection selection) {
		int selOffset = selection.getOffset();
		IASTDeclaration[] decls = astTu.getDeclarations();
		for (IASTDeclaration declaration : decls) {
			int nodeOffset = declaration.getFileLocation().getNodeOffset();
			int nodeLength = declaration.getFileLocation().asFileLocation().getNodeLength();
			if(selOffset > nodeOffset && selOffset < (nodeOffset+ nodeLength)) {
				return (nodeOffset + nodeLength);
			}else if(selOffset <= nodeOffset) {
				return selOffset;
			}
		}
		return selOffset;
	}


	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		editor = targetEditor;
	}
	
	

}
