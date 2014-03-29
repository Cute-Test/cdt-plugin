package ch.hsr.ifs.mockator.plugin.mockobject.qf;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;

public class MockObjectByFunsQuickFix extends MockObjectQuickFix {

  @Override
  protected LinkedEditModeStrategy getLinkedEditStrategy() {
    return LinkedEditModeStrategy.ChooseFunctions;
  }

  @Override
  protected String getResolutionLabelHeader() {
    return I18N.RecordMemFunsByChoosingFunSignaturesQuickfix;
  }
}
