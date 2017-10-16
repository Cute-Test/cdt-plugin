/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

import com.cevelop.elevenator.definition.CPPVersion;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
 * @since 4.0
 * 
 */
public class LinkSuiteToRunnerProcessor {

	private static final String STRING = "\"";
	private static final char[] CUTE = "cute".toCharArray();
	private final IASTFunctionDefinition testRunner;
	private final String suiteName;
	private final ICPPNodeFactory nodeFactory;

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
		IFile file = ResourceLookup.selectFileForLocation(implPath, null);
		TextFileChange change = new TextFileChange("include", file);
		IDocumentProvider provider = new TextFileDocumentProvider();
		IDocument document = null;
	    try { 
	        provider.connect(file); 
	        document = provider.getDocument(file);             
	    } catch (CoreException e) {
	    	e.printStackTrace();
	    }	
		String lineDelim = TextUtilities.getDefaultLineDelimiter(document);
		int offset = getMaxIncludeOffset(tu);
		String text = lineDelim + "#include \"" + suiteName + ".h\"";//$NON-NLS-2$
		TextEdit edit = new InsertEdit(offset, text);
		change.setEdit(edit);
		return change;
	}

	private int getMaxIncludeOffset(IASTTranslationUnit tu) {
		IASTPreprocessorIncludeStatement[] ppStmt = tu.getIncludeDirectives();
		int offset = 0;
		for (IASTPreprocessorIncludeStatement statement : ppStmt) {
			IASTFileLocation fileLocation = statement.getFileLocation();
			int end = fileLocation.getNodeOffset() + fileLocation.getNodeLength();
			if (offset < end) {
				offset = end;
			}
		}
		return offset;
	}

	protected void changeRunnerBody(ASTRewrite rw) {
		ICProject cProject = testRunner.getTranslationUnit().getOriginatingTranslationUnit().getCProject();
		IProject project = cProject.getProject();
		if(isCPPVersionAboveOrEqualEleven(project)) {
			IASTStatement makeNewSuiteStatement = createMakeSuiteStmt();
			IASTStatement insertionStatement = testRunner.getBody();
			IASTNode insertionPoint = insertionStatement.getChildren()[insertionStatement.getChildren().length-1];
			rw.insertBefore(insertionStatement, insertionPoint, makeNewSuiteStatement, null);
			IASTStatement newRunnerStatement = createRunnerCallStmt(cProject);
			rw.insertBefore(insertionStatement, insertionPoint, newRunnerStatement, null);
		} else {
			IASTStatement makeSuiteStmt = createMakeSuiteStmt();
			rw.insertBefore(testRunner.getBody(), null, makeSuiteStmt, null);
			IASTStatement runnerStmt = createRunnerStmt();
			rw.insertBefore(testRunner.getBody(), null, runnerStmt, null);
		}
	}
	
	private IASTStatement createRunnerCallStmt(ICProject project) {
		IASTName makeRunnerName = getMakeRunnerName();
		IASTIdExpression successExp = nodeFactory.newIdExpression(getBoolName(makeRunnerName));
		IASTIdExpression runnerExp = nodeFactory.newIdExpression(makeRunnerName);
		IASTInitializerClause[] makeArgs = new IASTInitializerClause[2];
		makeArgs[0] = nodeFactory.newIdExpression(nodeFactory.newName(suiteName));
		makeArgs[1] = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal, STRING + suiteName + STRING);
		IASTFunctionCallExpression runnerCallExp = nodeFactory.newFunctionCallExpression(runnerExp, makeArgs);
		IASTBinaryExpression andAssignExp = nodeFactory.newBinaryExpression(IASTBinaryExpression.op_binaryAndAssign, successExp, runnerCallExp);
		IASTStatement statement = nodeFactory.newExpressionStatement(andAssignExp);
		return statement;
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
		callArgs[1] = nodeFactory.newLiteralExpression(ICPPASTLiteralExpression.lk_string_literal, STRING + suiteName + STRING);
		IASTFunctionCallExpression callRunnerFuncCallExp = nodeFactory.newFunctionCallExpression(makeRunnerFuncCallExp, callArgs);
		return callRunnerFuncCallExp;
	}

	protected IASTFunctionCallExpression createMakeRunnerFuncCall() {
		IASTParameterDeclaration[] surroundingArgs = ((ICPPASTFunctionDeclarator)testRunner.getDeclarator()).getParameters();
		IASTInitializerClause[] makeArgs = new IASTInitializerClause[surroundingArgs.length + 1];
		ICPPASTQualifiedName cuteMakeRunner = nodeFactory.newQualifiedName(nodeFactory.newName("makeRunner".toCharArray()));
		cuteMakeRunner.addNameSpecifier(nodeFactory.newName(CUTE));
		IASTIdExpression makeRunnerID = nodeFactory.newIdExpression(cuteMakeRunner);
		makeArgs[0] = nodeFactory.newIdExpression(getListenerName());
		if(surroundingArgs.length > 0) {
			for(int i=0; i<surroundingArgs.length; i++) {
				IASTName argName = surroundingArgs[i].getDeclarator().getName().copy();
				makeArgs[i+1] = nodeFactory.newIdExpression(argName);
			}
		}
		IASTFunctionCallExpression makeRunnerFuncCallExp = nodeFactory.newFunctionCallExpression(makeRunnerID, makeArgs);
		return makeRunnerFuncCallExp;
	}

	private IASTName getListenerName() {
		ListenerFinder finder = new ListenerFinder();
		testRunner.getBody().accept(finder);
		return finder.listener != null ? finder.listener.copy() : nodeFactory.newName();
	}
	
	private IASTName getMakeRunnerName() {
		MakeRunnerFinder finder = new MakeRunnerFinder();
		testRunner.getBody().accept(finder);
		return finder.makeRunner != null ? finder.makeRunner.copy() : nodeFactory.newName();
	}
	
	private IASTName getBoolName(IASTName makeRunnerName) {
		BoolNameFinder finder = new BoolNameFinder(makeRunnerName);
		testRunner.getBody().accept(finder);
		return finder.boolName != null ? finder.boolName.copy() : nodeFactory.newName();
	}

	private IASTStatement createMakeSuiteStmt() {
		ICPPASTQualifiedName cuteSuite = nodeFactory.newQualifiedName(nodeFactory.newName("suite".toCharArray()));
		cuteSuite.addNameSpecifier(nodeFactory.newName(CUTE));
		IASTDeclSpecifier declSpecifier = nodeFactory.newTypedefNameSpecifier(cuteSuite);
		IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(declSpecifier);
		IASTName suiteASTName = getSuiteName();
		ICPPASTDeclarator declarator = nodeFactory.newDeclarator(suiteASTName);
		IASTName makeName = nodeFactory.newName(("make_suite_" + this.suiteName).toCharArray());
		IASTIdExpression idExpr = nodeFactory.newIdExpression(makeName);
		IASTInitializerClause initClause = nodeFactory.newFunctionCallExpression(idExpr, IASTExpression.EMPTY_EXPRESSION_ARRAY);
        IASTEqualsInitializer initializer = nodeFactory.newEqualsInitializer(initClause);
        declarator.setInitializer(initializer);
		declaration.addDeclarator(declarator);
		IASTDeclarationStatement declStmt = nodeFactory.newDeclarationStatement(declaration);
		return declStmt;
	}

	protected IASTName getSuiteName() {
		return nodeFactory.newName(this.suiteName.toCharArray());
	}
	
	private static CPPVersion getCPPVersion(IProject project) {
		return CPPVersion.getForProject(project);
	}
	
	private static boolean isCPPVersionAboveOrEqualEleven(IProject project) {
		CPPVersion version = getCPPVersion(project);
		if(version != null && !version.toString().equals(CPPVersion.CPP_98.toString())
				&& !version.toString().equals(CPPVersion.CPP_03.toString())) {
			return true;
		}
		return false;
	}

}

