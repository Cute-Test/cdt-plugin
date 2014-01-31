package ch.hsr.ifs.testframework.ui;

import org.eclipse.jface.action.Action;

import ch.hsr.ifs.testframework.TestFrameworkPlugin;

class RerunLastTestAction extends Action {

	private final TestRunnerViewPart testRunnerViewPart;

	public RerunLastTestAction(TestRunnerViewPart testRunnerViewPart) {
		this.testRunnerViewPart = testRunnerViewPart;
		setText(TestRunnerViewPart.msg.getString("TestRunnerViewPart.RerunTest"));
		setToolTipText(TestRunnerViewPart.msg.getString("TestRunnerViewPart.RerunTest"));
		setDisabledImageDescriptor(TestFrameworkPlugin.getImageDescriptor("dlcl16/relaunch.gif"));
		setHoverImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/relaunch.gif"));
		setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/relaunch.gif"));
		setEnabled(false);
	}

	@Override
	public void run() {
		this.testRunnerViewPart.rerunTestRun();
	}

}