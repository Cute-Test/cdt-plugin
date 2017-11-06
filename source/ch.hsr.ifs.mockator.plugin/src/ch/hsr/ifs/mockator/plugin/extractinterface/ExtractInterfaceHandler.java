package ch.hsr.ifs.mockator.plugin.extractinterface;

import static ch.hsr.ifs.mockator.plugin.base.functional.HigherOrder.forEach;

import java.util.function.Function;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.mockator.plugin.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.mockator.plugin.extractinterface.postconditions.ShadowedMemFunVerifier;
import ch.hsr.ifs.mockator.plugin.extractinterface.preconditions.DiagnosticsRegistry;
import ch.hsr.ifs.mockator.plugin.extractinterface.transform.TransformationsRegistry;


class ExtractInterfaceHandler {

   private final ExtractInterfaceContext context;

   public ExtractInterfaceHandler(final ExtractInterfaceContext context) {
      this.context = context;
   }

   public void preProcess() {
      final StopWhenFatalErrorCondition stopWhen = new StopWhenFatalErrorCondition(context.getStatus());
      forEach(new DiagnosticsRegistry().createInstances(), context, stopWhen);
   }

   public void performChanges() {
      forEach(new TransformationsRegistry().createInstances(), context);
   }

   public void postProcess(final RefactoringStatus status) throws CoreException {
      new ShadowedMemFunVerifier(context).checkForShadowedFunctions(status);
   }

   private static class StopWhenFatalErrorCondition implements Function<Void, Boolean> {

      private final RefactoringStatus status;

      public StopWhenFatalErrorCondition(final RefactoringStatus status) {
         this.status = status;
      }

      @Override
      public Boolean apply(final Void ignore) {
         return status.hasFatalError();
      }
   }
}
