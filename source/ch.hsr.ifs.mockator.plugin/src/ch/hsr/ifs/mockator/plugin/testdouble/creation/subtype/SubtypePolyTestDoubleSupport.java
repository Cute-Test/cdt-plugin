package ch.hsr.ifs.mockator.plugin.testdouble.creation.subtype;

import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.LinkedModeInformation;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.AbstractTestDoubleLinkedMode;

class SubtypePolyTestDoubleSupport extends AbstractTestDoubleLinkedMode {

  public SubtypePolyTestDoubleSupport(ChangeEdit edit, IDocument document, String newClassName) {
    super(edit, document, " " + newClassName);
  }

  @Override
  public LinkedModeInformation createLinkedModeInfo() {
    LinkedModeInformation lm = super.createLinkedModeInfo();

    for (Integer optBeginIdx : getBeginOfTestDouble()) {
      lm.addPosition(optBeginIdx, newClassName.length());
    }

    return lm;
  }
}
