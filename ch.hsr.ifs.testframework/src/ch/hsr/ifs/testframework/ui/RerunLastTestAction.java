package ch.hsr.ifs.testframework.ui;

import org.eclipse.jface.action.Action;

import ch.hsr.ifs.testframework.TestFrameworkPlugin;

class RerunLastTestAction extends Action {
	/**
	 * 
	 */
	private final TestRunnerViewPart testRunnerViewPart;

	public RerunLastTestAction(TestRunnerViewPart testRunnerViewPart) {
		this.testRunnerViewPart = testRunnerViewPart;
		setText(TestRunnerViewPart.msg.getString("TestRunnerViewPart.RerunTest")); //$NON-NLS-1$
		setToolTipText(TestRunnerViewPart.msg.getString("TestRunnerViewPart.RerunTest")); //$NON-NLS-1$
		setDisabledImageDescriptor(TestFrameworkPlugin.getImageDescriptor("dlcl16/relaunch.gif")); //$NON-NLS-1$
		setHoverImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/relaunch.gif")); //$NON-NLS-1$
		setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/relaunch.gif")); //$NON-NLS-1$
		setEnabled(false);
	}

	@Override
	public void run() {
		this.testRunnerViewPart.rerunTestRun();
	}

}