package ch.hsr.ifs.mockator.plugin.refsupport.linkededit;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.list;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import ch.hsr.ifs.iltis.core.functional.OptHelper;

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;


public class ChangeEdit {

   private final List<TextEdit> sources;

   public ChangeEdit(final Change change) {
      sources = list();
      final Change[] underlyingChanges = collectUnderlyingChanges(change);
      initTextEditSource(underlyingChanges);
   }

   private void initTextEditSource(final Change[] changes) {
      for (final Change change : changes) {
         Assert.instanceOf(change, TextChange.class, "Expected text change");
         final TextEdit[] textEdits = ((TextChange) change).getEdit().getChildren();

         for (final TextEdit edit : textEdits) {
            if (isReplaceOrInsertEdit(edit)) {
               sources.add(edit);
            }
         }
      }
   }

   public int getOffset(final String linkedModeText) {
      return OptHelper.returnIfPresentElse(getEdit(linkedModeText), (edit) -> edit.getOffset(), () -> 0);
   }

   public Optional<Integer> getAbsoluteIndex(final String linkedModeText, final String text) {
      return OptHelper.returnIfPresentElseEmpty(getText(linkedModeText), (oText) -> Optional.of(getOffset(linkedModeText) + oText.indexOf(text)));
   }

   public Optional<String> getText(final String text) {
      return OptHelper.returnIfPresentElseEmpty(getEdit(text), (textEdit) -> textEdit instanceof ReplaceEdit ? Optional.of(((ReplaceEdit) textEdit)
            .getText()) : Optional.of(((InsertEdit) textEdit).getText()));
   }

   private static Change[] collectUnderlyingChanges(final Change change) {
      if (change instanceof NullChange) { return array(); }

      Assert.instanceOf(change, CompositeChange.class, "Composite change expected");
      final Change[] subChanges = ((CompositeChange) change).getChildren();
      Assert.isFalse(subChanges.length == 0, "No changes passed");
      final Change fstChange = subChanges[0];

      if (fstChange instanceof CompositeChange) {
         return ((CompositeChange) fstChange).getChildren();
      } else if (fstChange instanceof TextChange) { return array(fstChange); }

      throw new MockatorException("Unsupported change object passed");
   }

   private static boolean isReplaceOrInsertEdit(final TextEdit edit) {
      return edit instanceof ReplaceEdit || edit instanceof InsertEdit;
   }

   private Optional<TextEdit> getEdit(final String text) {
      for (final TextEdit source : sources) {
         if (isMatchingReplaceEdit(source, text) || isMatchingInsertEdit(source, text)) { return Optional.of(source); }
      }
      return Optional.empty();
   }

   public Optional<ReplaceEdit> getNonMatchingReplaceEdit(final Pattern pattern) {
      for (final TextEdit source : sources) {
         if (source instanceof ReplaceEdit) {
            final ReplaceEdit replaceEdit = (ReplaceEdit) source;

            if (!pattern.matcher(replaceEdit.getText()).find()) { return Optional.of(replaceEdit); }
         }
      }
      return Optional.empty();
   }

   // why is getText not defined in its parent class TextEdit?
   private static boolean isMatchingReplaceEdit(final TextEdit edit, final String text) {
      return edit instanceof ReplaceEdit && ((ReplaceEdit) edit).getText().indexOf(text) > -1;
   }

   private static boolean isMatchingInsertEdit(final TextEdit edit, final String text) {
      return edit instanceof InsertEdit && ((InsertEdit) edit).getText().indexOf(text) > -1;
   }
}
