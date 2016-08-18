/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.sourceactions;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.MultiTextEdit;

import ch.hsr.ifs.cute.core.CuteCorePlugin;
import ch.hsr.ifs.cute.ui.ASTUtil;

/**
 * @since 4.0
 */
public class AddTestToSuite extends AbstractFunctionAction {

	@Override
	public MultiTextEdit createEdit(IFile file, IDocument doc, ISelection sel) throws CoreException {
		IAddStrategy adder = new NullStrategy(doc);
		IASTTranslationUnit astTu = null;
		IIndex index = null;
		initContext();
		try {
			if (sel != null && sel instanceof TextSelection) {
				TextSelection selection = (TextSelection) sel;

				try {
					astTu = acquireAST(file);
					index = astTu.getIndex();
					index.acquireReadLock();
					NodeAtCaretFinder n = new NodeAtCaretFinder(selection.getOffset());
					astTu.accept(n);
					IASTFunctionDefinition def = getFunctionDefinition(n.getMatchingNode());

					if (ASTUtil.isTestFunction(def)) {
						SuitePushBackFinder suiteFinder = new SuitePushBackFinder();
						astTu.accept(suiteFinder);
						IASTNode suite = suiteFinder.getSuiteNode();

						AddPushbackStatementStrategy lineStrategy = new NullStrategy(doc);
						IASTFunctionDeclarator declarator = def.getDeclarator();
						IASTName name = declarator.getName();
						if (isMemberFunction(def)) { // In .cpp file
							if (ASTHelper.isFunctor(def.getDeclarator())) {
								lineStrategy = new AddFunctorStrategy(doc, astTu, n.getMatchingNode(), file,
										suiteFinder);
							} else {
								lineStrategy = new AddMemberFunctionStrategy(doc, file, astTu, name, suiteFinder);
							}
						} else if (isFunction(def)) {
							String functionName = name.toString();
							lineStrategy = new AddFunctionStrategy(doc, file, astTu, functionName, suiteFinder);
						}
						if (suite == null) {
							adder = new AddSuiteStrategy(lineStrategy);
						} else {
							adder = lineStrategy;
						}

					}
				} catch (InterruptedException e) {
					CuteCorePlugin.log(e);
				} finally {
					disposeContext();
				}
			}

			return adder.getEdit();
		} finally {
			if (index != null) {
				index.releaseReadLock();
			}
		}
	}

	protected IASTFunctionDefinition getFunctionDefIfIsFunctor(IASTNode n) {
		if (n instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration sDecl = (IASTSimpleDeclaration) n;
			if (sDecl.getDeclSpecifier() instanceof IASTCompositeTypeSpecifier) {
				IASTCompositeTypeSpecifier comDeclSpec = (IASTCompositeTypeSpecifier) sDecl.getDeclSpecifier();
				IASTDeclaration[] members = comDeclSpec.getMembers();
				for (IASTDeclaration iastDeclaration : members) {
					if (iastDeclaration instanceof IASTFunctionDefinition) {
						IASTFunctionDefinition funcDef = (IASTFunctionDefinition) iastDeclaration;
						IASTName funcName = funcDef.getDeclarator().getName();
						if (funcName instanceof ICPPASTOperatorName && funcName.toString().contains("()")) {
							return funcDef;
						}
					}
				}
			}
		}
		return n != null && n.getParent() != null ? getFunctionDefIfIsFunctor(n.getParent()) : null;
	}

	private boolean isFunction(IASTFunctionDefinition def) {
		return bindingOfFunction(def) instanceof ICPPFunction;
	}

	private boolean isMemberFunction(IASTFunctionDefinition def) {
		return bindingOfFunction(def) instanceof ICPPMethod;
	}

	private IBinding bindingOfFunction(IASTFunctionDefinition def) {
		return def.getDeclarator().getName().resolveBinding();
	}

	private IASTFunctionDefinition getFunctionDefinition(IASTNode node) {
		if (node == null)
			return null;
		if (node instanceof IASTFunctionDefinition) {
			return (IASTFunctionDefinition) node;
		} else {
			IASTNode parent = node.getParent();
			return getFunctionDefinition(parent);
		}
	}
}