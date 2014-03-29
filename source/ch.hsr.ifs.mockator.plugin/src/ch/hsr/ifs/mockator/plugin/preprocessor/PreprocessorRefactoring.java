package ch.hsr.ifs.mockator.plugin.preprocessor;

import java.net.URI;

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

import ch.hsr.ifs.mockator.plugin.MockatorConstants;
import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;
import ch.hsr.ifs.mockator.plugin.base.util.ProjectUtil;
import ch.hsr.ifs.mockator.plugin.project.cdt.SourceFolderHandler;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.AstUtil;
import ch.hsr.ifs.mockator.plugin.refsupport.utils.NotInSameTuAsCalleeVerifier;

@SuppressWarnings("restriction")
public class PreprocessorRefactoring extends MockatorRefactoring {
  private IPath newHeaderFilePath;
  private IPath newSourceFilePath;

  public PreprocessorRefactoring(ICElement element, ITextSelection selection, ICProject cproject) {
    super(element, selection, cproject);
  }

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
    RefactoringStatus status = super.checkInitialConditions(pm);
    IASTTranslationUnit ast = getAST(tu, pm);
    Maybe<IASTName> selectedName = getSelectedName(ast);

    if (selectedName.isNone()) {
      status.addFatalError("No name in selection found");
      return status;
    }

    if (isFunctionTemplate(selectedName.get())) {
      status.addFatalError("Function templates are currently not supported");
      return status;
    }

    Maybe<ICPPASTFunctionDeclarator> funDeclCandidate = findFunDeclaration(selectedName.get(), pm);

    if (funDeclCandidate.isNone()) {
      status.addFatalError("No function declaration found");
    } else if (!isFreeFunction(funDeclCandidate.get())) {
      status.addFatalError("Member functions cannot be traced");
    } else {
      assureHasDefinitionNotInSameTu(status, ast, selectedName.get());
    }

    return status;
  }

  private static boolean isFunctionTemplate(IASTName funName) {
    IBinding binding = funName.resolveBinding();
    return binding instanceof ICPPFunctionTemplate || binding instanceof ICPPTemplateInstance;
  }

  private static void assureHasDefinitionNotInSameTu(RefactoringStatus status,
      IASTTranslationUnit ast, IASTName funName) {
    new NotInSameTuAsCalleeVerifier(status, ast).assurehasDefinitionNotInSameTu(funName
        .resolveBinding());
  }

  private static boolean isFreeFunction(ICPPASTFunctionDeclarator funDecl) {
    return AstUtil.getAncestorOfType(funDecl, ICPPASTCompositeTypeSpecifier.class) == null;
  }

  @Override
  protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
      throws CoreException, OperationCanceledException {
    for (IASTName optName : getSelectedName(getAST(tu, pm))) {
      for (ICPPASTFunctionDeclarator optFunDecl : findFunDeclaration(optName, pm)) {
        createTraceFolder(pm);
        createHeaderFile(pm, collector, optFunDecl);
        createSourceFile(pm, collector, optFunDecl);
        addUndefBeforeFunDefinition(optName, collector, pm);
      }
    }
  }

  private IFolder createTraceFolder(IProgressMonitor pm) {
    SourceFolderHandler handler = new SourceFolderHandler(project.getProject());

    try {
      return handler.createFolder(MockatorConstants.TRACE_FOLDER, pm);
    } catch (CoreException e) {
      throw new MockatorException(e);
    }
  }

  private void createHeaderFile(IProgressMonitor pm, ModificationCollector collector,
      ICPPASTFunctionDeclarator funDecl) throws CoreException {
    newHeaderFilePath = getProjectHeaderFilePath(funDecl);
    PreprocessorHeaderFileCreator creator =
        new PreprocessorHeaderFileCreator(collector, project, refactoringContext);
    creator.createFile(newHeaderFilePath, funDecl, pm);
  }

  private IPath getProjectHeaderFilePath(ICPPASTFunctionDeclarator funDecl) {
    return new TraceFileNameCreator(funDecl.getName().toString(), project.getProject())
        .getHeaderFilePath();
  }

  private void createSourceFile(IProgressMonitor pm, ModificationCollector collector,
      ICPPASTFunctionDeclarator funDecl) throws CoreException {
    String funDeclName = funDecl.getName().toString();
    newSourceFilePath =
        new TraceFileNameCreator(funDeclName, project.getProject()).getSourceFilePath();
    PreprocessorSourceFileCreator creator =
        new PreprocessorSourceFileCreator(newHeaderFilePath, collector, project, refactoringContext);
    creator.createFile(newSourceFilePath, funDecl, pm);
  }

  private void addUndefBeforeFunDefinition(IASTName selectedName, ModificationCollector collector,
      IProgressMonitor pm) {
    for (ICPPASTFunctionDefinition optFunDef : findFunDefinition(selectedName, pm)) {
      IASTTranslationUnit tuOfFunDef = optFunDef.getTranslationUnit();

      if (isTuOfDefinitionInSameProject(tuOfFunDef)) {
        ASTRewrite rewriter = createRewriter(collector, tuOfFunDef);
        UndefMacroAdder undefAdder = new UndefMacroAdder(tuOfFunDef, rewriter, optFunDef);
        undefAdder.addUndefMacro(selectedName.toString());
      }
    }
  }

  private boolean isTuOfDefinitionInSameProject(IASTTranslationUnit tuOfFunDef) {
    URI uriOfTu = FileUtil.stringToUri(tuOfFunDef.getFilePath());
    return ProjectUtil.isPartOfProject(uriOfTu, project.getProject());
  }

  private Maybe<ICPPASTFunctionDeclarator> findFunDeclaration(IASTName funName, IProgressMonitor pm) {
    NodeLookup lookup = new NodeLookup(project, pm);
    return lookup.findFunctionDeclaration(funName, refactoringContext);
  }

  private Maybe<ICPPASTFunctionDefinition> findFunDefinition(IASTName funName, IProgressMonitor pm) {
    NodeLookup lookup = new NodeLookup(project, pm);
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
