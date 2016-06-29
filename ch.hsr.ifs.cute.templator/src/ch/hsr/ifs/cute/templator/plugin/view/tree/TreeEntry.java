package ch.hsr.ifs.cute.templator.plugin.view.tree;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import ch.hsr.ifs.cute.templator.plugin.logger.TemplatorException;
import ch.hsr.ifs.cute.templator.plugin.util.ILoadingProgress;
import ch.hsr.ifs.cute.templator.plugin.view.components.AsyncEntryLoader;
import ch.hsr.ifs.cute.templator.plugin.view.components.ProblemsDialog;
import ch.hsr.ifs.cute.templator.plugin.view.components.RectangleContextMenu;
import ch.hsr.ifs.cute.templator.plugin.view.components.SourceEntry;
import ch.hsr.ifs.cute.templator.plugin.view.interfaces.IActionButtonCallback;
import ch.hsr.ifs.cute.templator.plugin.view.interfaces.IAsyncLoadCallback;
import ch.hsr.ifs.cute.templator.plugin.view.interfaces.IRectangleContextMenuActionHandler;
import ch.hsr.ifs.cute.templator.plugin.view.interfaces.ISubNameClickCallback;
import ch.hsr.ifs.cute.templator.plugin.view.interfaces.ITreeViewController;
import ch.hsr.ifs.cute.templator.plugin.viewdata.ViewData;

public class TreeEntry extends SourceEntry implements ISubNameClickCallback, IActionButtonCallback,
		IRectangleContextMenuActionHandler, IAsyncLoadCallback {

	private ITreeViewController controller;
	private ViewData data;

	public TreeEntry(Composite parent, ITreeViewController controller, ViewData data) {
		super(parent, controller, SWT.BORDER);

		this.controller = controller;
		this.data = data;

		AsyncEntryLoader asyncLoader = new AsyncEntryLoader(this, data.getTitle(), this);
		asyncLoader.start();
		controller.reflow();
	}

	@Override
	public void loadOperation(ILoadingProgress loadingProgress) throws TemplatorException {
		data.prepareForView(loadingProgress);
	}

	@Override
	public void loadComplete() {
		createComponents();

		setActionButtonCallback(this);
		setSourceText(data.getDataText());
		setDescription(data.getTitle(), data.getDescription());
		setRectangleMap(data.getSubSegments(), this);

		layout();
		setSize(calculateOptimalSize());

		minSize = calculateMinimizedSize();
		controller.reflow();
		controller.scrollToEntry(this);
	}

	@Override
	public void loadException(Throwable e) {
		controller.closeEntry(this);
		controller.reflow();
	}

	@Override
	public void nameClicked(int nameIndex, ClickAction clickAction) {
		switch (clickAction) {
		case CTRL_LEFT_CLICK:
			data.navigateToSubName(nameIndex);
			break;
		case LEFT_CLICK:
			openName(nameIndex);
			break;
		case RIGHT_CLICK:
			new RectangleContextMenu(this, nameIndex, this);
			break;
		default:
			break;
		}
	}

	@Override
	public void contextActionPerformed(int nameIndex, RectangleContextAction action) {
		switch (action) {
		case GO_TO_SOURCE:
			data.navigateToSubName(nameIndex);
			break;
		case OPEN_CLOSE:
			openName(nameIndex);
			break;
		default:
			break;
		}
	}

	@Override
	public void actionPerformed(ButtonAction action) {
		switch (action) {
		case MINIMIZE:
			minimize();
			break;
		default:
		case MAXIMIZE:
			maximize();
			break;
		case CLOSE:
			controller.closeEntry(this);
			break;
		case GO_TO_SOURCE:
			data.navigateToName();
			break;
		case CLOSE_ALL:
			controller.closeAllSubEntries(this);
			break;
		case MINIMIZE_ALL:
			controller.minimizeAllSubEntries(this);
			break;
		case MAXIMIZE_ALL:
			controller.maximizeAllSubEntries(this);
			break;
		case SEARCH:
			searchBar.show();
			break;
		case PROBLEMS:
			openProblemsDialog();
			break;
		}
	}

	private void openName(int nameIndex) {
		ViewData subData = data.getSubNameData(nameIndex);
		controller.addSubEntry(this, nameIndex, subData);
	}

	public void maximize() {
		setSize(calculateOptimalSize());
		controller.reflow();
	}

	public void minimize() {
		setSize(calculateMinimizedSize());
		controller.reflow();
	}

	private void openProblemsDialog() {
		ProblemsDialog problemsDialog = new ProblemsDialog(this.getShell(), data.getSubNameErrors());
		problemsDialog.open();
	}

	@Override
	protected void onScroll() {
		controller.entryScrolled(this);
	}
}
