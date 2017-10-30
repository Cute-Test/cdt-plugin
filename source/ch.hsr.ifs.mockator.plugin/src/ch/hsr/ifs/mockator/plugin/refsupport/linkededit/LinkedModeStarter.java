package ch.hsr.ifs.mockator.plugin.refsupport.linkededit;

import java.util.List;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.cdt.internal.ui.viewsupport.LinkedProposalModelPresenter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ISourceViewer;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;


@SuppressWarnings("restriction")
public class LinkedModeStarter implements F1V<LinkedModeInfoCreater> {

   @Override
   public void apply(final LinkedModeInfoCreater infoCreator) {
      final List<LinkedProposalPositionGroup> groups = infoCreator.createLinkedModeInfo().getGroups();

      if (groups.isEmpty()) {
         return;
      }

      final LinkedProposalModel model = createLinkedModel(groups);

      UiUtil.getActiveCEditor().ifPresent((ceditor) -> {
         try {
            final ISourceViewer viewer = ceditor.getViewer();
            new LinkedProposalModelPresenter().enterLinkedMode(viewer, ceditor, model);
         }
         catch (final BadLocationException e) {}
      });
   }

   private static LinkedProposalModel createLinkedModel(final List<LinkedProposalPositionGroup> groups) {
      final LinkedProposalModel model = new LinkedProposalModel();

      for (final LinkedProposalPositionGroup group : groups) {
         model.addPositionGroup(group);
      }

      return model;
   }
}
