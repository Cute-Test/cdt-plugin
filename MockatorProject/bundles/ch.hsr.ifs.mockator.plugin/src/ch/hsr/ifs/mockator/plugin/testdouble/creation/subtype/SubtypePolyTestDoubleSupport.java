package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInformation;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.AbstractTestDoubleLinkedMode;


class SubtypePolyTestDoubleSupport extends AbstractTestDoubleLinkedMode {

   public SubtypePolyTestDoubleSupport(final ChangeEdit edit, final IDocument document, final String newClassName) {
      super(edit, document, " " + newClassName);
   }

   @Override
   public LinkedModeInformation createLinkedModeInfo() {
      final LinkedModeInformation lm = super.createLinkedModeInfo();

      getBeginOfTestDouble().ifPresent((beginIdx) -> lm.addPosition(beginIdx, newClassName.length()));
      return lm;
   }
}
