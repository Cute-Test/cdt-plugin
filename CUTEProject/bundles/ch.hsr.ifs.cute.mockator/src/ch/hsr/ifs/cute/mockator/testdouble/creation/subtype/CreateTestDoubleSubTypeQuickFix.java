package ch.hsr.ifs.cute.mockator.testdouble.creation.subtype;

import java.util.Optional;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.iltis.core.core.resources.StringUtil;

import ch.hsr.ifs.iltis.cpp.core.resources.info.MarkerInfo;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.infos.CreateTestDoubleSubTypeInfo;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.ChangeEdit;
import ch.hsr.ifs.cute.mockator.refsupport.linkededit.LinkedModeInfoCreater;
import ch.hsr.ifs.cute.mockator.refsupport.qf.MockatorRefactoring;
import ch.hsr.ifs.cute.mockator.testdouble.creation.AbstractCreateTestDoubleQuickFix;


public class CreateTestDoubleSubTypeQuickFix extends AbstractCreateTestDoubleQuickFix {

    @Override
    protected MockatorRefactoring getRefactoring(final ICElement cElement, final Optional<ITextSelection> selection, final MarkerInfo<?> info) {
        return new CreateTestDoubleSubTypeRefactoring(cElement, selection, (CreateTestDoubleSubTypeInfo) info);
    }

    @Override
    protected Optional<LinkedModeInfoCreater> getLinkedModeCreator(final ChangeEdit edit, final IDocument doc,
            final MockatorRefactoring refactoring) {
        final String newClassName = StringUtil.capitalize(getNameOfNewClass());
        final LinkedModeInfoCreater creator = new SubtypePolyTestDoubleSupport(edit, doc, newClassName);
        return Optional.of(creator);
    };

    @Override
    protected String getQfLabel() {
        return I18N.CreateObjectSeamQuickfix;
    }

    @Override
    protected CreateTestDoubleSubTypeInfo getMarkerInfo(final IMarker marker) {
        return MarkerInfo.fromCodanProblemMarker(CreateTestDoubleSubTypeInfo::new, marker);
    }
}
