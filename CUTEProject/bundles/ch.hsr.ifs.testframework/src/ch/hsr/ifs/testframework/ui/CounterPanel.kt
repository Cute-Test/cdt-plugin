/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Label
import org.eclipse.ui.progress.UIJob

import ch.hsr.ifs.testframework.Messages
import ch.hsr.ifs.testframework.TestFrameworkPlugin
import ch.hsr.ifs.testframework.model.ISessionListener
import ch.hsr.ifs.testframework.model.ITestComposite
import ch.hsr.ifs.testframework.model.ITestCompositeListener
import ch.hsr.ifs.testframework.model.ITestElementListener
import ch.hsr.ifs.testframework.model.NotifyEvent
import ch.hsr.ifs.testframework.model.TestElement
import ch.hsr.ifs.testframework.model.TestSession


/**
 * @author egraf
 *
 */
class CounterPanel(parent: Composite, style: Int) : Composite(parent, style), ITestElementListener, ISessionListener, ITestCompositeListener {

   val msg = TestFrameworkPlugin.messages!!

   inner class UpdateCounterPanelJob(name: String?): UIJob(name) {

      override fun runInUIThread(monitor: IProgressMonitor): IStatus {
         updateNumbers()
         return Status(IStatus.OK, TestFrameworkPlugin.PLUGIN_ID, IStatus.OK, msg.getString("CounterPanel.Ok"), null)
      }
   }

   private lateinit var runLabel: Label
   private lateinit var runText: Label
   private val errorImage = TestFrameworkPlugin.getImageDescriptor("tcr/error.gif").createImage()
   private val failedImage = TestFrameworkPlugin.getImageDescriptor("tcr/failed.gif").createImage()
   private lateinit var errorImageLabel: Label
   private lateinit var errorLabel: Label
   private lateinit var errorText: Label
   private lateinit var failedImageLabel: Label
   private lateinit var failedLabel: Label
   private lateinit var failedText: Label

	private lateinit var session: TestSession

   private var total = 0

   init {
      TestFrameworkPlugin.getModel()?.addListener(this)
      initialize()
   }

   private fun initialize() {
      val gridData7 = GridData()
      gridData7.grabExcessHorizontalSpace = false
      gridData7.horizontalIndent = 7
      val gridData6 = GridData()
      gridData6.grabExcessHorizontalSpace = false
      val gridData5 = GridData()
      gridData5.grabExcessHorizontalSpace = true
      gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.END
      val gridData4 = GridData()
      gridData4.grabExcessHorizontalSpace = false
      gridData4.horizontalIndent = 7
      val gridData3 = GridData()
      gridData3.grabExcessHorizontalSpace = false
      val gridData2 = GridData()
      gridData2.grabExcessHorizontalSpace = true
      gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.END
      val gridData1 = GridData()
      gridData1.grabExcessHorizontalSpace = false
      gridData1.horizontalIndent = 7
      val gridData = GridData()
      gridData.grabExcessHorizontalSpace = false
      val gridLayout = GridLayout()
      gridLayout.numColumns = 8
      runLabel = Label(this, SWT.NONE)
      runLabel.setText(msg.getString("CounterPanel.Runs"))
      runLabel.setLayoutData(gridData)
      runText = Label(this, SWT.READ_ONLY)
      runText.setText(msg.getString("CounterPanel.ZeroSlashZero"))
      runText.setLayoutData(gridData1)
      errorImageLabel = Label(this, SWT.NONE)
      errorImage.setBackground(errorImageLabel.getBackground())
      errorImageLabel.setImage(errorImage)
      errorImageLabel.setLayoutData(gridData2)
      errorLabel = Label(this, SWT.NONE)
      errorLabel.setText(msg.getString("CounterPanel.Errors"))
      errorLabel.setLayoutData(gridData3)
      errorText = Label(this, SWT.READ_ONLY)
      errorText.setText(msg.getString("CounterPanel.Zero"))
      errorText.setLayoutData(gridData4)
      failedImageLabel = Label(this, SWT.NONE)
      failedImage.setBackground(failedImageLabel.getBackground())
      failedImageLabel.setImage(failedImage)
      failedImageLabel.setLayoutData(gridData5)
      failedLabel = Label(this, SWT.NONE)
      failedLabel.setText(msg.getString("CounterPanel.Failures"))
      failedLabel.setLayoutData(gridData6)
      failedText = Label(this, SWT.READ_ONLY)
      failedText.setText(msg.getString("CounterPanel.Zero"))
      failedText.setLayoutData(gridData7)

      addDisposeListener({
         disposeIcons()
         TestFrameworkPlugin.getModel()?.removeListener(this)
      })

      this.setLayout(gridLayout)
      this.setSize(Point(342, 30))
   }

   fun disposeIcons() {
      errorImage.dispose()
      failedImage.dispose()
   }

   fun setTotal(total: Int) {
      this.total = total
      layout()
   }

   fun setRun(run: Int) {
      runText.setText(Integer.toString(run) + msg.getString("CounterPanel.Slash") + Integer.toString(total))
      runText.pack(true)
      layout()
      redraw()
   }

   fun setErrors(errors: Int) {
      errorText.setText(Integer.toString(errors))
      errorText.pack(true)
      layout()
      redraw()
   }

   fun setFailures(failures: Int) {
      failedText.setText(Integer.toString(failures))
      failedText.pack(true)
      redraw()
   }

   @Suppress("UNUSED_PARAMETER")
   fun reset(session: TestSession) {
      updateNumbers()
   }

   private fun updateNumbers() {
      setTotal(session.getTotalTests())
      setRun(session.getRun())
      setFailures(session.getFailure())
      setErrors(session.getError())
   }

   override fun modelCanged(source: TestElement, event: NotifyEvent) {
      val job = UpdateCounterPanelJob(msg.getString("CounterPanel.UpdateCounterPanelJob"))
      job.schedule()
   }

   override fun sessionStarted(session: TestSession) {
      this.session = session
      session.addListener(this)
      val job = UpdateCounterPanelJob(msg.getString("CounterPanel.UpdateCounterPanelJob"))
      job.schedule()
   }

   override fun sessionFinished(session: TestSession) {}

   override fun newTestElement(source: ITestComposite, newElement: TestElement) {
      val job = UpdateCounterPanelJob(msg.getString("CounterPanel.UpdateCounterPanelJob"))
      newElement.addTestElementListener(this)
      job.schedule()
   }

}
