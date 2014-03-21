package ch.hsr.ifs.cute.macronator.quickassist;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

class CompletionProposoalAstRunnable implements ASTRunnable {

    private final int selectionOffset;
    private final int selectionLength;
    private final RunnableCallback callback;

    public CompletionProposoalAstRunnable(RunnableCallback callback, int selectionOffset, int selectionLength) {
        this.callback = callback;
        this.selectionOffset = selectionOffset;
        this.selectionLength = selectionLength;
    }

    @Override
    public IStatus runOnAST(ILanguage lang, IASTTranslationUnit ast) throws CoreException {
        IASTNode selectedNode = ast.getNodeSelector(null).findEnclosingNode(selectionOffset, selectionLength);
        IASTName selectedName = ast.getNodeSelector(null).findFirstContainedName(selectedNode.getFileLocation().getNodeOffset(), selectedNode.getFileLocation().getNodeLength());
        callback.setSelectedName(selectedName);
        return isMacroDefinition(selectedName) ? Status.OK_STATUS : Status.CANCEL_STATUS;
    }

    private boolean isMacroDefinition(IASTName macro) {
        return macro != null && macro.getBinding() instanceof IMacroBinding && macro.isDefinition();
    }
}
