package ch.hsr.ifs.cute.mockator.linker.wrapfun.gnuoption.qf;

import static ch.hsr.ifs.iltis.cpp.core.util.constants.CommonCPPConstants.END_IF_DIRECTIVE;

import java.util.Optional;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.MultiTextEdit;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.iltis.cpp.core.wrappers.CCompositeChange;
import ch.hsr.ifs.iltis.cpp.core.wrappers.ModificationCollector;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;


class DeleteWrappedFunctionRefactoring extends MockatorRefactoring {

    private final IDocument doc;

    public DeleteWrappedFunctionRefactoring(final ICElement element, final Optional<ITextSelection> selection, final ICProject project,
                                            final IDocument doc) {
        super(element, selection);
        this.doc = doc;
    }

    @Override
    public Change createChange(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
        final CCompositeChange cChange = new CCompositeChange("Delete wrapped code segment");
        final int offset = selection.map(ITextSelection::getOffset).orElse(-1);
        final DeleteEdit deleteEdit = new DeleteEdit(offset, getLengthOfWrappedFunCode(offset));
        final MultiTextEdit multiTextEdit = new MultiTextEdit();
        multiTextEdit.addChild(deleteEdit);
        final TextFileChange change = createTextFileChange(pm, multiTextEdit);
        cChange.add(change);
        return cChange;
    }

    private TextFileChange createTextFileChange(final IProgressMonitor pm, final MultiTextEdit multiTextEdit) throws CoreException {
        final TextFileChange change = new TextFileChange("Delete wrapped function", getIFile());
        change.setEdit(multiTextEdit);
        return change;
    }

    private int getLengthOfWrappedFunCode(final int offset) {
        try {
            final String wrappedCode = doc.get(offset, doc.getLength() - offset);
            return wrappedCode.indexOf(END_IF_DIRECTIVE) + END_IF_DIRECTIVE.length();
        } catch (final BadLocationException e) {}
        throw new ILTISException("Was not able to determine wrapped code segment").rethrowUnchecked();
    }

    @Override
    protected void collectModifications(final IProgressMonitor pm, final ModificationCollector collector) throws CoreException,
            OperationCanceledException {}

    @Override
    public String getDescription() {
        return I18N.DeleteWrappedFunctionRefactoringDesc;
    }
}
