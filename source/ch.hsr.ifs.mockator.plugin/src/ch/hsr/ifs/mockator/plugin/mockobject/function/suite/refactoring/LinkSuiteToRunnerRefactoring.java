/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil, Switzerland,
 * http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any purpose without fee is hereby
 * granted, provided that the above copyright notice and this permission notice appear in all
 * copies.
 ******************************************************************************/
package ch.hsr.ifs.mockator.plugin.mockobject.function.suite.refactoring;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.CppIncludeResolver;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;

// Taken and adapted from CUTE
@SuppressWarnings("restriction")
public class LinkSuiteToRunnerRefactoring extends MockatorRefactoring {
  private static final String MAKE_SUITE_PREFIX = "make_suite_";
  private static final String MAKE_RUNNER = "makeRunner";
  private static final String CUTE_ID_LISTENER = "cute::ide_listener";
  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private IASTFunctionDefinition testRunner;
  private String suiteName;
  private IPath destinationPath;

  public LinkSuiteToRunnerRefactoring(ICElement cElement, ITextSelection selection,
      ICProject cProject) {
    super(cElement, selection, cProject);
  }

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
    RefactoringStatus status = super.checkInitialConditions(pm);

    if (testRunner == null) {
      status.addFatalError("Was not able to determine test runner");
    }

    return status;
  }

  @Override
  protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
      throws CoreException, OperationCanceledException {
    ASTRewrite rewriter = createRewriter(collector, testRunner.getTranslationUnit());
    changeRunnerBody(rewriter);
    addIncludeForSuite(rewriter);
  }

  private void changeRunnerBody(ASTRewrite rewriter) {
    IASTStatement makeSuiteStmt = createMakeSuiteStmt();
    rewriter.insertBefore(testRunner.getBody(), null, makeSuiteStmt, null);
    IASTStatement runnerStmt = createRunnerStmt();
    rewriter.insertBefore(testRunner.getBody(), null, runnerStmt, null);
  }

  private IASTStatement createMakeSuiteStmt() {
    ICPPASTQualifiedName cuteSuite = nodeFactory.newQualifiedName();
    cuteSuite.addName(nodeFactory.newName(MockatorConstants.CUTE_NS.toCharArray()));
    cuteSuite.addName(nodeFactory.newName(MockatorConstants.CUTE_SUITE.toCharArray()));
    IASTDeclSpecifier declSpecifier = nodeFactory.newTypedefNameSpecifier(cuteSuite);
    IASTSimpleDeclaration declaration = nodeFactory.newSimpleDeclaration(declSpecifier);
    ICPPASTDeclarator declarator = nodeFactory.newDeclarator(createSuiteName());
    IASTName makeName = nodeFactory.newName((MAKE_SUITE_PREFIX + suiteName).toCharArray());
    IASTIdExpression idExpr = nodeFactory.newIdExpression(makeName);
    IASTInitializerClause initClause =
        nodeFactory.newFunctionCallExpression(idExpr, IASTExpression.EMPTY_EXPRESSION_ARRAY);
    IASTEqualsInitializer initializer = nodeFactory.newEqualsInitializer(initClause);
    declarator.setInitializer(initializer);
    declaration.addDeclarator(declarator);
    return nodeFactory.newDeclarationStatement(declaration);
  }

  private IASTName createSuiteName() {
    return nodeFactory.newName(suiteName.toCharArray());
  }

  private IASTStatement createRunnerStmt() {
    IASTFunctionCallExpression makeRunnerFuncCallExp = createMakeRunnerFuncCall();
    IASTFunctionCallExpression callRunnerFuncCallExp =
        createCallRunnerFuncCall(makeRunnerFuncCallExp);
    return nodeFactory.newExpressionStatement(callRunnerFuncCallExp);
  }

  private void addIncludeForSuite(ASTRewrite rewriter) throws OperationCanceledException {
    String suiteInclude = suiteName + MockatorConstants.HEADER_SUFFIX;
    IPath suiteIncludePath = destinationPath.append(suiteInclude);
    String includeAbsPath = FileUtil.toIFile(suiteIncludePath).getLocation().toString();
    IASTTranslationUnit ast = testRunner.getTranslationUnit();
    CppIncludeResolver resolver = new CppIncludeResolver(ast, project, getIndex());
    AstIncludeNode includeForFunDecl = resolver.resolveIncludeNode(includeAbsPath);
    includeForFunDecl.insertInTu(ast, rewriter);
  }

  private IASTFunctionCallExpression createCallRunnerFuncCall(
      IASTFunctionCallExpression makeRunnerFun) {
    IASTInitializerClause[] callArgs = new IASTInitializerClause[2];
    callArgs[0] = nodeFactory.newIdExpression(createSuiteName());
    String suite = StringUtil.quote(suiteName);
    callArgs[1] = nodeFactory.newLiteralExpression(IASTLiteralExpression.lk_string_literal, suite);
    return nodeFactory.newFunctionCallExpression(makeRunnerFun, callArgs);
  }

  private IASTFunctionCallExpression createMakeRunnerFuncCall() {
    IASTInitializerClause[] makeArgs = new IASTInitializerClause[1];
    ICPPASTQualifiedName cuteMakeRunner = nodeFactory.newQualifiedName();
    cuteMakeRunner.addName(nodeFactory.newName(MockatorConstants.CUTE_NS.toCharArray()));
    cuteMakeRunner.addName(nodeFactory.newName(MAKE_RUNNER.toCharArray()));
    IASTIdExpression makeRunnerID = nodeFactory.newIdExpression(cuteMakeRunner);
    makeArgs[0] = nodeFactory.newIdExpression(getListenerName());
    return nodeFactory.newFunctionCallExpression(makeRunnerID, makeArgs);
  }

  private IASTName getListenerName() {
    ListenerFinder finder = new ListenerFinder();
    testRunner.getBody().accept(finder);
    return finder.listener != null ? finder.listener.copy() : nodeFactory.newName();
  }

  public void setTestRunner(IASTFunctionDefinition testRunner) {
    this.testRunner = testRunner;
  }

  public void setSuiteName(String suiteName) {
    this.suiteName = suiteName;
  }

  public void setDestinationPath(IPath destinationPath) {
    this.destinationPath = destinationPath;
  }

  @Override
  public String getDescription() {
    return I18N.LinkSuiteToRunnerRefactoringDesc;
  }

  private static class ListenerFinder extends ASTVisitor {
    IASTName listener = null;
    {
      shouldVisitStatements = true;
    }

    @Override
    public int visit(IASTStatement statement) {
      if (!(statement instanceof IASTDeclarationStatement))
        return PROCESS_CONTINUE;

      IASTDeclarationStatement declStmt = (IASTDeclarationStatement) statement;

      if (!(declStmt.getDeclaration() instanceof IASTSimpleDeclaration))
        return PROCESS_CONTINUE;

      IASTSimpleDeclaration simpDecl = (IASTSimpleDeclaration) declStmt.getDeclaration();

      if (simpDecl.getDeclSpecifier() instanceof ICPPASTNamedTypeSpecifier) {
        ICPPASTNamedTypeSpecifier typeName =
            (ICPPASTNamedTypeSpecifier) simpDecl.getDeclSpecifier();

        if (typeName.getName().toString().equals(CUTE_ID_LISTENER)) {
          listener = simpDecl.getDeclarators()[0].getName();
        }
      }
      return PROCESS_CONTINUE;
    }
  }
}
