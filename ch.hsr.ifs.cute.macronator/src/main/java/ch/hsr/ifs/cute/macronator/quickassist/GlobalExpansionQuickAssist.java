package ch.hsr.ifs.cute.macronator.quickassist;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.IInvocationContext;
import org.eclipse.cdt.ui.text.IProblemLocation;
import org.eclipse.cdt.ui.text.IQuickAssistProcessor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ch.hsr.ifs.cute.macronator.refactoring.ExpandMacroAction;

/**
 * Displays a quickassist to start the global macro expansion refactoring.
 */
public class GlobalExpansionQuickAssist implements IQuickAssistProcessor, RunnableCallback {

    public static final String ID = "ch.hsr.ifs.macronator.plugin.assist.GlobalExpansion";

    @Override
    public boolean hasAssists(final IInvocationContext context) throws CoreException {
        return true;
    }

    @Override
    public ICCompletionProposal[] getAssists(final IInvocationContext context, IProblemLocation[] locations) throws CoreException {
        IStatus status = ASTProvider.getASTProvider().runOnAST(context.getTranslationUnit(), ASTProvider.WAIT_ACTIVE_ONLY, new NullProgressMonitor(), new CompletionProposoalAstRunnable(this, context.getSelectionOffset(), context.getSelectionLength()));
        if (!status.isOK()) {
            return new ICCompletionProposal[0];
        }
        return new ICCompletionProposal[] { new ICCompletionProposal() {

            @Override
            public void apply(IDocument document) {
                new ExpandMacroAction().run(null);
            }

            @Override
            public Point getSelection(IDocument document) {
                return null;
            }

            @Override
            public String getAdditionalProposalInfo() {
                return null;
            }

            @Override
            public String getDisplayString() {
                return "Expand globally / remove definition";
            }

            @Override
            public Image getImage() {
                return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_MACRO);
            }

            @Override
            public IContextInformation getContextInformation() {
                return null;
            }

            @Override
            public int getRelevance() {
                return 0;
            }

            @Override
            public String getIdString() {
                return ID;
            }

        } };
    }

    @Override
    public void setSelectedName(IASTName macro) {

    }
}