package ch.hsr.ifs.mockator.plugin.refsupport.linkededit;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.maybe;
import static ch.hsr.ifs.mockator.plugin.base.maybe.Maybe.none;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;
import ch.hsr.ifs.mockator.plugin.base.maybe.Maybe;

public class ChangeEdit {
  private final List<TextEdit> sources;

  public ChangeEdit(Change change) {
    sources = list();
    Change[] underlyingChanges = collectUnderlyingChanges(change);
    initTextEditSource(underlyingChanges);
  }

  private void initTextEditSource(Change[] changes) {
    for (Change change : changes) {
      Assert.instanceOf(change, TextChange.class, "Expected text change");
      TextEdit[] textEdits = ((TextChange) change).getEdit().getChildren();

      for (TextEdit edit : textEdits) {
        if (isReplaceOrInsertEdit(edit)) {
          sources.add(edit);
        }
      }
    }
  }

  public int getOffset(String linkedModeText) {
    for (TextEdit optEdit : getEdit(linkedModeText))
      return optEdit.getOffset();
    return 0;
  }

  public Maybe<Integer> getAbsoluteIndex(String linkedModeText, String text) {
    for (String optText : getText(linkedModeText)) {
      int offset = getOffset(linkedModeText);
      return maybe(offset + optText.indexOf(text));
    }
    return none();
  }

  public Maybe<String> getText(String text) {
    for (TextEdit textEdit : getEdit(text)) {
      if (textEdit instanceof ReplaceEdit)
        return maybe(((ReplaceEdit) textEdit).getText());

      return maybe(((InsertEdit) textEdit).getText());
    }
    return none();
  }

  private static Change[] collectUnderlyingChanges(Change change) {
    if (change instanceof NullChange)
      return array();

    Assert.instanceOf(change, CompositeChange.class, "Composite change expected");
    Change[] subChanges = ((CompositeChange) change).getChildren();
    Assert.isFalse(subChanges.length == 0, "No changes passed");
    Change fstChange = subChanges[0];

    if (fstChange instanceof CompositeChange)
      return ((CompositeChange) fstChange).getChildren();
    else if (fstChange instanceof TextChange)
      return array(fstChange);

    throw new MockatorException("Unsupported change object passed");
  }

  private static boolean isReplaceOrInsertEdit(TextEdit edit) {
    return edit instanceof ReplaceEdit || edit instanceof InsertEdit;
  }

  private Maybe<TextEdit> getEdit(String text) {
    for (TextEdit source : sources) {
      if (isMatchingReplaceEdit(source, text) || isMatchingInsertEdit(source, text))
        return maybe(source);
    }
    return none();
  }

  public Maybe<ReplaceEdit> getNonMatchingReplaceEdit(Pattern pattern) {
    for (TextEdit source : sources) {
      if (source instanceof ReplaceEdit) {
        ReplaceEdit replaceEdit = (ReplaceEdit) source;

        if (!pattern.matcher(replaceEdit.getText()).find())
          return maybe(replaceEdit);
      }
    }
    return none();
  }

  // why is getText not defined in its parent class TextEdit?
  private static boolean isMatchingReplaceEdit(TextEdit edit, String text) {
    return edit instanceof ReplaceEdit && ((ReplaceEdit) edit).getText().indexOf(text) > -1;
  }

  private static boolean isMatchingInsertEdit(TextEdit edit, String text) {
    return edit instanceof InsertEdit && ((InsertEdit) edit).getText().indexOf(text) > -1;
  }
}
