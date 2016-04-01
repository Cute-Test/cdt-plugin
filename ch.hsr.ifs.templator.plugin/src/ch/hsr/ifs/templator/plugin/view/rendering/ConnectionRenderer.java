package ch.hsr.ifs.templator.plugin.view.rendering;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.templator.plugin.view.interfaces.IConnection;
import ch.hsr.ifs.templator.plugin.view.interfaces.IConnectionCollection;

public class ConnectionRenderer implements PaintListener {

	private static final int VERTICAL_LINE_WIDTH = 2;

	private IConnectionCollection connectionCollection;

	private BezierRenderer bezierRenderer = new BezierRenderer();
	private LineRenderer lineRenderer = new LineRenderer();

	public ConnectionRenderer(Composite composite, IConnectionCollection entryCollection) {

		this.connectionCollection = entryCollection;

		composite.addPaintListener(this);
	}

	@Override
	public void paintControl(PaintEvent e) {
		for (IConnection connection : connectionCollection.getConnections()) {
			if (connection.getConnectionStart() != null) {
				drawBezierConnection(e, connection);
				drawDestinationVerticalLine(e, connection);
				drawSourceVerticalLine(e, connection);
			}
		}
	}

	private void drawSourceVerticalLine(PaintEvent e, IConnection connection) {
		Composite sourceComposite = connection.getConnectionStart();

		Point midPoint = sourceComposite.getLocation();
		midPoint.y += connection.getConnectionStartRectOffset();
		midPoint.x += sourceComposite.getSize().x + VERTICAL_LINE_WIDTH;

		int rectHeight = connection.getConnectionStartRectHeight();

		int startY = midPoint.y - rectHeight / 2;
		int endY = midPoint.y + rectHeight / 2;

		startY = limitY(startY, sourceComposite);
		endY = limitY(endY, sourceComposite);

		Point start = new Point(midPoint.x, startY);
		Point end = new Point(midPoint.x, endY);

		int colorId = connection.getNameIndex();
		lineRenderer.draw(e, start, end, colorId, SWT.LINE_SOLID, VERTICAL_LINE_WIDTH);
	}

	private void drawDestinationVerticalLine(PaintEvent e, IConnection connection) {
		Composite destinationComposite = connection.getConnectionEnd();

		Point start = destinationComposite.getLocation();
		Point end = destinationComposite.getLocation();

		start.x -= VERTICAL_LINE_WIDTH;
		end.y += destinationComposite.getSize().y;
		end.x -= VERTICAL_LINE_WIDTH;

		int colorId = connection.getNameIndex();
		lineRenderer.draw(e, start, end, colorId, SWT.LINE_SOLID, VERTICAL_LINE_WIDTH);
	}

	private void drawBezierConnection(PaintEvent e, IConnection connection) {
		Composite sourceEntry = connection.getConnectionStart();
		Composite destinationEntry = connection.getConnectionEnd();

		Point start = sourceEntry.getLocation();
		Point end = destinationEntry.getLocation();

		start.y += connection.getConnectionStartRectOffset();
		start.x += sourceEntry.getSize().x + VERTICAL_LINE_WIDTH;
		end.y += destinationEntry.getSize().y / 2;
		end.x -= VERTICAL_LINE_WIDTH;

		start.y = limitY(start.y, sourceEntry);

		int colorId = connection.getNameIndex();
		bezierRenderer.draw(e, start, end, colorId);
	}

	private int limitY(int pointY, Composite composite) {
		if (pointY < composite.getLocation().y) {
			pointY = composite.getLocation().y;
		}
		if (pointY > composite.getLocation().y + composite.getSize().y - 1) {
			pointY = composite.getLocation().y + composite.getSize().y - 1;
		}
		return pointY;
	}
}
