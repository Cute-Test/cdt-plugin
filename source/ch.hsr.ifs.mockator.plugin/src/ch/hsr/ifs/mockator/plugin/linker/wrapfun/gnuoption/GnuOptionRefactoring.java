package ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTLiteralNode;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.PlatformUtil;
import ch.hsr.ifs.mockator.plugin.linker.ItaniumMangledNameGenerator;
import ch.hsr.ifs.mockator.plugin.linker.LinkerRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionDelegateCallCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
public class GnuOptionRefactoring extends LinkerRefactoring {
  private static final String REAL_PREFIX = "__real_";
  private static final String WRAP_PREFIX = "__wrap_";
  private static final String C_LINKAGE = "\"C\"";
  private static String NEW_LINE = PlatformUtil.toSystemNewLine("%n");
  private String newFunName;

  public GnuOptionRefactoring(ICElement element, ITextSelection selection, ICProject cproject) {
    super(element, selection, cproject);
  }

  @Override
  protected void createLinkerSeamSupport(ModificationCollector collector, IASTName funName,
      IProgressMonitor pm) throws CoreException {
    rememberFunName(funName);
    createWrapperFunction(funName, collector, getAST(tu, pm), pm);
  }

  private void rememberFunName(IASTName funName) {
    IBinding binding = funName.resolveBinding();
    Assert.instanceOf(binding, ICPPFunction.class, "Function expected");
    newFunName = new ItaniumMangledNameGenerator((ICPPFunction) binding).createMangledName();
  }

  private void createWrapperFunction(IASTName funName, ModificationCollector collector,
      IASTTranslationUnit tu, IProgressMonitor pm) {
    for (ICPPASTFunctionDeclarator optFunDecl : findFunDeclaration(funName, pm)) {
      IASTDeclSpecifier declSpecifier = AstUtil.getDeclSpec(optFunDecl);
      IASTSimpleDeclaration realFunDecl = createRealFunDecl(declSpecifier, optFunDecl);
      ICPPASTFunctionDefinition wrapFunDef =
          createWrappedFun(declSpecifier, optFunDecl, realFunDecl);
      ICPPASTLinkageSpecification cLinkage = wrapFunctionsInCLinkage(realFunDecl, wrapFunDef);
      insertWrapperFunsInCLinkage(collector, tu, cLinkage, funName);
    }
  }

  private IASTSimpleDeclaration createRealFunDecl(IASTDeclSpecifier declSpec,
      ICPPASTFunctionDeclarator function) {
    ICPPASTFunctionDeclarator newFunDecl = function.copy();
    disableCvQualifiers(newFunDecl);
    adjustParamNamesIfNecessary(newFunDecl);
    setRealFunName(newFunDecl);
    IASTDeclSpecifier newDeclSpec = getRealDeclSpecBasedOn(declSpec);
    prepareDeclSpecifier(newDeclSpec);
    IASTSimpleDeclaration realFunDecl = nodeFactory.newSimpleDeclaration(newDeclSpec);
    realFunDecl.addDeclarator(newFunDecl);
    return realFunDecl;
  }

  private static IASTDeclSpecifier getRealDeclSpecBasedOn(IASTDeclSpecifier declSpec) {
    IASTDeclSpecifier newDeclSpec = declSpec.copy();
    newDeclSpec.setStorageClass(IASTDeclSpecifier.sc_extern);
    return newDeclSpec;
  }

  private void setRealFunName(ICPPASTFunctionDeclarator newFunDecl) {
    IASTName newName = nodeFactory.newName((REAL_PREFIX + newFunName).toCharArray());
    newFunDecl.setName(newName);
  }

  private static ICPPASTLinkageSpecification createCLinkageSpec() {
    return nodeFactory.newLinkageSpecification(C_LINKAGE);
  }

  private ICPPASTFunctionDefinition createWrappedFun(IASTDeclSpecifier declSpec,
      ICPPASTFunctionDeclarator function, IASTSimpleDeclaration realFunDecl) {
    ICPPASTFunctionDeclarator newFunDecl = function.copy();
    disableCvQualifiers(newFunDecl);
    adjustParamNamesIfNecessary(newFunDecl);
    setWrappedFunName(newFunDecl);
    IASTCompoundStatement funBody = createWrappedFunReturnStmt(realFunDecl, newFunDecl);
    IASTDeclSpecifier newDeclSpec = declSpec.copy();
    prepareDeclSpecifier(newDeclSpec);
    AstUtil.removeExternalStorageIfSet(newDeclSpec);
    return nodeFactory.newFunctionDefinition(newDeclSpec, newFunDecl, funBody);
  }

