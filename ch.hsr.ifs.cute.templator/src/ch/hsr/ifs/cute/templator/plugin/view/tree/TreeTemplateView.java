package ch.hsr.ifs.cute.templator.plugin.view.tree;

import java.util.List;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;

import ch.hsr.ifs.cute.templator.plugin.handler.ViewOpener;
import ch.hsr.ifs.cute.templator.plugin.view.components.GlobalToolBar;
import ch.hsr.ifs.cute.templator.plugin.view.components.ScrollAnimator;
import ch.hsr.ifs.cute.templator.plugin.view.interfaces.ITemplateView;
import ch.hsr.ifs.cute.templator.plugin.view.interfaces.ITreeViewController;
import ch.hsr.ifs.cute.templator.plugin.view.rendering.ConnectionRenderer;
import ch.hsr.ifs.cute.templator.plugin.viewdata.ViewData;

public class TreeTemplateView extends ViewPart implements ITemplateView, ITreeViewController, IShowInTarget {

	public static final String VIEW_ID = "ch.hsr.ifs.cute.templator.plugin.view.tree.TreeTemplateView";

	public static final int MARGIN = 15;
	public static final int BORDER_MARGIN = 30;
	public static final int CONNECTION_COLUMN_WIDTH = 80;

	private Composite treeComposite;
	private ScrolledForm form;

	private Composite borderExtension;

	private TreeEntryCollection entryCollection = new TreeEntryCollection();

	private ScrollAnimator scrollAnimator;

	@Override
	public void setRootData(ViewData data) {
		clear();
		TreeEntry rootEntry = new TreeEntry(treeComposite, this, data);
		entryCollection.addRoot(rootEntry);
		reflow();
	}

	@Override
	public void addSubEntry(TreeEntry parent, int nameIndex, ViewData data) {
		if (!entryCollection.remove(parent, nameIndex)) {
			TreeEntry newEntry = new TreeEntry(treeComposite, this, data);
			entryCollection.add(newEntry, parent, nameIndex);
		}
		reflow();
	}

	@Override
	public void closeEntry(TreeEntry treeEntry) {
		entryCollection.remove(treeEntry);
		reflow();
	}

	@Override
	public void createPartControl(final Composite parent) {
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.getBody().setLayout(new FillLayout());
		
		form.setText("\nNO TEMPLATE INFORMATION AVAILABLE"
				+ "\n\nPlease select one of the following types and refresh this view:"
				+ "\n\n- Function"
				+ "\n- Function Template"
				+ "\n- Class Template");
		Font font = new Font(form.getDisplay(), new FontData(form.getFont().toString(), 12, SWT.NONE));
		form.setFont(font);
		form.setForeground(form.getDisplay().getSystemColor(SWT.COLOR_BLACK));

		treeComposite = new Composite(form.getBody(), SWT.DOUBLE_BUFFERED);
		treeComposite.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		treeComposite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				treeComposite.setFocus();
			}
		});
		
		// This code enables to scroll left/right when control is pressed.
		// This does not work with OSX/Linux (scrolling gets diagonal)
		
		/*treeComposite.addListener(SWT.MouseWheel, new Listener() {
			@Override
			public void handleEvent(Event e) {
				if (e.stateMask == SWT.CONTROL) {
					Point newOrigin = form.getOrigin();
					newOrigin.x += e.count * form.getHorizontalBar().getIncrement();
					form.setOrigin(newOrigin);
				}
			}
		});*/

		borderExtension = new Composite(treeComposite, SWT.NONE);
		borderExtension.setSize(BORDER_MARGIN, BORDER_MARGIN);
		borderExtension.setVisible(false);
		// borderExtension.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_CYAN));

		scrollAnimator = new ScrollAnimator(form);
		SelectionListener stopScrollListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scrollAnimator.stopScrolling();
			}
		};
		form.getHorizontalBar().addSelectionListener(stopScrollListener);
		form.getVerticalBar().addSelectionListener(stopScrollListener);

		new ConnectionRenderer(treeComposite, entryCollection);
		new GlobalToolBar(this, getViewSite());
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void reflow() {
		recalculateLayout();
		form.reflow(true);
		treeComposite.redraw();
	}

	@Override
	public void entryScrolled(TreeEntry treeEntry) {
		treeComposite.redraw();
	}

	public void recalculateLayout() {
		int currentLeft = BORDER_MARGIN;
		int maxTop = 0;
		
		if (entryCollection.getEntries().size() == 0) {
			form.setText("\nNO TEMPLATE INFORMATION AVAILABLE"
					+ "\n\nPlease select a name of one of the following and refresh this view:"
					+ "\n\n- Function"
					+ "\n- Function Template"
					+ "\n- Class Template");
		} else {
			form.setText(null);
		}

		for (TreeSet<TreeEntry> column : entryCollection.getEntries()) {
			int maxWidth = 0;
			int currentTop = BORDER_MARGIN;

			for (TreeEntry entry : column) {
				entry.setLocation(currentLeft, currentTop);
				currentTop += entry.getSize().y + MARGIN;

				if (entry.getSize().x > maxWidth) {
					maxWidth = entry.getSize().x;
				}
			}
			currentLeft += maxWidth + CONNECTION_COLUMN_WIDTH;

			if (currentTop > maxTop) {
				maxTop = currentTop;
			}
		}
		borderExtension.setLocation(currentLeft - CONNECTION_COLUMN_WIDTH, maxTop - MARGIN);
	}

	@Override
	public boolean show(ShowInContext context) {
		ViewOpener.showTemplateInfoUnderCursor();
		return true;
	}

	@Override
	public ScrolledForm getForm() {
		return form;
	}

	@Override
	public void minimizeAll() {
		for (TreeSet<TreeEntry> column : entryCollection.getEntries()) {
			for (TreeEntry entry : column) {
				entry.minimize();
			}
		}
	}

	@Override
	public void maximizeAll() {
		for (TreeSet<TreeEntry> column : entryCollection.getEntries()) {
			for (TreeEntry entry : column) {
				entry.maximize();
			}
		}
	}

	@Override
	public void refreshFromEditor() {
		ViewOpener.showTemplateInfoUnderCursor();
	}

	@Override
	public void clear() {
		entryCollection.clear();
		reflow();
	}

	@Override
	public void closeAllSubEntries(TreeEntry treeEntry) {
		List<TreeEntry> subEntries = entryCollection.getAllSubEntries(treeEntry);
		for (TreeEntry entry : subEntries) {
			entryCollection.remove(entry);
		}
		reflow();
	}

	@Override
	public void minimizeAllSubEntries(TreeEntry treeEntry) {
		List<TreeEntry> subEntries = entryCollection.getAllSubEntries(treeEntry);
		for (TreeEntry entry : subEntries) {
			entry.minimize();
		}
	}

	@Override
	public void maximizeAllSubEntries(TreeEntry treeEntry) {
		List<TreeEntry> subEntries = entryCollection.getAllSubEntries(treeEntry);
		for (TreeEntry entry : subEntries) {
			entry.maximize();
		}
	}

	@Override
	public void scrollToEntry(TreeEntry treeEntry) {
		scrollAnimator.scrollTo(treeEntry);
	}
}
