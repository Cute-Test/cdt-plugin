package ch.hsr.ifs.mockator.plugin.linker;

import java.net.URI;

import org.eclipse.cdt.core.dom.ast.ASTNodeFactoryFactory;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.core.resources.IFile;

import ch.hsr.ifs.iltis.core.resources.FileUtil;
import ch.hsr.ifs.iltis.core.resources.ProjectUtil;
import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.resources.CProjectUtil;
import ch.hsr.ifs.iltis.cpp.wrappers.ModificationCollector;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterSignatureHandler;


public class WeakDeclAdder {

   private static final ICPPNodeFactory nodeFactory    = ASTNodeFactoryFactory.getDefaultCPPNodeFactory();
   private static final String          WEAK_DECL_ATTR = "__attribute__((weak))";
   private final ModificationCollector  collector;

   public WeakDeclAdder(final ModificationCollector collector) {
      this.collector = collector;
   }

   public void addWeakDeclAttribute(final ICPPASTFunctionDeclarator funDecl) {
      final IASTTranslationUnit tu = funDecl.getTranslationUnit();

      if (!isTuPartOfWorkspace(tu)) {
         return;
      }

      final ICPPASTNamedTypeSpecifier withWeakDeclSpec = createWeakDeclSpec(funDecl);
      final IASTNode funDeclParentCopy = getWeakParentCopy(funDecl, withWeakDeclSpec);
      final ASTRewrite rewriter = collector.rewriterForTranslationUnit(tu);
      rewriter.replace(funDecl.getParent(), funDeclParentCopy, null);
   }

   private static boolean isTuPartOfWorkspace(final IASTTranslationUnit tuOfFunDef) {
      final URI uriOfTu = FileUtil.stringToUri(tuOfFunDef.getFilePath());
      final IFile[] files = ProjectUtil.getWorkspaceRoot().findFilesForLocationURI(uriOfTu);
      return files.length > 0;
   }

   private static IASTNode getWeakParentCopy(final ICPPASTFunctionDeclarator funDecl, final ICPPASTNamedTypeSpecifier withWeakDeclSpec) {
      final IASTNode copy = funDecl.getParent().copy(CopyStyle.withLocations);

      if (funDecl.getParent() instanceof ICPPASTFunctionDefinition) {
         ((ICPPASTFunctionDefinition) copy).setDeclSpecifier(withWeakDeclSpec);
      } else if (funDecl.getParent() instanceof IASTSimpleDeclaration) {
         ((IASTSimpleDeclaration) copy).setDeclSpecifier(withWeakDeclSpec);
      }
      return copy;
   }

   private static ICPPASTNamedTypeSpecifier createWeakDeclSpec(final ICPPASTFunctionDeclarator funDecl) {
      final ICPPASTDeclSpecifier declSpec = ASTUtil.getDeclSpec(funDecl);
      final String returnTypeSpec = getStringRepresentation(declSpec);
      final String weakDecl = WEAK_DECL_ATTR + " " + returnTypeSpec;
      final IASTName weakDeclSpec = nodeFactory.newName(weakDecl.toCharArray());
      return nodeFactory.newTypedefNameSpecifier(weakDeclSpec);
   }

   private static String getStringRepresentation(final ICPPASTDeclSpecifier declSpec) {
      final StringBuilder sb = new StringBuilder();
      ParameterSignatureHandler.appendDeclSpecifierString(sb, declSpec);
      return sb.toString();
   }
}
