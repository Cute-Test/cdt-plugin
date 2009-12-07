package org.ginkgo.gcov.navigator;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

public class CoverageLabelProvider extends OwnerDrawLabelProvider {

	// private static final String BINARIES = "Binaries";

	private TreeViewerColumn column;
	private TreeViewer viewer;

	public CoverageLabelProvider(TreeViewerColumn column2,
			TreeViewer commonViewer) {
		column = column2;
		viewer = commonViewer;
	}

	// @Override
	protected void measure(Event event, Object element) {
		// TODO Ž©“®�¶�¬‚³‚ê‚½ƒ�ƒ\ƒbƒh�EƒXƒ^ƒu

	}

	// @Override
	protected void paint(Event event, Object element) {
		// TODO Ž©“®�¶�¬‚³‚ê‚½ƒ�ƒ\ƒbƒh�EƒXƒ^ƒu
		int totalLine = 133;
		int coverLine = 20;
		int offset = 2;
		float coverage = 0;
		GC gc = event.gc;

		if (element instanceof IProject) {
			try {
				IProject p = (IProject) element;
				if (p.isOpen()) {
					String n = p.getFullPath().toOSString();
					System.out.println("Project: " + n);
					totalLine = Integer.parseInt(p
							.getPersistentProperty(new QualifiedName(n,
									"totalLine")));
					coverage = Float.parseFloat(p
							.getPersistentProperty(new QualifiedName(n,
									"persent")));
					coverLine = (int) (totalLine * coverage / 100);
				} else {
					return;
				}
			} catch (NumberFormatException e) {
				coverLine = 0;
				return;
			} catch (CoreException e) {
			}
		} else if (element instanceof ICElement) {
			ICElement a = (ICElement) element;
			try {
				IProject p = a.getCProject().getProject();
				String n = a.getPath().toOSString();
				if (a.getResource() instanceof IFile
						|| (a.getResource() instanceof IFolder)) {
					if (a.getResource() instanceof IFolder)
						totalLine = Integer.parseInt(p
								.getPersistentProperty(new QualifiedName(n,
										"totalLine")));
					coverage = Float.parseFloat(p
							.getPersistentProperty(new QualifiedName(n,
									"persent")));
					coverLine = (int) (totalLine * coverage / 100);
				} else if (a instanceof IFunction) {
					IFunction func = (IFunction) a;
					totalLine = Integer.parseInt(p
							.getPersistentProperty(new QualifiedName(func
									.getSignature(), "totalLine")));
					coverage = Float.parseFloat(p
							.getPersistentProperty(new QualifiedName(func
									.getSignature(), "persent")));
					coverLine = (int) (totalLine * coverage / 100);
				} else {
					return;
				}

			} catch (NumberFormatException e) {
				coverLine = 0;
				return;
			} catch (CoreException e) {
				// TODO Ž©“®�¶�¬‚³‚ê‚½ catch ƒuƒ�ƒbƒN
				e.printStackTrace();
			}
		} else {
			return;
		}
		String str = String.format("%6.2f %% [ %3d / %3d ]", coverage,
				coverLine, totalLine);

		Display disp = viewer.getControl().getDisplay();
		Rectangle rect = new Rectangle(event.x, event.y + 1
				+ gc.textExtent(str).y, column.getColumn().getWidth(),
				event.height);
		rect = drawBevelRect(gc, rect, disp
				.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW), disp
				.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		drawCoverRect(gc, rect, (int) coverage, disp
				.getSystemColor(SWT.COLOR_GREEN));
		drawUnCoverRect(gc, rect, (int) coverage, disp
				.getSystemColor(SWT.COLOR_RED));

		gc.drawText(str, event.x + 1, event.y + offset, true);
	}

	private Rectangle drawBevelRect(GC gc, Rectangle rect, Color normalShadow,
			Color highlightShadow) {
		Color fg = gc.getForeground();
		Rectangle insideRect = new Rectangle(rect.x + 1, rect.y + 1,
				rect.width - 2, rect.height - 2);
		gc.setForeground(normalShadow);
		gc.drawLine(rect.x, rect.y, rect.x + rect.width - 1, rect.y);
		gc.drawLine(rect.x, rect.y, rect.x, rect.y + rect.height - 1);
		gc.setForeground(highlightShadow);
		gc.drawLine(rect.x + rect.width, rect.y, rect.x + rect.width, rect.y
				+ rect.height);
		gc.drawLine(rect.x, rect.y + rect.height, rect.x + rect.width, rect.y
				+ rect.height);
		gc.setForeground(fg);
		return insideRect;
	}

	private void drawUnCoverRect(GC gc, Rectangle rect, int coverage,
			Color color) {
		Color bg = gc.getBackground();
		gc.setBackground(color);
		int w = ((rect.width) * coverage) / 100;
		gc.fillRectangle(rect.x + w, rect.y, rect.width - w, rect.height);
		gc.setBackground(bg);
	}

	private void drawCoverRect(GC gc, Rectangle rect, int coverage, Color color) {
		Color bg = gc.getBackground();
		gc.setBackground(color);
		gc.setBackground(color);
		int w = ((rect.width) * coverage) / 100;
		gc.fillRectangle(rect.x, rect.y, w, rect.height);
		gc.setBackground(bg);
	}
}
