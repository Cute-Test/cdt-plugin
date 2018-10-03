package ch.hsr.ifs.cute.mockator.testdouble.creation.staticpoly;

import org.eclipse.jface.text.IDocument;

import ch.hsr.ifs.cute.mockator.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.cute.mockator.testdouble.creation.AbstractTestDoubleLinkedMode;


class StaticPolyTestDoubleSupport extends AbstractTestDoubleLinkedMode {

    public StaticPolyTestDoubleSupport(final ChangeEdit edit, final IDocument document, final String newClassName) {
        super(edit, document, newClassName);
    }
}
