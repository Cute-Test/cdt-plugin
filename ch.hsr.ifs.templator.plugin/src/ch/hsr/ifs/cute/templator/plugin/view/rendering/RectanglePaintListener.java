package ch.hsr.ifs.cute.templator.plugin.view.rendering;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import ch.hsr.ifs.cute.templator.plugin.util.ColorPalette;

public class RectanglePaintListener implements Listener {

	private List<Rectangle> rects;

	public RectanglePaintListener(Composite parent, List<Rectangle> collection) {
		this.rects = collection;

		parent.addListener(SWT.Paint, this);
	}

	@Override
	public void handleEvent(Event event) {

		int colorId = 0;

		for (Rectangle rect : rects) {
			event.gc.setForeground(ColorPalette.getColor(colorId++));
			event.gc.drawRectangle(rect);
		}

	}
}
