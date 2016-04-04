package ch.hsr.ifs.cute.templator.plugin.view.rendering;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Point;

import ch.hsr.ifs.cute.templator.plugin.util.ColorPalette;

public class BezierRenderer {

	// commented points are an example of possible coords for a half bezier path
	private Point c1; // = new Point(120, 100);
	private Point c2; // = new Point(140, 100);
	private Point s; // = new Point(100, 100);
	private Point e; // = new Point(140, 300);

	private static final int LINE_STYLE = SWT.LINE_SOLID;
	private static final int LINE_WIDTH = 1;

	public void draw(PaintEvent event, Point start, Point end, int colorId) {

		int width = (end.x - start.x);
		int height = (end.y - start.y);

		calcUpperCoords(start, end, width, height);
		drawSection(event, colorId);

		calcLowerCoords(start, end, width, height);
		drawSection(event, colorId);
	}

	private void calcUpperCoords(Point start, Point end, int width, int height) {
		s = start;
		e = new Point(start.x + width / 2, start.y + height / 2);
		c1 = new Point(s.x + width / 6, s.y);
		c2 = new Point(e.x - width / 6, s.y);
	}

	private void calcLowerCoords(Point start, Point end, int width, int height) {
		s = new Point(start.x + width / 2, start.y + height / 2);
		e = end;
		c1 = new Point(s.x + width / 6, e.y);
		c2 = new Point(e.x - width / 6, e.y);
	}

	private void drawSection(PaintEvent event, int colorId) {
		Path path = new Path(event.display);
		path.moveTo(s.x, s.y);
		path.cubicTo(c1.x, c1.y, c2.x, c2.y, e.x, e.y);

		event.gc.setLineStyle(LINE_STYLE);
		event.gc.setLineWidth(LINE_WIDTH);
		event.gc.setForeground(ColorPalette.getColor(colorId));
		event.gc.drawPath(path);
		event.gc.setLineStyle(SWT.LINE_SOLID);
		path.dispose();
	}
}
