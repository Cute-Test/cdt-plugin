package ch.hsr.ifs.cute.macronator.quickassist;

import static ch.hsr.ifs.cute.macronator.MacronatorPlugin.SUPPRESSED_MACROS;
import static ch.hsr.ifs.cute.macronator.MacronatorPlugin.getDefaultPreferenceValue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.cxx.Activator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.IInvocationContext;
import org.eclipse.cdt.ui.text.IProblemLocation;
import org.eclipse.cdt.ui.text.IQuickAssistProcessor;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import ch.hsr.ifs.cute.macronator.common.SuppressedMacros;

/**
 * Displays a quickassist to suppress the selected refactoring suggestion to be
 * displayed.
 * 
 */
public class SuppressMacroQuickAssist implements IQuickAssistProcessor {

    public static String ID = "ch.hsr.ifs.macronator.plugin.assist.SuppressMacro";

    @Override
    public boolean hasAssists(IInvocationContext context) throws CoreException {
        return true;
    }

    @Override
    public ICCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
        String projectLocation = context.getTranslationUnit().getCProject().getProject().getLocation().toString();
        Map<QualifiedName, String> properties = context.getTranslationUnit().getCProject().getProject().getPersistentProperties();
        String suppressedMacroFileName = properties.containsKey(SUPPRESSED_MACROS) ? properties.get(SUPPRESSED_MACROS) : getDefaultPreferenceValue(SUPPRESSED_MACROS);
        Path path = FileSystems.getDefault().getPath(projectLocation, suppressedMacroFileName);
        if (!Files.exists(path)) {
            try {
                path = Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        final SelectionASTRunnable runnable = new SelectionASTRunnable(context.getSelectionOffset(), context.getSelectionLength());
        IStatus status = ASTProvider.getASTProvider().runOnAST(context.getTranslationUnit(), ASTProvider.WAIT_ACTIVE_ONLY, new NullProgressMonitor(), runnable);
        return (status.isOK() && isMacroDefinition(runnable.getResult())) ? new ICCompletionProposal[] { new SuppressMacroProposal(runnable.getResult(), new SuppressedMacros(path)) } : new ICCompletionProposal[0];
    }    

    
    private boolean isMacroDefinition(IASTName macro) {
        return macro != null && macro.getBinding() instanceof IMacroBinding && macro.isDefinition();
    }

    private class SuppressMacroProposal implements ICCompletionProposal {

        private final IASTName macroName;
        private final SuppressedMacros suppressedMacros;

        public SuppressMacroProposal(IASTName macroName, SuppressedMacros suppressedMacros) {
            this.macroName = macroName;
            this.suppressedMacros = suppressedMacros;
        }

        @Override
        public void apply(IDocument document) {
            if (!suppressedMacros.isSuppressed(macroName.toString())) {
                suppressedMacros.add(macroName.toString());
                deleteMarker();
            } else {
                suppressedMacros.remove(macroName.toString());
                CodanRuntime.getInstance().getBuilder().processResource(macroName.getTranslationUnit().getOriginatingTranslationUnit().getCProject().getProject(), new NullProgressMonitor());
            }
        }

        private void deleteMarker() {
            IResource resource = macroName.getTranslationUnit().getOriginatingTranslationUnit().getResource();
            try {
                IMarker[] markers = resource.findMarkers("org.eclipse.cdt.codan.core.codanProblem", true, IResource.DEPTH_INFINITE);
                for (IMarker marker : markers) {
                    Integer lineNumber = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
                    if (macroName.getFileLocation().getStartingLineNumber() == lineNumber)
                        marker.delete();
                }
            } catch (CoreException e) {
                Activator.log("Error deleting marker", e);
            }
        }

        @Override
        public Point getSelection(IDocument document) {
            return null;
        }

        @Override
        public String getAdditionalProposalInfo() {
            return "Hides refactoring suggestions on demand";
        }

        @Override
        public String getDisplayString() {
            return hiddenStatusString() + "refactoring suggestion";
        }

        private String hiddenStatusString() {
            return (suppressedMacros.isSuppressed(macroName.toString())) ? "Enable " : "Disable ";
        }

        @Override
        public Image getImage() {
            return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_MACRO);
        }

        @Override
        public int getRelevance() {
            return 0;
        }

        @Override
        public String getIdString() {
            return ID;
        }

        @Override
        public IContextInformation getContextInformation() {
            return null;
        }
    }
}
