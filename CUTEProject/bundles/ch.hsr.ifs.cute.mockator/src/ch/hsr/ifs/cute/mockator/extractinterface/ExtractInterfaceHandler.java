package ch.hsr.ifs.cute.mockator.extractinterface;

import java.util.function.Predicate;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ch.hsr.ifs.cute.mockator.extractinterface.context.ExtractInterfaceContext;
import ch.hsr.ifs.cute.mockator.extractinterface.postconditions.ShadowedMemFunVerifier;
import ch.hsr.ifs.cute.mockator.extractinterface.preconditions.DiagnosticsRegistry;
import ch.hsr.ifs.cute.mockator.extractinterface.transform.TransformationsRegistry;


class ExtractInterfaceHandler {

    private final ExtractInterfaceContext context;

    public ExtractInterfaceHandler(final ExtractInterfaceContext context) {
        this.context = context;
    }

    public void preProcess() {
        final StopWhenFatalErrorCondition stopWhen = new StopWhenFatalErrorCondition(context.getStatus());
        new DiagnosticsRegistry().createInstances().stream().forEachOrdered((it) -> {
            if (!stopWhen.test(null)) {
                it.accept(context);
            }
        });
    }

    public void performChanges() {
        new TransformationsRegistry().createInstances().stream().forEachOrdered((it) -> it.accept(context));
    }

    public void postProcess(final RefactoringStatus status) throws CoreException {
        new ShadowedMemFunVerifier(context).checkForShadowedFunctions(status);
    }

    private static class StopWhenFatalErrorCondition implements Predicate<Void> {

        private final RefactoringStatus status;

        public StopWhenFatalErrorCondition(final RefactoringStatus status) {
            this.status = status;
        }

        @Override
        public boolean test(final Void ignore) {
            return status.hasFatalError();
        }
    }
}
