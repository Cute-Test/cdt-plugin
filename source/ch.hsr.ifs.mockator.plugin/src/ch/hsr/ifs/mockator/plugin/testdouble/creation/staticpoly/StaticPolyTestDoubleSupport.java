package ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly;

import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.AbstractTestDoubleLinkedMode;

class StaticPolyTestDoubleSupport extends AbstractTestDoubleLinkedMode {

  public StaticPolyTestDoubleSupport(ChangeEdit edit, IDocument document, String newClassName) {
    super(edit, document, newClassName);
  }
}
