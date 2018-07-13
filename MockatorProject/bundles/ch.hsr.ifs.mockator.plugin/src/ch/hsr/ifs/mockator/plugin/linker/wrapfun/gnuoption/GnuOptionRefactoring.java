package ch.hsr.ifs.mockator.plugin.linker.wrapfun.gnuoption;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.cpp.core.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.core.util.constants.CommonCPPConstants;
import ch.hsr.ifs.iltis.cpp.core.wrappers.CPPVisitor;
import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.PlatformUtil;
import ch.hsr.ifs.mockator.plugin.linker.ItaniumMangledNameGenerator;
import ch.hsr.ifs.mockator.plugin.linker.LinkerRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.FunctionDelegateCallCreator;


@SuppressWarnings("restriction")
public class GnuOptionRefactoring extends LinkerRefactoring {

   private static final String REAL_PREFIX = "__real_";
   private static final String WRAP_PREFIX = "__wrap_";
   private static final String C_LINKAGE   = "\"C\"";
   private static String       NEW_LINE    = PlatformUtil.toSystemNewLine("%n");
   private String              newFunName;

   public GnuOptionRefactoring(final ICElement element, final Optional<ITextSelection> selection, final ICProject cproject) {
      super(element, selection, cproject);
   }

   @Override
   protected void createLinkerSeamSupport(final ModificationCollector collector, final IASTName funName, final IProgressMonitor pm)
         throws CoreException {
      rememberFunName(funName);
      createWrapperFunction(funName, collector.rewriterForTranslationUnit(getAST(tu, pm)), pm);
   }

   private void rememberFunName(final IASTName funName) {
      final IBinding binding = funName.resolveBinding();
      ILTISException.Unless.assignableFrom("Function expected", ICPPFunction.class, binding);
      newFunName = new ItaniumMangledNameGenerator((ICPPFunction) binding).createMangledName();
   }

   private void createWrapperFunction(final IASTName funName, final ASTRewrite rewriter, final IProgressMonitor pm) {
      findFunDeclaration(funName, pm).ifPresent((funDecl) -> {
         final IASTDeclSpecifier declSpecifier = ASTUtil.getDeclSpec(funDecl);
         final IASTSimpleDeclaration realFunDecl = createRealFunDecl(declSpecifier, funDecl);
         final ICPPASTFunctionDefinition wrapFunDef = createWrappedFun(declSpecifier, funDecl, realFunDecl);
         final ICPPASTLinkageSpecification cLinkage = wrapFunctionsInCLinkage(realFunDecl, wrapFunDef);
         insertWrapperFunsInCLinkage(rewriter, cLinkage, funName);
      });
   }

   private IASTSimpleDeclaration createRealFunDecl(final IASTDeclSpecifier declSpec, final ICPPASTFunctionDeclarator function) {
      final ICPPASTFunctionDeclarator newFunDecl = function.copy();
      disableCvQualifiers(newFunDecl);
      adjustParamNamesIfNecessary(newFunDecl);
      setRealFunName(newFunDecl);
      final IASTDeclSpecifier newDeclSpec = getRealDeclSpecBasedOn(declSpec);
      prepareDeclSpecifier(newDeclSpec);
      final IASTSimpleDeclaration realFunDecl = nodeFactory.newSimpleDeclaration(newDeclSpec);
      realFunDecl.addDeclarator(newFunDecl);
      return realFunDecl;
   }

   private static IASTDeclSpecifier getRealDeclSpecBasedOn(final IASTDeclSpecifier declSpec) {
      final IASTDeclSpecifier newDeclSpec = declSpec.copy();
      newDeclSpec.setStorageClass(IASTDeclSpecifier.sc_extern);
      return newDeclSpec;
   }

   private void setRealFunName(final ICPPASTFunctionDeclarator newFunDecl) {
      final IASTName newName = nodeFactory.newName((REAL_PREFIX + newFunName).toCharArray());
      newFunDecl.setName(newName);
   }

   private static ICPPASTLinkageSpecification createCLinkageSpec() {
      return nodeFactory.newLinkageSpecification(C_LINKAGE);
   }

