package ch.hsr.ifs.templator.plugin.view.rendering;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Point;

import ch.hsr.ifs.templator.plugin.util.ColorPalette;

public class LineRenderer {

	public void draw(PaintEvent event, Point start, Point end, int colorId, int lineStyle, int lineWidth) {

		event.gc.setLineStyle(lineStyle);
		event.gc.setLineWidth(lineWidth);
		event.gc.setForeground(ColorPalette.getColor(colorId));
		event.gc.drawLine(start.x, start.y, end.x, end.y);
		event.gc.setLineStyle(SWT.LINE_SOLID);
	}
}
