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
		// aViewer = createColumn(aViewer);
		return aViewer;
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		CommonViewer aViewer = getCommonViewer();
		TreeColumnLayout ad = new TreeColumnLayout();
		parent.setLayout(ad);
		createColumn(aViewer, ad);
		// createColumn(aViewer);
	}

	// private CommonViewer createColumn(CommonViewer aViewer) {
	// TreeColumn column =new TreeColumn(aViewer.getTree(),SWT.NONE);
	// TreeColumn b = column;
	// b.setWidth(200);
	// b.setText("Column 1");
	//		
	// final TreeViewerColumn column2 = new TreeViewerColumn(aViewer,SWT.NONE);
	// column2.getColumn().setWidth(200);
	// column2.getColumn().setText("Column 2");
	//		
	// column2.setLabelProvider(new CoverageLabelProvider(column2,aViewer));
	//		
	// aViewer.getTree().setHeaderVisible(true);
	// OwnerDrawLabelProvider.setUpOwnerDraw(getCommonViewer());
	// return aViewer;
	// }
	/* copy from coverage history SampleView */
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
		// OwnerDrawLabelProvider.setUpOwnerDraw(aViewer);
		return aViewer;
	}

	public Navigator() {
		// TODO �����������ꂽ�R���X�g���N�^�[�E�X�^�u
	}

}
