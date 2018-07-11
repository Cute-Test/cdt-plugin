package ch.hsr.ifs.testframework.ui

import org.eclipse.jface.action.Action
import ch.hsr.ifs.testframework.TestFrameworkPlugin

internal class RerunLastTestAction(private val testRunnerViewPart: TestRunnerViewPart) : Action() {

   init {
      setText(TestRunnerViewPart.msg.getString("TestRunnerViewPart.RerunTest"))
      setToolTipText(TestRunnerViewPart.msg.getString("TestRunnerViewPart.RerunTest"))
      setDisabledImageDescriptor(TestFrameworkPlugin.getImageDescriptor("dlcl16/relaunch.gif"))
      setHoverImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/relaunch.gif"))
      setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/relaunch.gif"))
      setEnabled(false)
   }

   override fun run() = testRunnerViewPart.rerunTestRun()

}