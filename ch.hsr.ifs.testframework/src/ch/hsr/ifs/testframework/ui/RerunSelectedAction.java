package ch.hsr.ifs.testframework.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;

import ch.hsr.ifs.testframework.Messages;
import ch.hsr.ifs.testframework.TestFrameworkPlugin;
import ch.hsr.ifs.testframework.model.TestElement;

public class RerunSelectedAction extends Action {
	private final TreeViewer treeViewer;
	private final TestRunnerViewPart testRunnerViewPart;

	public RerunSelectedAction(TestRunnerViewPart testRunnerViewPart, TreeViewer treeViewer) {
		this.testRunnerViewPart = testRunnerViewPart;
		this.treeViewer = treeViewer;
		Messages msg = TestRunnerViewPart.msg;
		setText(msg.getString("TestRunnerViewPart.RerunSelectedTest")); //$NON-NLS-1$
		setToolTipText(msg.getString("TestRunnerViewPart.RerunSelectedTest")); //$NON-NLS-1$
		setDisabledImageDescriptor(TestFrameworkPlugin.getImageDescriptor("dlcl16/relaunch.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/relaunch.gif")); //$NON-NLS-1$
		setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/relaunch.gif")); //$NON-NLS-1$
		setEnabled(false);
	}

	@Override
	public void run() {
		ISelection selection = treeViewer.getSelection();
		String rerunname = "";
		if (selection != null && selection instanceof ITreeSelection) {
			TreePath[] paths = ((ITreeSelection) selection).getPaths();
			if (paths.length > 0) {
				Object leaf = paths[0].getLastSegment();
				if (leaf != null && leaf instanceof TestElement)
					rerunname = ((TestElement) leaf).getRerunName();
			}
		}
		System.err.println("RerunSelectedAction: " + rerunname);
		this.testRunnerViewPart.rerunSelectedTestRun(rerunname);

	}
}
