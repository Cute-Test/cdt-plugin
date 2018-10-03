package ch.hsr.ifs.cute.mockator.testdouble.creation;

import java.util.Optional;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.cute.mockator.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.LinkedModeInformation;


@SuppressWarnings("restriction")
public abstract class AbstractTestDoubleLinkedMode implements LinkedModeInfoCreater {

    private static final Proposal[] TEST_DOUBLE_PROPOSALS;
    protected final String          newClassName;
    private final ChangeEdit        edit;
    private final IDocument         document;

    static {
        TEST_DOUBLE_PROPOSALS = new Proposal[] { new Proposal("class", CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CLASS), 0), new Proposal(
                "struct", CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_STRUCT), 0) };
    }

    public AbstractTestDoubleLinkedMode(final ChangeEdit edit, final IDocument document, final String newClassName) {
        this.edit = edit;
        this.document = document;
        this.newClassName = newClassName;
    }

    @Override
    public LinkedModeInformation createLinkedModeInfo() {
        final LinkedModeInformation lm = new LinkedModeInformation();
        try {
            getBeginOfClassDefinition().ifPresent((offset) -> {
                try {
                    getClassSpecifierLength(offset).ifPresent((length) -> {
                        lm.addPosition(offset, length);
                        lm.addProposal(offset, TEST_DOUBLE_PROPOSALS);
                    });
                } catch (final BadLocationException ignored) {}
            });
        } catch (final BadLocationException ignored) {}
        return lm;
    }

    private Optional<Integer> getBeginOfClassDefinition() throws BadLocationException {
        final Optional<Integer> optOffset = getBeginOfTestDouble();
        if (optOffset.isPresent()) {
            Integer offset = optOffset.get();
            for (; offset >= 0; offset--) {
                if (isNewline(offset)) {
                    break;
                }
            }

            for (; offset <= document.getLength(); offset++) {
                if (!isWhitespace(offset)) {
                    return Optional.of(offset);
                }
            }
        }

        return Optional.empty();
    }

    protected Optional<Integer> getBeginOfTestDouble() {
        return edit.getAbsoluteIndex(newClassName, newClassName);
    }

    private boolean isNewline(final int offset) throws BadLocationException {
        return document.getChar(offset) == '\n';
    }

    private boolean isWhitespace(final int offset) throws BadLocationException {
        return Character.isWhitespace(document.getChar(offset));
    }

    private Optional<Integer> getClassSpecifierLength(final Integer startOffset) throws BadLocationException {
        return getEndOfClassSpecifier().map(endOffset -> endOffset - startOffset);
    }

    private Optional<Integer> getEndOfClassSpecifier() throws BadLocationException {
        final Optional<Integer> posIndex = getBeginOfTestDouble();
        if (posIndex.isPresent()) {
            for (int i = posIndex.get(); i >= 0; i--) {
                if (isWhitespace(i)) {
                    return Optional.of(i);
                }
            }
        }
        return Optional.empty();
    }
}
