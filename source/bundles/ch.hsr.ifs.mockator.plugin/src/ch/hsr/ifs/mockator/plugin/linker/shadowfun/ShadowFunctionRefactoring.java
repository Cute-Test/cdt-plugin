package ch.hsr.ifs.mockator.plugin.linker.shadowfun;

import static ch.hsr.ifs.iltis.core.core.collections.CollectionUtil.list;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;
import ch.hsr.ifs.iltis.core.core.resources.FileUtil;
import ch.hsr.ifs.iltis.cpp.core.resources.CProjectUtil;
import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.PathProposalUtil;
import ch.hsr.ifs.mockator.plugin.linker.LinkerRefactoring;
import ch.hsr.ifs.mockator.plugin.linker.ReferencingExecutableFinder;
import ch.hsr.ifs.mockator.plugin.linker.WeakDeclAdder;
import ch.hsr.ifs.mockator.plugin.project.cdt.SourceFolderHandler;
import ch.hsr.ifs.mockator.plugin.project.properties.CppStandard;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.AstIncludeNode;
import ch.hsr.ifs.mockator.plugin.refsupport.includes.CppIncludeResolver;
import ch.hsr.ifs.mockator.plugin.refsupport.tu.TranslationUnitCreator;


public class ShadowFunctionRefactoring extends LinkerRefactoring {

   private static final String SHADOW_FOLDER_NAME = "shadows";
   private IFile               newFile;

   public ShadowFunctionRefactoring(final ICElement element, final Optional<ITextSelection> selection, final ICProject cProject) {
      super(element, selection, cProject);
   }

   @Override
   protected void createLinkerSeamSupport(final ModificationCollector collector, final IASTName funName, final IProgressMonitor pm)
         throws CoreException {
      final Optional<ICPPASTFunctionDeclarator> optFunDecl = findFunDeclaration(funName, pm);
      if (optFunDecl.isPresent()) {
         for (final IProject refProj : getReferencingExecutables()) {
            final IASTTranslationUnit newTu = createAndGetNewTu(refProj, funName.toString(), pm);
            final ICPPASTFunctionDefinition funDef = createFunDefinition(refProj, optFunDecl.get());
            final ASTRewrite rewriter = collector.rewriterForTranslationUnit(newTu);
            final ICProject cProject = CProjectUtil.getCProject(refProj);
            insertFunDeclInclude(optFunDecl.get(), newTu, rewriter, cProject);
            insertFunDefinition(funDef, newTu, rewriter);
         }

         setWeakDeclPropertyIfNecessary(collector, optFunDecl.get());
      }
   }

   private static void setWeakDeclPropertyIfNecessary(final ModificationCollector collector, final ICPPASTFunctionDeclarator funDecl) {
      new WeakDeclAdder(collector).addWeakDeclAttribute(funDecl);
   }

   private void insertFunDeclInclude(final ICPPASTFunctionDeclarator funDecl, final IASTTranslationUnit tu, final ASTRewrite rewriter,
         final ICProject mockatorProject) {
      final CppIncludeResolver resolver = new CppIncludeResolver(tu, mockatorProject, getIndex());
      final String funDeclTuPath = funDecl.getTranslationUnit().getFilePath();
      final AstIncludeNode includeForFunDecl = resolver.resolveIncludeNode(funDeclTuPath);
      rewriter.insertBefore(tu, null, includeForFunDecl, null);
   }

   private static void insertFunDefinition(final ICPPASTFunctionDefinition funDef, final IASTTranslationUnit newTu, final ASTRewrite rewriter) {
      rewriter.insertBefore(newTu, null, funDef, null);
   }

   private IASTTranslationUnit createAndGetNewTu(final IProject referencingProj, final String funName, final IProgressMonitor pm)
         throws CoreException {
      final IFolder shadowFolder = createShadowFolder(referencingProj, pm);
      final IPath newFilePath = getPathForNewFile(shadowFolder, funName);
      newFile = FileUtil.toIFile(newFilePath);
      final TranslationUnitCreator creator = new TranslationUnitCreator(referencingProj, refactoringContext);
      return creator.createAndGetNewTu(newFilePath, pm);
   }

   private static IPath getPathForNewFile(final IFolder shadowFolder, final String funName) {
      final PathProposalUtil proposal = new PathProposalUtil(shadowFolder.getFullPath());
      return proposal.getUniquePathForNewFile(funName, MockatorConstants.SOURCE_SUFFIX);
   }

   private static IFolder createShadowFolder(final IProject project, final IProgressMonitor pm) {
      final SourceFolderHandler handler = new SourceFolderHandler(project);
      try {
         return handler.createFolder(SHADOW_FOLDER_NAME, pm);
      } catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }

   private Collection<IProject> getReferencingExecutables() {
      final ReferencingExecutableFinder finder = new ReferencingExecutableFinder(getProject().getProject());
      final Collection<IProject> referencingExecutables = finder.findReferencingExecutables();

      if (referencingExecutables.isEmpty()) {
         // this is just for unit testing purposes because there we don't
         // have a referencing project there
         return list(getProject().getProject());
      }

      return referencingExecutables;
   }

   private static ICPPASTFunctionDefinition createFunDefinition(final IProject referencingProj, final ICPPASTFunctionDeclarator funDecl) {
      final CppStandard cppStd = CppStandard.fromCompilerFlags(referencingProj);
      return new ShadowFunctionGenerator(cppStd).createShadowedFunction(funDecl, nodeFactory.newCompoundStatement());
   }

   IFile getNewFile() {
      return newFile;
   }

   @Override
   public String getDescription() {
      return I18N.ShadwoFunctionRefactoringDesc;
   }
}
