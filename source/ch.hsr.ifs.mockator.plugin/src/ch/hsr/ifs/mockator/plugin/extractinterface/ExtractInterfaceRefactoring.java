package ch.hsr.ifs.mockator.plugin.extractinterface;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

import ch.hsr.ifs.iltis.cpp.ast.ASTUtil;
import ch.hsr.ifs.iltis.cpp.wrappers.CRefactoringDescriptor;
import ch.hsr.ifs.iltis.cpp.wrappers.ModificationCollector;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.refsupport.qf.MockatorRefactoring;


public class ExtractInterfaceRefactoring extends MockatorRefactoring {

   public static final String            ID = "ch.hsr.ifs.mockator.plugin.extractinterface.ExtractInterfaceRefactoring";
   private final ExtractInterfaceContext context;
   private ExtractInterfaceHandler       handler;

   public ExtractInterfaceRefactoring(final ExtractInterfaceContext context) {
      super(context.getTuOfSelection(), context.getSelection(), context.getCProject());
      this.context = context;
   }

   @Override
   public RefactoringStatus checkInitialConditions(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
      final RefactoringStatus status = super.checkInitialConditions(pm);
      pm.subTask(I18N.ExtractInterfaceAnalyzingInProgress);
      prepareRefactoring(status, pm);
      handler.preProcess();
      return status;
   }

   private void prepareRefactoring(final RefactoringStatus status, final IProgressMonitor pm) throws OperationCanceledException, CoreException {
      context.setStatus(status);
      context.setSelectedName(getSelectedName(getAST(getTranslationUnit(), pm)));
      context.setProgressMonitor(pm);
      context.setCRefContext(refactoringContext());
      handler = new ExtractInterfaceHandler(context);
   }

   @Override
   protected RefactoringStatus checkFinalConditions(final IProgressMonitor pm, final CheckConditionsContext c) throws CoreException {
      final RefactoringStatus status = c.check(pm);

      try {
         handler.postProcess(status);
      }
      //TODO check if Exception is the right replacement for ILTISException
      catch (final Exception e) {
         status.addFatalError("Extract interface refactoring failed: " + e.getMessage());
      }

      return status;
   }

   @Override
   protected RefactoringDescriptor getRefactoringDescriptor() {
      return new ExtractInterfaceDescriptor(ID, getProject().getProject().getName(), "Extract Interface Refactoring", getRefactoringDescription(),
               getArgumentMap());
   }

   private String getRefactoringDescription() {
      final String className = ASTUtil.getQfNameF(context.getChosenClass());
      return String.format("Extract interface for class '%s'", className);
   }

   private Map<String, String> getArgumentMap() {
      final Map<String, String> args = unorderedMap();
      args.put(CRefactoringDescriptor.FILE_NAME, getTranslationUnit().getLocationURI().toString());
      args.put(CRefactoringDescriptor.SELECTION, selectedRegion().getOffset() + "," + selectedRegion().getLength());
      args.put(ExtractInterfaceDescriptor.NEW_INTERFACE_NAME, context.getNewInterfaceName());
      args.put(ExtractInterfaceDescriptor.REPLACE_ALL_OCCURENCES, String.valueOf(context.shouldReplaceAllOccurences()));
      return args;
   }

   @Override
   protected void collectModifications(final IProgressMonitor pm, final ModificationCollector c) throws CoreException, OperationCanceledException {
      pm.subTask(I18N.ExtractInterfacePerformingChangesInProgress);
      context.setModificationCollector(c);
      context.setProgressMonitor(pm);
      handler.performChanges();
   }

   public ExtractInterfaceContext getContext() {
      return context;
   }

   @Override
   public String getDescription() {
      return I18N.ExtractInterfaceName;
   }
}
