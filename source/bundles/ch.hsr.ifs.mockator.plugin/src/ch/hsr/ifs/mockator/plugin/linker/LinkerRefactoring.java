package ch.hsr.ifs.mockator.plugin.linker;

import java.util.Optional;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.iltis.core.functional.OptionalUtil;
import ch.hsr.ifs.iltis.cpp.wrappers.ModificationCollector;

import ch.hsr.ifs.mockator.plugin.refsupport.functions.params.ParameterNameFunDecorator;
import ch.hsr.ifs.mockator.plugin.refsupport.lookup.NodeLookup;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;


public abstract class LinkerRefactoring extends MockatorRefactoring {

   public LinkerRefactoring(final ICElement element, final ITextSelection selection, final ICProject project) {
      super(element, selection, project);
   }

   @Override
   public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) throws CoreException {
      final RefactoringStatus status = super.checkInitialConditions(pm);
      final IASTTranslationUnit ast = getAST(tu(), pm);
      final LinkerFunctionPreconVerifier verifier = new LinkerFunctionPreconVerifier(status, ast);
      verifier.assureSatisfiesLinkSeamProperties(getSelectedName(ast));
      return status;
   }

   @Override
   protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
         OperationCanceledException {
      OptionalUtil.doIfPresentT(getSelectedName(getAST(tu(), pm)), (selectedName) -> createLinkerSeamSupport(collector, selectedName, pm));
   }

   protected abstract void createLinkerSeamSupport(ModificationCollector collector, IASTName selectedName, IProgressMonitor pm) throws CoreException;

   protected void adjustParamNamesIfNecessary(final ICPPASTFunctionDeclarator newFunDecl) {
      final ParameterNameFunDecorator funDecorator = new ParameterNameFunDecorator(newFunDecl);
      funDecorator.adjustParamNamesIfNecessary();
   }

   protected Optional<ICPPASTFunctionDeclarator> findFunDeclaration(final IASTName funName, final IProgressMonitor pm) {
      final NodeLookup lookup = new NodeLookup(getProject(), pm);
      return lookup.findFunctionDeclaration(funName, refactoringContext());
   }
}