   private ICPPASTFunctionDefinition createWrappedFun(final IASTDeclSpecifier declSpec, final ICPPASTFunctionDeclarator function,
         final IASTSimpleDeclaration realFunDecl) {
      final ICPPASTFunctionDeclarator newFunDecl = function.copy();
      disableCvQualifiers(newFunDecl);
      adjustParamNamesIfNecessary(newFunDecl);
      setWrappedFunName(newFunDecl);
      final IASTCompoundStatement funBody = createWrappedFunReturnStmt(realFunDecl, newFunDecl);
      final IASTDeclSpecifier newDeclSpec = declSpec.copy();
      prepareDeclSpecifier(newDeclSpec);
      ASTUtil.removeExternalStorageIfSet(newDeclSpec);
      return nodeFactory.newFunctionDefinition(newDeclSpec, newFunDecl, funBody);
   }

   private void setWrappedFunName(final ICPPASTFunctionDeclarator newFunDecl) {
      newFunDecl.setName(nodeFactory.newName((WRAP_PREFIX + newFunName).toCharArray()));
   }

   private static void disableCvQualifiers(final ICPPASTFunctionDeclarator newFunDecl) {
      newFunDecl.setConst(false);
      newFunDecl.setVolatile(false);
   }

   private void insertWrapperFunsInCLinkage(final ASTRewrite rewriter, final ICPPASTLinkageSpecification cLinkageSpec, final IASTName funName) {
      final IASTNode insertionPoint = getInsertionPoint(funName);
      rewriter.insertBefore(insertionPoint.getParent(), insertionPoint, createIfDefWrapFun(), null);
      rewriter.insertBefore(insertionPoint.getParent(), insertionPoint, cLinkageSpec, null);
      rewriter.insertBefore(insertionPoint.getParent(), insertionPoint, createEndIf(), null);
   }

   private static ASTLiteralNode createEndIf() {
      return new ASTLiteralNode(CommonCPPConstants.END_IF_DIRECTIVE + NEW_LINE);
   }

   private ASTLiteralNode createIfDefWrapFun() {
      return new ASTLiteralNode(CommonCPPConstants.IFDEF_DIRECTIVE + MockatorConstants.SPACE + MockatorConstants.WRAP_MACRO_PREFIX + newFunName +
                                NEW_LINE);
   }

   private static ICPPASTLinkageSpecification wrapFunctionsInCLinkage(final IASTSimpleDeclaration realFunDecl,
         final ICPPASTFunctionDefinition wrapFunDef) {
      final ICPPASTLinkageSpecification cLinkage = createCLinkageSpec();
      cLinkage.addDeclaration(realFunDecl);
      cLinkage.addDeclaration(wrapFunDef);
      return cLinkage;
   }

   private static IASTNode getInsertionPoint(final IASTName funName) {
      IASTNode insertionPoint = CPPVisitor.findAncestorWithType(funName, ICPPASTCompositeTypeSpecifier.class).orElse(null);

      if (insertionPoint != null) { return insertionPoint.getParent(); }

      insertionPoint = CPPVisitor.findAncestorWithType(funName, ICPPASTFunctionDefinition.class).orElse(null);

      if (insertionPoint != null) { return insertionPoint; }

      return CPPVisitor.findAncestorWithType(funName, IASTStatement.class).orElse(null);
   }

   private static IASTCompoundStatement createWrappedFunReturnStmt(final IASTSimpleDeclaration realFunDecl,
         final ICPPASTFunctionDeclarator wrapFunDecl) {
      final FunctionDelegateCallCreator creator = new FunctionDelegateCallCreator(wrapFunDecl);
      final ICPPASTFunctionDeclarator funDecl = CPPVisitor.findChildWithType(realFunDecl, ICPPASTFunctionDeclarator.class).orElse(null);
      final IASTStatement delegateToRealFun = creator.createDelegate(funDecl.getName(), realFunDecl.getDeclSpecifier());
      return ASTUtil.toCompoundStatement(delegateToRealFun);
   }

   String getNewFunName() {
      return newFunName;
   }

   @Override
   public String getDescription() {
      return I18N.GnuOptionRefactoringDesc;
   }

   private static void prepareDeclSpecifier(final IASTDeclSpecifier declSpec) {
      if (!(declSpec instanceof ICPPASTSimpleDeclSpecifier)) { return; }

      final ICPPASTSimpleDeclSpecifier simpleDeclSpec = (ICPPASTSimpleDeclSpecifier) declSpec;
      simpleDeclSpec.setExplicit(false);

      if (ASTUtil.isUnspecified(simpleDeclSpec)) {
         simpleDeclSpec.setType(IASTSimpleDeclSpecifier.t_void);
      }
   }
}
