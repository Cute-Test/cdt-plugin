package ch.hsr.ifs.mockator.plugin.preprocessor;

import java.net.URI;
import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.cpp.resources.CProjectUtil;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.iltis.core.exception.ILTISException;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.project.cdt.SourceFolderHandler;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NotInSameTuAsCalleeVerifier;


@SuppressWarnings("restriction")
public class PreprocessorRefactoring extends MockatorRefactoring {

   private IPath newHeaderFilePath;
   private IPath newSourceFilePath;

   public PreprocessorRefactoring(final ICElement element, final ITextSelection selection, final ICProject cproject) {
      super(element, selection, cproject);
   }

   @Override
   public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) throws CoreException {
      final RefactoringStatus status = super.checkInitialConditions(pm);
      final IASTTranslationUnit ast = getAST(tu, pm);
      final Optional<IASTName> selectedName = getSelectedName(ast);

      if (!selectedName.isPresent()) {
         status.addFatalError("No name in selection found");
         return status;
      }

      if (isFunctionTemplate(selectedName.get())) {
         status.addFatalError("Function templates are currently not supported");
         return status;
      }

      final Optional<ICPPASTFunctionDeclarator> funDeclCandidate = findFunDeclaration(selectedName.get(), pm);

      if (!funDeclCandidate.isPresent()) {
         status.addFatalError("No function declaration found");
      } else if (!isFreeFunction(funDeclCandidate.get())) {
         status.addFatalError("Member functions cannot be traced");
      } else {
         assureHasDefinitionNotInSameTu(status, ast, selectedName.get());
      }

      return status;
   }

   private static boolean isFunctionTemplate(final IASTName funName) {
      final IBinding binding = funName.resolveBinding();
      return binding instanceof ICPPFunctionTemplate || binding instanceof ICPPTemplateInstance;
   }

   private static void assureHasDefinitionNotInSameTu(final RefactoringStatus status, final IASTTranslationUnit ast, final IASTName funName) {
      new NotInSameTuAsCalleeVerifier(status, ast).assurehasDefinitionNotInSameTu(funName.resolveBinding());
   }

   private static boolean isFreeFunction(final ICPPASTFunctionDeclarator funDecl) {
      return AstUtil.getAncestorOfType(funDecl, ICPPASTCompositeTypeSpecifier.class) == null;
   }

   @Override
   protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
         OperationCanceledException {
      final Optional<IASTName> optSelectedName = getSelectedName(getAST(tu, pm));
      if (optSelectedName.isPresent()) {
         final Optional<ICPPASTFunctionDeclarator> funDecl = findFunDeclaration(optSelectedName.get(), pm);
         if (funDecl.isPresent()) {
            createTraceFolder(pm);
            createHeaderFile(pm, collector, funDecl.get());
            createSourceFile(pm, collector, funDecl.get());
            addUndefBeforeFunDefinition(optSelectedName.get(), collector, pm);
         }
      }
   }

   private IFolder createTraceFolder(final IProgressMonitor pm) {
      final SourceFolderHandler handler = new SourceFolderHandler(project.getProject());

      try {
         return handler.createFolder(MockatorConstants.TRACE_FOLDER, pm);
      }
      catch (final CoreException e) {
         throw new ILTISException(e).rethrowUnchecked();
      }
   }

   private void createHeaderFile(final IProgressMonitor pm, final ModificationCollector collector, final ICPPASTFunctionDeclarator funDecl)
         throws CoreException {
      newHeaderFilePath = getProjectHeaderFilePath(funDecl);
      final PreprocessorHeaderFileCreator creator = new PreprocessorHeaderFileCreator(collector, project, refactoringContext);
      creator.createFile(newHeaderFilePath, funDecl, pm);
   }

   private IPath getProjectHeaderFilePath(final ICPPASTFunctionDeclarator funDecl) {
      return new TraceFileNameCreator(funDecl.getName().toString(), project.getProject()).getHeaderFilePath();
   }

   private void createSourceFile(final IProgressMonitor pm, final ModificationCollector collector, final ICPPASTFunctionDeclarator funDecl)
         throws CoreException {
      final String funDeclName = funDecl.getName().toString();
      newSourceFilePath = new TraceFileNameCreator(funDeclName, project.getProject()).getSourceFilePath();
      final PreprocessorSourceFileCreator creator = new PreprocessorSourceFileCreator(newHeaderFilePath, collector, project, refactoringContext);
      creator.createFile(newSourceFilePath, funDecl, pm);
   }

   private void addUndefBeforeFunDefinition(final IASTName selectedName, final ModificationCollector collector, final IProgressMonitor pm) {
      findFunDefinition(selectedName, pm).ifPresent((funDef) -> {
         final IASTTranslationUnit tuOfFunDef = funDef.getTranslationUnit();

         if (isTuOfDefinitionInSameProject(tuOfFunDef)) {
            final ASTRewrite rewriter = createRewriter(collector, tuOfFunDef);
            final UndefMacroAdder undefAdder = new UndefMacroAdder(tuOfFunDef, rewriter, funDef);
            undefAdder.addUndefMacro(selectedName.toString());
         }
      });
   }

   private boolean isTuOfDefinitionInSameProject(final IASTTranslationUnit tuOfFunDef) {
      final URI uriOfTu = FileUtil.stringToUri(tuOfFunDef.getFilePath());
      return CProjectUtil.isPartOfProject(uriOfTu, project.getProject());
   }

   private Optional<ICPPASTFunctionDeclarator> findFunDeclaration(final IASTName funName, final IProgressMonitor pm) {
      final NodeLookup lookup = new NodeLookup(project, pm);
      return lookup.findFunctionDeclaration(funName, refactoringContext);
   }

   private Optional<ICPPASTFunctionDefinition> findFunDefinition(final IASTName funName, final IProgressMonitor pm) {
      final NodeLookup lookup = new NodeLookup(project, pm);
      return lookup.findFunctionDefinition(funName, refactoringContext);
   }

   IPath getNewHeaderFilePath() {
      return newHeaderFilePath;
   }

   IPath getNewSourceFilePath() {
      return newSourceFilePath;
   }

   @Override
   public String getDescription() {
      return I18N.PreprocessorRefactoringDesc;
   }
}
