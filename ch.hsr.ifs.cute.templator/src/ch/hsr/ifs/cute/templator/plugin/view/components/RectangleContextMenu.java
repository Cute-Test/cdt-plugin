package ch.hsr.ifs.cute.templator.plugin.view.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ch.hsr.ifs.cute.templator.plugin.view.interfaces.IRectangleContextMenuActionHandler;
import ch.hsr.ifs.cute.templator.plugin.view.interfaces.IRectangleContextMenuActionHandler.RectangleContextAction;

public class RectangleContextMenu {

	private int rectangleIndex;
	private Menu contextMenu;
	private IRectangleContextMenuActionHandler callback;

	public RectangleContextMenu(Composite parent, int rectangleIndex, IRectangleContextMenuActionHandler callback) {

		this.contextMenu = new Menu(parent);
		this.rectangleIndex = rectangleIndex;
		this.callback = callback;

		addMenuItem(RectangleContextAction.OPEN_CLOSE);
		addMenuItem(RectangleContextAction.GO_TO_SOURCE);

		contextMenu.setVisible(true);
	}

	private void performLinkAction(RectangleContextAction action) {
		callback.contextActionPerformed(rectangleIndex, action);
	}

	private void addMenuItem(final RectangleContextAction action) {
		MenuItem item = new MenuItem(contextMenu, SWT.NONE);
		item.setText(action.getText());
		item.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				performLinkAction(action);
			}
		});
	}
}
