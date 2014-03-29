package ch.hsr.ifs.mockator.plugin.linker.shadowfun;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.Collection;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
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
import ch.hsr.ifs.mockator.plugin.base.util.ProjectUtil;
import ch.hsr.ifs.mockator.plugin.linker.LinkerRefactoring;
import ch.hsr.ifs.mockator.plugin.linker.ReferencingExecutableFinder;
import ch.hsr.ifs.mockator.plugin.linker.WeakDeclAdder;
import ch.hsr.ifs.mockator.plugin.project.cdt.SourceFolderHandler;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.CppIncludeResolver;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitCreator;

@SuppressWarnings("restriction")
public class ShadowFunctionRefactoring extends LinkerRefactoring {
  private static final String SHADOW_FOLDER_NAME = "shadows";
  private IFile newFile;

  public ShadowFunctionRefactoring(ICElement element, ITextSelection selection, ICProject cProject) {
    super(element, selection, cProject);
  }

  @Override
  protected void createLinkerSeamSupport(ModificationCollector collector, IASTName funName,
      IProgressMonitor pm) throws CoreException {
    for (ICPPASTFunctionDeclarator optFunDecl : findFunDeclaration(funName, pm)) {
      for (IProject refProj : getReferencingExecutables()) {
        IASTTranslationUnit newTu = createAndGetNewTu(refProj, funName.toString(), pm);
        ICPPASTFunctionDefinition funDef = createFunDefinition(refProj, optFunDecl);
        ASTRewrite rewriter = createRewriter(collector, newTu);
        ICProject cProject = ProjectUtil.getCProject(refProj);
        insertFunDeclInclude(optFunDecl, newTu, rewriter, cProject);
        insertFunDefinition(funDef, newTu, rewriter);
      }

      setWeakDeclPropertyIfNecessary(collector, optFunDecl);
    }
  }

  private static void setWeakDeclPropertyIfNecessary(ModificationCollector collector,
      ICPPASTFunctionDeclarator funDecl) {
    new WeakDeclAdder(collector).addWeakDeclAttribute(funDecl);
  }

  private void insertFunDeclInclude(ICPPASTFunctionDeclarator funDecl, IASTTranslationUnit tu,
      ASTRewrite rewriter, ICProject mockatorProject) {
    CppIncludeResolver resolver = new CppIncludeResolver(tu, mockatorProject, getIndex());
    String funDeclTuPath = funDecl.getTranslationUnit().getFilePath();
    AstIncludeNode includeForFunDecl = resolver.resolveIncludeNode(funDeclTuPath);
    rewriter.insertBefore(tu, null, includeForFunDecl, null);
  }

  private static void insertFunDefinition(ICPPASTFunctionDefinition funDef,
      IASTTranslationUnit newTu, ASTRewrite rewriter) {
    rewriter.insertBefore(newTu, null, funDef, null);
  }

  private IASTTranslationUnit createAndGetNewTu(IProject referencingProj, String funName,
      IProgressMonitor pm) throws CoreException {
    IFolder shadowFolder = createShadowFolder(referencingProj, pm);
    IPath newFilePath = getPathForNewFile(shadowFolder, funName);
    newFile = FileUtil.toIFile(newFilePath);
    TranslationUnitCreator creator =
        new TranslationUnitCreator(referencingProj, refactoringContext);
    return creator.createAndGetNewTu(newFilePath, pm);
  }

  private static IPath getPathForNewFile(IFolder shadowFolder, String funName) {
    PathProposalUtil proposal = new PathProposalUtil(shadowFolder.getFullPath());
    return proposal.getUniquePathForNewFile(funName, MockatorConstants.SOURCE_SUFFIX);
  }

  private static IFolder createShadowFolder(IProject project, IProgressMonitor pm) {
    SourceFolderHandler handler = new SourceFolderHandler(project);
    try {
      return handler.createFolder(SHADOW_FOLDER_NAME, pm);
    } catch (CoreException e) {
      throw new MockatorException(e);
    }
  }

  private Collection<IProject> getReferencingExecutables() {
    ReferencingExecutableFinder finder = new ReferencingExecutableFinder(project.getProject());
    Collection<IProject> referencingExecutables = finder.findReferencingExecutables();

    if (referencingExecutables.isEmpty())
      // this is just for unit testing purposes because there we don't
      // have a referencing project there
      return list(project.getProject());

    return referencingExecutables;
  }

  private static ICPPASTFunctionDefinition createFunDefinition(IProject referencingProj,
      ICPPASTFunctionDeclarator funDecl) {
    CppStandard cppStd = CppStandard.fromCompilerFlags(referencingProj);
    return new ShadowFunctionGenerator(cppStd).createShadowedFunction(funDecl,
        nodeFactory.newCompoundStatement());
  }

  IFile getNewFile() {
    return newFile;
  }

  @Override
  public String getDescription() {
    return I18N.ShadwoFunctionRefactoringDesc;
  }
}
