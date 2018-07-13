package ch.hsr.ifs.mockator.plugin.testdouble.creation.staticpoly;

import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.mockator.plugin.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.mockator.plugin.testdouble.creation.AbstractTestDoubleLinkedMode;


class StaticPolyTestDoubleSupport extends AbstractTestDoubleLinkedMode {

   public StaticPolyTestDoubleSupport(final ChangeEdit edit, final IDocument document, final String newClassName) {
      super(edit, document, newClassName);
   }
}