  private void setWrappedFunName(ICPPASTFunctionDeclarator newFunDecl) {
    newFunDecl.setName(nodeFactory.newName((WRAP_PREFIX + newFunName).toCharArray()));
  }

  private static void disableCvQualifiers(ICPPASTFunctionDeclarator newFunDecl) {
    newFunDecl.setConst(false);
    newFunDecl.setVolatile(false);
  }

  private void insertWrapperFunsInCLinkage(ModificationCollector collector,
      IASTTranslationUnit ast, ICPPASTLinkageSpecification cLinkageSpec, IASTName funName) {
    ASTRewrite rewriter = createRewriter(collector, ast);
    IASTNode insertionPoint = getInsertionPoint(funName);
    rewriter.insertBefore(insertionPoint.getParent(), insertionPoint, createIfDefWrapFun(), null);
    rewriter.insertBefore(insertionPoint.getParent(), insertionPoint, cLinkageSpec, null);
    rewriter.insertBefore(insertionPoint.getParent(), insertionPoint, createEndIf(), null);
  }

  private static ASTLiteralNode createEndIf() {
    return new ASTLiteralNode(MockatorConstants.END_IF_DIRECTIVE + NEW_LINE);
  }

  private ASTLiteralNode createIfDefWrapFun() {
    return new ASTLiteralNode(MockatorConstants.IFDEF_DIRECTIVE + MockatorConstants.SPACE
        + MockatorConstants.WRAP_MACRO_PREFIX + newFunName + NEW_LINE);
  }

  private static ICPPASTLinkageSpecification wrapFunctionsInCLinkage(
      IASTSimpleDeclaration realFunDecl, ICPPASTFunctionDefinition wrapFunDef) {
    ICPPASTLinkageSpecification cLinkage = createCLinkageSpec();
    cLinkage.addDeclaration(realFunDecl);
    cLinkage.addDeclaration(wrapFunDef);
    return cLinkage;
  }

  private static IASTNode getInsertionPoint(IASTName funName) {
    IASTNode insertionPoint =
        AstUtil.getAncestorOfType(funName, ICPPASTCompositeTypeSpecifier.class);

    if (insertionPoint != null)
      return insertionPoint.getParent();

    insertionPoint = AstUtil.getAncestorOfType(funName, ICPPASTFunctionDefinition.class);

    if (insertionPoint != null)
      return insertionPoint;

    return AstUtil.getAncestorOfType(funName, IASTStatement.class);
  }

  private static IASTCompoundStatement createWrappedFunReturnStmt(
      IASTSimpleDeclaration realFunDecl, ICPPASTFunctionDeclarator wrapFunDecl) {
    FunctionDelegateCallCreator creator = new FunctionDelegateCallCreator(wrapFunDecl);
    ICPPASTFunctionDeclarator funDecl =
        AstUtil.getChildOfType(realFunDecl, ICPPASTFunctionDeclarator.class);
    IASTStatement delegateToRealFun =
        creator.createDelegate(funDecl.getName(), realFunDecl.getDeclSpecifier());
    return AstUtil.toCompoundStatement(delegateToRealFun);
  }

  String getNewFunName() {
    return newFunName;
  }

  @Override
  public String getDescription() {
    return I18N.GnuOptionRefactoringDesc;
  }

  private static void prepareDeclSpecifier(IASTDeclSpecifier declSpec) {
    if (!(declSpec instanceof ICPPASTSimpleDeclSpecifier))
      return;

    ICPPASTSimpleDeclSpecifier simpleDeclSpec = (ICPPASTSimpleDeclSpecifier) declSpec;
    simpleDeclSpec.setExplicit(false);

    if (AstUtil.isUnspecified(simpleDeclSpec)) {
      simpleDeclSpec.setType(IASTSimpleDeclSpecifier.t_void);
    }
  }
}
