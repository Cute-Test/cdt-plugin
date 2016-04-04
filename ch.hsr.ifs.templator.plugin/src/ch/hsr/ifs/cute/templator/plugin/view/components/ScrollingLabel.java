package ch.hsr.ifs.cute.templator.plugin.view.components;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.hsr.ifs.cute.templator.plugin.util.ImageCache;
import ch.hsr.ifs.cute.templator.plugin.util.ImageCache.ImageID;

public class ScrollingLabel {

	private String originalText;
	private int visibleLabelCharCount = -1;
	private int maxCharOffset = 0;
	private float currentCharOffset = 0;

	private Point lastMousePos = null;

	private Cursor openHandCursor;
	private Cursor closedHandCursor;

	private MouseTrackListener trackListener;
	private MouseListener mouseListener;
	private MouseMoveListener mouseMoveListener;

	private Label label;
	private Point optimalTextSize;

	private boolean isScrollable = false;
	private boolean isDragging = false;

	public ScrollingLabel(final Composite parent, int style, int width, String text) {

		// Append whitespaces because float is inexact
		text += "   ";
		originalText = text;

		label = new Label(parent, style);
		label.setText(text);
		optimalTextSize = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		Image openHand = ImageCache.get(ImageID.HAND_OPEN);
		Image closedHand = ImageCache.get(ImageID.HAND_CLOSED);

		openHandCursor = new Cursor(parent.getDisplay(), openHand.getImageData(), 12, 14);
		closedHandCursor = new Cursor(parent.getDisplay(), closedHand.getImageData(), 12, 14);

		trackListener = new MouseTrackAdapter() {
			@Override
			public void mouseExit(MouseEvent e) {
				label.setCursor(null);
			}

			@Override
			public void mouseEnter(MouseEvent e) {
				label.setCursor(openHandCursor);
			}
		};
		mouseListener = new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				isDragging = false;
				label.setCursor(openHandCursor);
			}

			@Override
			public void mouseDown(MouseEvent e) {
				lastMousePos = new Point(e.x, e.y);
				isDragging = true;
				label.setCursor(closedHandCursor);
			}
		};
		mouseMoveListener = new MouseMoveListener() {
			@Override
			public void mouseMove(MouseEvent e) {
				if (isDragging) {
					Point currentMousePos = new Point(e.x, e.y);
					int relativeX = currentMousePos.x - lastMousePos.x;
					currentCharOffset += relativeX * -0.1f;

					updateLabelText();

					lastMousePos = currentMousePos;
				}
			}

		};
	}

	public void setWidth(int width) {
		if (optimalTextSize.x > width) {

			float sizePercentage = (float) (width) / optimalTextSize.x;
			float numChars = originalText.length() * sizePercentage;

			visibleLabelCharCount = (int) numChars;
			maxCharOffset = originalText.length() - visibleLabelCharCount;
			updateLabelText();

			if (!isScrollable) {
				label.addMouseTrackListener(trackListener);
				label.addMouseListener(mouseListener);
				label.addMouseMoveListener(mouseMoveListener);

				isScrollable = true;
			}
		} else if (isScrollable) {

			label.setText(originalText);

			label.removeMouseTrackListener(trackListener);
			label.removeMouseListener(mouseListener);
			label.removeMouseMoveListener(mouseMoveListener);

			isScrollable = false;
		}
	}

	private void updateLabelText() {
		currentCharOffset = currentCharOffset < 0 ? 0 : currentCharOffset;
		currentCharOffset = currentCharOffset > maxCharOffset ? maxCharOffset : currentCharOffset;

		int intCharOffset = (int) currentCharOffset;
		String text = originalText.substring(intCharOffset, intCharOffset + visibleLabelCharCount);
		label.setText(text);
	}

	public Label getLabel() {
		return label;
	}
}
