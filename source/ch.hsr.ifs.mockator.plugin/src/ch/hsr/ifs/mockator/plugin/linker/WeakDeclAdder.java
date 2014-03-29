package ch.hsr.ifs.mockator.plugin.linker;

import java.net.URI;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.resources.IFile;

import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.base.util.ProjectUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterSignatureHandler;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;

@SuppressWarnings("restriction")
public class WeakDeclAdder {
  private static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();
  private static final String WEAK_DECL_ATTR = "__attribute__((weak))";
  private final ModificationCollector collector;

  public WeakDeclAdder(ModificationCollector collector) {
    this.collector = collector;
  }

  public void addWeakDeclAttribute(ICPPASTFunctionDeclarator funDecl) {
    IASTTranslationUnit tu = funDecl.getTranslationUnit();

    if (!isTuPartOfWorkspace(tu)) // otherwise we cannot adapt its function declaration
      return;

    ICPPASTNamedTypeSpecifier withWeakDeclSpec = createWeakDeclSpec(funDecl);
    IASTNode funDeclParentCopy = getWeakParentCopy(funDecl, withWeakDeclSpec);
    ASTRewrite rewriter = collector.rewriterForTranslationUnit(tu);
    rewriter.replace(funDecl.getParent(), funDeclParentCopy, null);
  }

  private static boolean isTuPartOfWorkspace(IASTTranslationUnit tuOfFunDef) {
    URI uriOfTu = FileUtil.stringToUri(tuOfFunDef.getFilePath());
    IFile[] files = ProjectUtil.getWorkspaceRoot().findFilesForLocationURI(uriOfTu);
    return files.length > 0;
  }

  private static IASTNode getWeakParentCopy(ICPPASTFunctionDeclarator funDecl,
      ICPPASTNamedTypeSpecifier withWeakDeclSpec) {
    IASTNode copy = funDecl.getParent().copy(CopyStyle.withLocations);

    if (funDecl.getParent() instanceof ICPPASTFunctionDefinition) {
      ((ICPPASTFunctionDefinition) copy).setDeclSpecifier(withWeakDeclSpec);
    } else if (funDecl.getParent() instanceof IASTSimpleDeclaration) {
      ((IASTSimpleDeclaration) copy).setDeclSpecifier(withWeakDeclSpec);
    }
    return copy;
  }

  private static ICPPASTNamedTypeSpecifier createWeakDeclSpec(ICPPASTFunctionDeclarator funDecl) {
    ICPPASTDeclSpecifier declSpec = AstUtil.getDeclSpec(funDecl);
    String returnTypeSpec = getStringRepresentation(declSpec);
    String weakDecl = WEAK_DECL_ATTR + " " + returnTypeSpec;
    IASTName weakDeclSpec = nodeFactory.newName(weakDecl.toCharArray());
    return nodeFactory.newTypedefNameSpecifier(weakDeclSpec);
  }

  private static String getStringRepresentation(ICPPASTDeclSpecifier declSpec) {
    StringBuilder sb = new StringBuilder();
    ParameterSignatureHandler.appendDeclSpecifierString(sb, declSpec);
    return sb.toString();
  }
}
