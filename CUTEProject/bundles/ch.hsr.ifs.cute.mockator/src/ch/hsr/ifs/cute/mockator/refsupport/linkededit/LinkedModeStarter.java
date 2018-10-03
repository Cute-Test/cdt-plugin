package ch.hsr.ifs.cute.mockator.refsupport.linkededit;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup;
import org.eclipse.cdt.internal.ui.viewsupport.LinkedProposalModelPresenter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ISourceViewer;

import ch.hsr.ifs.cute.mockator.base.util.UiUtil;


@SuppressWarnings("restriction")
public class LinkedModeStarter implements Consumer<LinkedModeInfoCreater> {

    @Override
    public void accept(final LinkedModeInfoCreater infoCreator) {
        final List<LinkedProposalPositionGroup> groups = infoCreator.createLinkedModeInfo().getGroups();

        if (groups.isEmpty()) {
            return;
        }

        final LinkedProposalModel model = createLinkedModel(groups);

        UiUtil.getActiveCEditor().ifPresent((ceditor) -> {
            try {
                final ISourceViewer viewer = ceditor.getViewer();
                new LinkedProposalModelPresenter().enterLinkedMode(viewer, ceditor, model);
            } catch (final BadLocationException e) {}
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
