package ch.hsr.ifs.mockator.plugin.testdouble.creation;

import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInformation;

@SuppressWarnings("restriction")
public abstract class AbstractTestDoubleLinkedMode implements LinkedModeInfoCreater {
  private static final Proposal[] TEST_DOUBLE_PROPOSALS;
  protected final String newClassName;
  private final ChangeEdit edit;
  private final IDocument document;

  static {
    TEST_DOUBLE_PROPOSALS =
        new Proposal[] {
            new Proposal("class", CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CLASS), 0),
            new Proposal("struct", CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_STRUCT), 0)};
  }

  public AbstractTestDoubleLinkedMode(ChangeEdit edit, IDocument document, String newClassName) {
    this.edit = edit;
    this.document = document;
    this.newClassName = newClassName;
  }

  @Override
  public LinkedModeInformation createLinkedModeInfo() {
    LinkedModeInformation lm = new LinkedModeInformation();

    try {
      for (Integer optOffset : getBeginOfClassDefinition()) {
        for (Integer optLength : getClassSpecifierLength(optOffset)) {
          lm.addPosition(optOffset, optLength);
          lm.addProposal(optOffset, TEST_DOUBLE_PROPOSALS);
        }
      }
    } catch (BadLocationException e) {
    }

    return lm;
  }

  private Maybe<Integer> getBeginOfClassDefinition() throws BadLocationException {
    for (int optOffset : getBeginOfTestDouble()) {
      for (; optOffset >= 0; optOffset--) {
        if (isNewline(optOffset)) {
          break;
        }
      }

      for (; optOffset <= document.getLength(); optOffset++) {
        if (!isWhitespace(optOffset))
          return maybe(optOffset);
      }
    }

    return none();
  }

  protected Maybe<Integer> getBeginOfTestDouble() {
    return edit.getAbsoluteIndex(newClassName, newClassName);
  }

  private boolean isNewline(int offset) throws BadLocationException {
    return document.getChar(offset) == '\n';
  }

  private boolean isWhitespace(int offset) throws BadLocationException {
    return Character.isWhitespace(document.getChar(offset));
  }

  private Maybe<Integer> getClassSpecifierLength(Integer startOffset) throws BadLocationException {
    for (Integer optEndOffset : getEndOfClassSpecifier())
      return maybe(optEndOffset - startOffset);

    return none();
  }

  private Maybe<Integer> getEndOfClassSpecifier() throws BadLocationException {
    for (Integer posIndex : getBeginOfTestDouble()) {
      for (int i = posIndex; i >= 0; i--) {
        if (isWhitespace(i))
          return maybe(i);
      }
    }
    return none();
  }
}
