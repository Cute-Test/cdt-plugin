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
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Emanuel Graf IFS
 * @since 4.0
 *
 */
@SuppressWarnings("restriction")
public class LinkSuiteToRunnerProcessor {

	private static final String STRING = "\""; //$NON-NLS-1$
	private static final char[] CUTE = "cute".toCharArray(); //$NON-NLS-1$
	private IASTFunctionDefinition testRunner;
	private String suiteName;
	private ICPPNodeFactory nodeFactory;

	public LinkSuiteToRunnerProcessor(IASTFunctionDefinition testRunner, String suitename) {
		this.testRunner = testRunner;
		this.suiteName = suitename;
		nodeFactory = (ICPPNodeFactory) testRunner.getTranslationUnit().getASTNodeFactory();
	}

	public Change getLinkSuiteToRunnerChange() {
		
		ASTRewrite rw = ASTRewrite.create(testRunner.getTranslationUnit());
		changeRunnerBody(rw);
		
		CompositeChange change = (CompositeChange) rw.rewriteAST();
		Change include = getIncludeChange();
		change.add(include);
		return change;
	}

	private Change getIncludeChange() {
		IASTTranslationUnit tu = testRunner.getTranslationUnit();
		IPath implPath = new Path(tu.getContainingFilename());
		IFile file= ResourceLookup.selectFileForLocation(implPath, null);
		TextFileChange change = new TextFileChange("include", file); //$NON-NLS-1$
		int offset = getMaxIncludeOffset(tu) + 1;
		String text = "#include \"" + suiteName + ".h\"\n";  //$NON-NLS-1$//$NON-NLS-2$
		TextEdit edit = new InsertEdit(offset, text);
		change.setEdit(edit);
		return change;
	}

	private int getMaxIncludeOffset(IASTTranslationUnit tu) {
		IASTPreprocessorIncludeStatement[] ppStmt = tu.getIncludeDirectives();
		int offset = 0;
		for (IASTPreprocessorIncludeStatement statement : ppStmt) {
			IASTFileLocation fileLocation = statement.getFileLocation();
			int end = fileLocation.getNodeOffset()+ fileLocation.getNodeLength();
			if(offset < end) {
				offset = end;
			}
		}
		return offset;
	}

	protected void changeRunnerBody(ASTRewrite rw) {
		IASTStatement makeSuiteStmt = createMakeSuiteStmt();
		rw.insertBefore(testRunner.getBody(), null, makeSuiteStmt, null);
		IASTStatement runnerStmt = createRunnerStmt();
		rw.insertBefore(testRunner.getBody(), null, runnerStmt, null);
	}

	private IASTStatement createRunnerStmt() {
		IASTFunctionCallExpression makeRunnerFuncCallExp = createMakeRunnerFuncCall();
		IASTFunctionCallExpression callRunnerFuncCallExp = createCallRunnerFuncCall(makeRunnerFuncCallExp);
		IASTStatement expStatement = nodeFactory.newExpressionStatement(callRunnerFuncCallExp);
		return expStatement;
	}

	protected IASTFunctionCallExpression createCallRunnerFuncCall(IASTFunctionCallExpression makeRunnerFuncCallExp) {
		IASTInitializerClause[] callArgs = new IASTInitializerClause[2];
		callArgs[0] = nodeFactory.newIdExpression(getSuiteName());
		callArgs[1] = nodeFactory.newLiteralExpression(ICPPASTLiteralExpression.lk_string_literal, STRING+ suiteName + STRING);
		IASTFunctionCallExpression callRunnerFuncCallExp = nodeFactory.newFunctionCallExpression(makeRunnerFuncCallExp, callArgs);
		return callRunnerFuncCallExp;
	}

	protected IASTFunctionCallExpression createMakeRunnerFuncCall() {
		IASTInitializerClause[] makeArgs = new IASTInitializerClause[1];
		ICPPASTQualifiedName cuteMakeRunner = nodeFactory.newQualifiedName();
		cuteMakeRunner.addName(nodeFactory.newName(CUTE));
		cuteMakeRunner.addName(nodeFactory.newName("makeRunner".toCharArray())); //$NON-NLS-1$
		IASTIdExpression makeRunnerID = nodeFactory.newIdExpression(cuteMakeRunner);
		makeArgs[0] = nodeFactory.newIdExpression(getListenerName());
		IASTFunctionCallExpression makeRunnerFuncCallExp = nodeFactory.newFunctionCallExpression(makeRunnerID, makeArgs);
		return makeRunnerFuncCallExp;
	}

	private IASTName getListenerName() {
		ListenerFinder finder = new ListenerFinder();
		testRunner.getBody().accept(finder);
		return finder.listener != null ? finder.listener.copy() : nodeFactory.newName();
	}

	private IASTStatement createMakeSuiteStmt() {
		ICPPASTQualifiedName cuteSuite = nodeFactory.newQualifiedName();
		cuteSuite.addName(nodeFactory.newName(CUTE));
		cuteSuite.addName(nodeFactory.newName("suite".toCharArray())); //$NON-NLS-1$
		IASTDeclSpecifier declSpecifier = nodeFactory.newTypedefNameSpecifier(cuteSuite);
		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(declSpecifier);
		
		IASTName suiteASTName = getSuiteName();
		ICPPASTDeclarator declarator = nodeFactory.newDeclarator(suiteASTName);
		IASTName makeName = nodeFactory.newName(("make_suite_" + this.suiteName).toCharArray()); //$NON-NLS-1$
		IASTIdExpression idExpr = nodeFactory.newIdExpression(makeName);
		IASTInitializerClause initClause = nodeFactory.newFunctionCallExpression(idExpr,IASTExpression.EMPTY_EXPRESSION_ARRAY);
		IASTEqualsInitializer initializer = nodeFactory.newEqualsInitializer(initClause);
		declarator.setInitializer(initializer);
		declaration.addDeclarator(declarator);
		
		IASTDeclarationStatement declStmt = nodeFactory.newDeclarationStatement(declaration);
		return declStmt;
	}

	protected IASTName getSuiteName() {
		return nodeFactory.newName(this.suiteName.toCharArray());
	}

}

final class ListenerFinder extends CPPASTVisitor {
	IASTName listener = null;
	{
		shouldVisitStatements = true;
	}

	@Override
	public int visit(IASTStatement statement) {
		if (statement instanceof IASTDeclarationStatement) {
			IASTDeclarationStatement declStmt = (IASTDeclarationStatement) statement;
			if (declStmt.getDeclaration() instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simpDecl = (IASTSimpleDeclaration) declStmt.getDeclaration();
				if (simpDecl.getDeclSpecifier() instanceof ICPPASTNamedTypeSpecifier) {
					ICPPASTNamedTypeSpecifier typeName = (ICPPASTNamedTypeSpecifier) simpDecl.getDeclSpecifier();
					if(typeName.getName().toString().equals("cute::ide_listener")) { //$NON-NLS-1$
						listener = simpDecl.getDeclarators()[0].getName();
					}
				}
			}
		}
		return ASTVisitor.PROCESS_CONTINUE;
	}
}