package ch.hsr.ifs.cute.mockator.mockobject.qf;

import ch.hsr.ifs.cute.mockator.base.i18n.I18N;
import ch.hsr.ifs.cute.mockator.project.properties.LinkedEditModeStrategy;


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
