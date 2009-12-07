package org.ginkgo.gcov.navigator;

import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

public class Navigator extends CommonNavigator {
	@Override
	protected CommonViewer createCommonViewer(Composite parent) {
		CommonViewer aViewer = super.createCommonViewer(parent);
		return aViewer;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		CommonViewer aViewer = getCommonViewer();
		TreeColumnLayout ad = new TreeColumnLayout();
		parent.setLayout(ad);
		createColumn(aViewer, ad);
	}

	private TreeViewer createColumn(TreeViewer aViewer, TreeColumnLayout ad) {

		TreeColumn column = new TreeColumn(aViewer.getTree(), SWT.NONE);
		column.setWidth(200);
		column.setText("Element");
		ad.setColumnData(column, new ColumnWeightData(50, 100));

		TreeViewerColumn viewerColumn2 = new TreeViewerColumn(aViewer, SWT.NONE);
		viewerColumn2.setLabelProvider(new CoverageLabelProvider(viewerColumn2,
				aViewer));

		TreeColumn column2 = viewerColumn2.getColumn();
		column2.setWidth(200);
		column2.setText("Coverage");
		ad.setColumnData(column2, new ColumnWeightData(50, 100));

		aViewer.getTree().setHeaderVisible(true);
		return aViewer;
	}

	public Navigator() {
	}

}
