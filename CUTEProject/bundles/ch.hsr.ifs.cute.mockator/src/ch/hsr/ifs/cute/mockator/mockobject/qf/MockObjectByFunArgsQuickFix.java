package ch.hsr.ifs.cute.mockator.mockobject.qf;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;


public class MockObjectByFunArgsQuickFix extends MockObjectQuickFix {

    @Override
    protected LinkedEditModeStrategy getLinkedEditStrategy() {
        return LinkedEditModeStrategy.ChooseArguments;
    }

    @Override
    protected String getResolutionLabelHeader() {
        return I18N.RecordMemFunsByChoosingFunArgsQuickfix;
    }
}
