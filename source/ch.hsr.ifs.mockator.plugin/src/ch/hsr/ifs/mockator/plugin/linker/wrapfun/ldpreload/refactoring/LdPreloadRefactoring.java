package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.refactoring;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.base.util.PathProposalUtil;
import ch.hsr.ifs.mockator.plugin.linker.LinkerRefactoring;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.CppIncludeResolver;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitCreator;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.QualifiedNameCreator;

@SuppressWarnings("restriction")
public class LdPreloadRefactoring extends LinkerRefactoring {
  private final CppStandard cppStd;
  private final IProject targetProject;
  private IFile newFile;

  public LdPreloadRefactoring(CppStandard cppStd, ICElement element, ITextSelection selection,
      ICProject cProject, IProject targetProject) {
    super(element, selection, cProject);
    this.cppStd = cppStd;
    this.targetProject = targetProject;
  }

  @Override
  protected void createLinkerSeamSupport(ModificationCollector collector, IASTName funName,
      IProgressMonitor pm) throws CoreException {
    for (ICPPASTFunctionDeclarator optFunDecl : findFunDeclaration(funName, pm)) {
      IASTTranslationUnit newTu = createAndGetNewTu(funName.toString(), pm);
      ASTRewrite rewriter = createRewriter(collector, newTu);
      insertFunDeclInclude(optFunDecl, newTu, rewriter);
      insertSupportingIncludes(optFunDecl, newTu, rewriter);
      insertFunDefinition(createFunDefinition(optFunDecl), newTu, rewriter);
    }
  }

  private static void insertSupportingIncludes(ICPPASTFunctionDeclarator funDecl,
      IASTTranslationUnit tu, ASTRewrite rewriter) {
    AstIncludeNode dlfcn = new AstIncludeNode("dlfcn.h", true);
    dlfcn.insertInTu(tu, rewriter);

    if (isMemberFunction(funDecl)) {
      AstIncludeNode cstring = new AstIncludeNode("cstring", true);
      cstring.insertInTu(tu, rewriter);
    }
  }

  private static boolean isMemberFunction(ICPPASTFunctionDeclarator funDecl) {
    return AstUtil.getAncestorOfType(funDecl, ICPPASTCompositeTypeSpecifier.class) != null;
  }

  private void insertFunDeclInclude(ICPPASTFunctionDeclarator funDecl, IASTTranslationUnit tu,
      ASTRewrite rewriter) {
    try {
      CppIncludeResolver resolver = new CppIncludeResolver(tu, project, getIndex());
      AstIncludeNode includeForFunDecl =
          resolver.resolveIncludeNode(funDecl.getTranslationUnit().getFilePath());
      rewriter.insertBefore(tu, null, includeForFunDecl, null);
    } catch (Exception e) {
      throw new MockatorException(e);
    }
  }

  private static void insertFunDefinition(ICPPASTFunctionDefinition funDef,
      IASTTranslationUnit newTu, ASTRewrite rewriter) {
    rewriter.insertBefore(newTu, null, funDef, null);
  }

  private IASTTranslationUnit createAndGetNewTu(String funName, IProgressMonitor pm)
      throws CoreException {
    IPath newLocation = getPathForNewFile(funName);
    newFile = FileUtil.toIFile(newLocation);
    TranslationUnitCreator creator = new TranslationUnitCreator(targetProject, refactoringContext);
    return creator.createAndGetNewTu(newLocation, pm);
  }

  private IPath getPathForNewFile(String funName) {
    PathProposalUtil proposal = new PathProposalUtil(targetProject.getFullPath());
    return proposal.getUniquePathForNewFile(funName, MockatorConstants.SOURCE_SUFFIX);
  }

  private ICPPASTFunctionDefinition createFunDefinition(ICPPASTFunctionDeclarator funDecl) {
    ICPPASTDeclSpecifier newDeclSpec = getNewFunDeclSpec(funDecl);
    AstUtil.removeExternalStorageIfSet(newDeclSpec);
    ICPPASTFunctionDeclarator newFunDecl = funDecl.copy();
    adjustParamNamesIfNecessary(newFunDecl);
    newFunDecl.setName(getFullyQualifiedName(funDecl));
    IASTCompoundStatement funBody = getFunBody(funDecl);
    return nodeFactory.newFunctionDefinition(newDeclSpec, newFunDecl, funBody);
  }

  private static ICPPASTDeclSpecifier getNewFunDeclSpec(ICPPASTFunctionDeclarator funDecl) {
    return AstUtil.getDeclSpec(funDecl).copy();
  }

  private IASTCompoundStatement getFunBody(ICPPASTFunctionDeclarator funDecl) {
    LdPreloadFunBodyFactory factory = new LdPreloadFunBodyFactory();
    LdPreloadFunBodyStrategy funBodyStrategy = factory.getFunBodyStrategy(funDecl);
    return funBodyStrategy.getPreloadFunBody(cppStd, funDecl);
  }

  private static IASTName getFullyQualifiedName(ICPPASTFunctionDeclarator funDecl) {
    QualifiedNameCreator resolver = new QualifiedNameCreator(funDecl.getName());
    ICPPASTQualifiedName qualifiedName = resolver.createQualifiedName();
    qualifiedName.addName(funDecl.getName().copy());
    return qualifiedName;
  }

  public IFile getNewFile() {
    return newFile;
  }

  @Override
  public String getDescription() {
    return I18N.LdPreloadRefactoringDesc;
  }
}
