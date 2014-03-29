package ch.hsr.ifs.mockator.plugin.linker;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPNodeFactory;
import org.eclipse.cdt.internal.ui.refactoring.ModificationCollector;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterNameFunDecorator;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;

@SuppressWarnings("restriction")
public abstract class LinkerRefactoring extends MockatorRefactoring {
  protected static final CPPNodeFactory nodeFactory = CPPNodeFactory.getDefault();

  public LinkerRefactoring(ICElement element, ITextSelection selection, ICProject project) {
    super(element, selection, project);
  }

  @Override
  public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException {
    RefactoringStatus status = super.checkInitialConditions(pm);
    IASTTranslationUnit ast = getAST(tu, pm);
    LinkerFunctionPreconVerifier verifier = new LinkerFunctionPreconVerifier(status, ast);
    verifier.assureSatisfiesLinkSeamProperties(getSelectedName(ast));
    return status;
  }

  @Override
  protected void collectModifications(IProgressMonitor pm, ModificationCollector collector)
      throws CoreException, OperationCanceledException {
    for (IASTName optFunName : getSelectedName(getAST(tu, pm))) {
      createLinkerSeamSupport(collector, optFunName, pm);
    }
  }

  protected abstract void createLinkerSeamSupport(ModificationCollector collector,
      IASTName selectedName, IProgressMonitor pm) throws CoreException;

  protected void adjustParamNamesIfNecessary(ICPPASTFunctionDeclarator newFunDecl) {
    ParameterNameFunDecorator funDecorator = new ParameterNameFunDecorator(newFunDecl);
    funDecorator.adjustParamNamesIfNecessary();
  }

  protected Maybe<ICPPASTFunctionDeclarator> findFunDeclaration(IASTName funName,
      IProgressMonitor pm) {
    NodeLookup lookup = new NodeLookup(project, pm);
    return lookup.findFunctionDeclaration(funName, refactoringContext);
  }
}
