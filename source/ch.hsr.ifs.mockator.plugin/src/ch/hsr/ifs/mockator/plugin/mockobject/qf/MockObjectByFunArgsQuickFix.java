package ch.hsr.ifs.mockator.plugin.mockobject.qf;

import ch.hsr.ifs.mockator.plugin.base.i18n.I18N;
import ch.hsr.ifs.mockator.plugin.project.properties.LinkedEditModeStrategy;


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
