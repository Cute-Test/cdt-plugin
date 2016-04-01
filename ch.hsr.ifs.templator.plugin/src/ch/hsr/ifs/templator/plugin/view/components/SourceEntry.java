package ch.hsr.ifs.templator.plugin.view.components;

import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import java.util.List;
import java.util.Map;

import ch.hsr.ifs.templator.plugin.view.interfaces.IActionButtonCallback;
import ch.hsr.ifs.templator.plugin.view.interfaces.ISubNameClickCallback;
import ch.hsr.ifs.templator.plugin.view.interfaces.ITreeViewController;

public class SourceEntry extends ResizableComposite {

	public static final Point DEFAULT_SIZE = new Point(600, 750);
	private static final Point SIZE_INCREASE = new Point(40, 30);

	private EntryHeader entryHeader;

	private SourceTextField sourceText;
	private ScrolledComposite scrolledComposite;
	protected SearchBar searchBar;

	private RectangleCollection rectangleCollection;

	public SourceEntry(Composite parent, ITreeViewController controller, int style) {
		super(parent, controller, style);

		setLayout(new GridLayout());
	}

	protected void createComponents() {

		entryHeader = new EntryHeader(this, SWT.NONE);

		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		entryHeader.setLayoutData(gridData);

		scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		scrolledComposite.setLayoutData(gridData);

		sourceText = new SourceTextField(scrolledComposite);

		scrolledComposite.setLayout(new FillLayout());
		scrolledComposite.setContent(sourceText.getTextWidget());
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		scrolledComposite.getVerticalBar().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onScroll();
			}
		});

		searchBar = new SearchBar(this, sourceText.getTextWidget(), scrolledComposite);
	}

	protected void setDescription(String titleText, List<String> descriptions) {
		entryHeader.setDescription(titleText, descriptions);
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		refreshSize();
	}

	@Override
	public void setSize(Point size) {
		setSize(size.x, size.y);
	}

	public void refreshSize() {
		if (entryHeader != null) {
			entryHeader.updateSize();
		}
	}

	protected void setSourceText(String text) {
		sourceText.setText(text);
		Point optimalTextSize = sourceText.getTextWidget().computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolledComposite.setMinSize(optimalTextSize);
		searchBar.setSearchText(text);
	}

	protected void setRectangleMap(Map<Integer, IRegion> rectMap, ISubNameClickCallback clickCallback) {
		rectangleCollection = new RectangleCollection(sourceText.getTextWidget(), rectMap, clickCallback);
	}

	protected void setActionButtonCallback(final IActionButtonCallback callback) {
		entryHeader.setActionButtonCallback(callback);
	}

	public int getRectOffset(int index) {
		Rectangle rect = rectangleCollection.get(index);
		int scrollSelection = scrolledComposite.getVerticalBar().getSelection();
		int headerHeight = entryHeader.getSize().y + searchBar.getHeight();
		return rect.y + rect.height / 2 + headerHeight + 15 - scrollSelection;
	}

	public int getRectHeight(int index) {
		return rectangleCollection.get(index).height;
	}

	protected Point calculateOptimalSize() {
		Point optimalTextSize = sourceText.getTextWidget().computeSize(SWT.DEFAULT, SWT.DEFAULT);

		int preferredWidth = optimalTextSize.x + SIZE_INCREASE.x;
		int preferredHeight = optimalTextSize.y + entryHeader.getSize().y + SIZE_INCREASE.y;

		preferredWidth = preferredWidth > minSize.x ? preferredWidth : minSize.x;

		int width = preferredWidth < DEFAULT_SIZE.x ? preferredWidth : DEFAULT_SIZE.x;
		int height = preferredHeight < DEFAULT_SIZE.y ? preferredHeight : DEFAULT_SIZE.y;

		return new Point(width, height);
	}

	protected Point calculateMinimizedSize() {
		int width = minSize.x;
		int height = entryHeader.getSize().y + 14;
		return new Point(width, height);
	}

	protected void onScroll() {
	}
}
