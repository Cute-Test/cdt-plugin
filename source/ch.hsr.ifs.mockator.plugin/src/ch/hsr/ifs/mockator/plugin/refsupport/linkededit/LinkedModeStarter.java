package ch.hsr.ifs.mockator.plugin.refsupport.linkededit;

import java.util.List;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.viewsupport.LinkedProposalModelPresenter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ISourceViewer;

import ch.hsr.ifs.mockator.plugin.base.functional.F1V;
import ch.hsr.ifs.mockator.plugin.base.util.UiUtil;

@SuppressWarnings("restriction")
public class LinkedModeStarter implements F1V<LinkedModeInfoCreater> {

  @Override
  public void apply(LinkedModeInfoCreater infoCreator) {
    List<LinkedProposalPositionGroup> groups = infoCreator.createLinkedModeInfo().getGroups();

    if (groups.isEmpty())
      return;

    LinkedProposalModel model = createLinkedModel(groups);

    for (CEditor optCEditor : UiUtil.getActiveCEditor()) {
      try {
        ISourceViewer viewer = optCEditor.getViewer();
        new LinkedProposalModelPresenter().enterLinkedMode(viewer, optCEditor, model);
      } catch (BadLocationException e) {
      }
    }
  }

  private static LinkedProposalModel createLinkedModel(List<LinkedProposalPositionGroup> groups) {
    LinkedProposalModel model = new LinkedProposalModel();

    for (LinkedProposalPositionGroup group : groups) {
      model.addPositionGroup(group);
    }

    return model;
  }
}
