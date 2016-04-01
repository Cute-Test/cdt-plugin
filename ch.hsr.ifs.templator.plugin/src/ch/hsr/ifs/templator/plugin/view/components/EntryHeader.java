package ch.hsr.ifs.templator.plugin.view.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.ifs.templator.plugin.view.interfaces.IActionButtonCallback;
import ch.hsr.ifs.templator.plugin.view.interfaces.IActionButtonCallback.ButtonAction;

public class EntryHeader extends Composite {

	private static final int ACTION_BUTTON_MARGIN = 50;

	private Composite parent;

	private Composite descriptionArea;
	private Composite filler;

	private ScrollingLabel titleLabel;
	private List<ScrollingLabel> descLabels;

	private ActionButtons actionButtons;
	private Point actionButtonsSize;

	public EntryHeader(Composite parent, int style) {
		super(parent, style);

		this.parent = parent;

		setLayout(new GridLayout(3, false));

		descriptionArea = new Composite(this, SWT.NONE);
		descriptionArea.setLayout(new GridLayout());
		filler = new Composite(this, SWT.NONE);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		filler.setLayoutData(gridData);
	}

	protected void setDescription(String titleText, List<String> descriptions) {

		int width = getSize().x - actionButtonsSize.x - ACTION_BUTTON_MARGIN;
		titleLabel = new ScrollingLabel(descriptionArea, SWT.NONE, width, titleText);

		descLabels = new ArrayList<>();
		for (int i = 0; i < descriptions.size(); i++) {

			ScrollingLabel descLabel = new ScrollingLabel(descriptionArea, SWT.NONE, width, descriptions.get(i));

			if (i == 0) {
				GridData gridData = new GridData();
				gridData.verticalIndent = 10;
				descLabel.getLabel().setLayoutData(gridData);
			}
			descLabels.add(descLabel);
		}
	}

	protected void setActionButtonCallback(final IActionButtonCallback callback) {
		actionButtons = new ActionButtons(this, SWT.NONE, callback);
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		actionButtons.setLayoutData(gridData);

		actionButtonsSize = actionButtons.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		MouseAdapter maximizeMouseListener = new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				callback.actionPerformed(ButtonAction.MAXIMIZE);
			}
		};
		parent.addMouseListener(maximizeMouseListener);
		addMouseListener(maximizeMouseListener);
		descriptionArea.addMouseListener(maximizeMouseListener);
		filler.addMouseListener(maximizeMouseListener);
	}

	public void updateSize() {
		int width = parent.getSize().x - actionButtonsSize.x - ACTION_BUTTON_MARGIN;

		titleLabel.setWidth(width);
		for (ScrollingLabel descLabel : descLabels) {
			descLabel.setWidth(width);
		}
		layout();
	}
}