final class ListenerFinder extends ASTVisitor {
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
					if (typeName.getName().toString().equals("cute::ide_listener")
							|| typeName.getName().toString().equals("cute::xml_listener<cute::ide_listener<>>")) {
						listener = simpDecl.getDeclarators()[0].getName();
					}
				}
			}
		}
		return ASTVisitor.PROCESS_CONTINUE;
	}
}

final class MakeRunnerFinder extends ASTVisitor {
	IASTName makeRunner = null;
	{
		shouldVisitStatements = true;
	}
	
	@Override
	public int visit(IASTStatement statement) {
		if (statement instanceof IASTDeclarationStatement) {
			IASTDeclarationStatement declStmt = (IASTDeclarationStatement) statement;
			if (declStmt.getDeclaration() instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simpDecl = (IASTSimpleDeclaration) declStmt.getDeclaration();
				if(simpDecl.getDeclarators()[0] instanceof IASTDeclarator) {
					IASTDeclarator declarator = simpDecl.getDeclarators()[0];
					if(declarator.getInitializer() instanceof IASTEqualsInitializer) {
						IASTEqualsInitializer equalInit = (IASTEqualsInitializer) declarator.getInitializer();
						if(equalInit.getInitializerClause() instanceof IASTFunctionCallExpression) {
							IASTFunctionCallExpression funcCallExp = (IASTFunctionCallExpression) equalInit.getInitializerClause();
							if(funcCallExp.getFunctionNameExpression() instanceof IASTIdExpression) {
								IASTIdExpression funcNameExp = (IASTIdExpression) funcCallExp.getFunctionNameExpression();
								if(funcNameExp.getName().toString().equals("cute::makeRunner")) {
									makeRunner = declarator.getName();
								}
							}
						}
					}
				}
			}
		}
		return ASTVisitor.PROCESS_CONTINUE;
	}
}

