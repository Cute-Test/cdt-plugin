package ch.hsr.ifs.testframework.ui;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;

import ch.hsr.ifs.testframework.Messages;
import ch.hsr.ifs.testframework.TestFrameworkPlugin;
import ch.hsr.ifs.testframework.model.TestElement;


public class RerunSelectedAction extends Action {

   private final TreeViewer         treeViewer;
   private final TestRunnerViewPart testRunnerViewPart;

   public RerunSelectedAction(TestRunnerViewPart testRunnerViewPart, TreeViewer treeViewer) {
      this.testRunnerViewPart = testRunnerViewPart;
      this.treeViewer = treeViewer;
      Messages msg = TestRunnerViewPart.msg;
      setText(msg.getString("TestRunnerViewPart.RerunSelectedTest"));
      setToolTipText(msg.getString("TestRunnerViewPart.RerunSelectedTest"));
      setDisabledImageDescriptor(TestFrameworkPlugin.getImageDescriptor("dlcl16/relaunch.gif"));
      setHoverImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/relaunch.gif"));
      setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/relaunch.gif"));
      setEnabled(false);
   }

   @Override
   public void run() {
      ISelection selection = treeViewer.getSelection();
      ArrayList<String> rerunnames = new ArrayList<>();
      if (selection != null && selection instanceof ITreeSelection) {
         TreePath[] paths = ((ITreeSelection) selection).getPaths();
         for (TreePath path : paths) {
            Object leaf = path.getLastSegment();
            if (leaf != null && leaf instanceof TestElement) rerunnames.add(((TestElement) leaf).getRerunName());
         }
      }
      this.testRunnerViewPart.rerunSelectedTestRun(rerunnames);
   }
}
