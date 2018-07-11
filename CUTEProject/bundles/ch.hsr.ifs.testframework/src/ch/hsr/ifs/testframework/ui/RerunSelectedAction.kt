package ch.hsr.ifs.testframework.ui

import java.util.ArrayList
import org.eclipse.jface.action.Action
import org.eclipse.jface.viewers.ISelection
import org.eclipse.jface.viewers.ITreeSelection
import org.eclipse.jface.viewers.TreePath
import org.eclipse.jface.viewers.TreeViewer
import ch.hsr.ifs.testframework.Messages
import ch.hsr.ifs.testframework.TestFrameworkPlugin
import ch.hsr.ifs.testframework.model.TestElement

class RerunSelectedAction(private val testRunnerViewPart: TestRunnerViewPart, private val treeViewer: TreeViewer) : Action() {

	init {
		val msg = TestRunnerViewPart.msg
		setText(msg.getString("TestRunnerViewPart.RerunSelectedTest"))
		setToolTipText(msg.getString("TestRunnerViewPart.RerunSelectedTest"))
		setDisabledImageDescriptor(TestFrameworkPlugin.getImageDescriptor("dlcl16/relaunch.gif"))
		setHoverImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/relaunch.gif"))
		setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/relaunch.gif"))
		setEnabled(false)
	}

	override fun run() = with(treeViewer.selection) {
		testRunnerViewPart.rerunSelectedTestRun(when (this) {
			is ITreeSelection -> paths.mapNotNull { it.lastSegment as? TestElement }.map(TestElement::getRerunName)
			else -> emptyList()
		})
	}

}