final class BoolNameFinder extends ASTVisitor {
	IASTName boolName = null;
	IASTName makeRunnerName = null;
	{
		shouldVisitStatements = true;
	}
	
	public BoolNameFinder(IASTName makeRunnerName) {
		this.makeRunnerName = makeRunnerName;
	}
	
	@Override
	public int visit(IASTStatement statement) {
		if (statement instanceof IASTDeclarationStatement) {
			IASTDeclarationStatement declStmt = (IASTDeclarationStatement) statement;
			if (declStmt.getDeclaration() instanceof IASTSimpleDeclaration) {
				IASTSimpleDeclaration simpDecl = (IASTSimpleDeclaration) declStmt.getDeclaration();
				if(simpDecl.getDeclSpecifier() instanceof IASTSimpleDeclSpecifier) {
					IASTSimpleDeclSpecifier simpDeclSpec = (IASTSimpleDeclSpecifier) simpDecl.getDeclSpecifier();
					if(simpDeclSpec.getType() == IASTSimpleDeclSpecifier.t_bool) {
						if(simpDecl.getDeclarators()[0] instanceof IASTDeclarator) {
							IASTDeclarator declarator = simpDecl.getDeclarators()[0];
							if(declarator.getInitializer() instanceof IASTEqualsInitializer) {
								IASTEqualsInitializer eqInit = (IASTEqualsInitializer) declarator.getInitializer();
								if(eqInit.getInitializerClause() instanceof IASTFunctionCallExpression) {
									IASTFunctionCallExpression funcCallExp = (IASTFunctionCallExpression) eqInit.getInitializerClause();
									if(funcCallExp.getFunctionNameExpression() instanceof IASTIdExpression) {
										IASTIdExpression funcNameExp = (IASTIdExpression) funcCallExp.getFunctionNameExpression();
										if(funcNameExp.getName().toString().equals(makeRunnerName.toString())) {
											boolName = declarator.getName();
										}
									}
								}
							}
						}
					}
					
				}
			}
		}
		return ASTVisitor.PROCESS_CONTINUE;
	}
}
