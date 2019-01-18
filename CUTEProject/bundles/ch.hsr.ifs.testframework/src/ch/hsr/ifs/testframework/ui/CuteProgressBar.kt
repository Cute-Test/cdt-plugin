/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 * Stephan Michels, stephan@apache.org - 104944 [JUnit] Unnecessary code in JUnitProgressBar
 * Institute for Software, Emanuel Graf - Adaption for CUTE
 *******************************************************************************/
package ch.hsr.ifs.testframework.ui

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.swt.SWT
import org.eclipse.swt.events.ControlAdapter
import org.eclipse.swt.events.ControlEvent
import org.eclipse.swt.events.PaintEvent
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.GC
import org.eclipse.swt.graphics.Point
import org.eclipse.swt.graphics.Rectangle
import org.eclipse.swt.widgets.Canvas
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
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


private const val DEFAULT_WIDTH  = 160
private const val DEFAULT_HEIGHT = 18

/**
 * A progress bar with a red/green indication for success or failure.
 */
class CuteProgressBar(parent: Composite) : Canvas(parent, SWT.NONE), ITestElementListener, ISessionListener, ITestCompositeListener {

   private var fCurrentTickCount = 0
   private var fMaxTickCount = 0
   private var fColorBarWidth = 0
   private val fOKColor: Color
   private val fFailureColor: Color
   private val fStoppedColor: Color
   private var fError = false
   private var fStopped = false
   private val msg = TestFrameworkPlugin.messages

   private lateinit var session: TestSession

   init {
      TestFrameworkPlugin.getModel().addListener(this)
      addControlListener(object : ControlAdapter() {
         override fun controlResized(e: ControlEvent) {
            fColorBarWidth = scale(fCurrentTickCount)
            redraw()
         }
      })
      addPaintListener{ paint(it) }
      val display = parent.getDisplay()
      fFailureColor = Color(display, 159, 63, 63)
      fOKColor = Color(display, 95, 191, 95)
      fStoppedColor = Color(display, 120, 120, 120)
      addDisposeListener{
         fFailureColor.dispose()
         fOKColor.dispose()
         fStoppedColor.dispose()
         TestFrameworkPlugin.getModel().removeListener(this)
      }
   }

   fun setMaximum(max: Int) {
      fMaxTickCount = max
   }

   private fun reset() {
      fError = false
      fStopped = false
      fCurrentTickCount = session.getRun()
      fMaxTickCount = session.getTotalTests()
      fColorBarWidth = 0
      redraw()
   }

   private fun paintStep(startX: Int, endX: Int) {
      val gc = GC(this)
      setStatusColor(gc)
      val rect = getClientArea()
      val start = Math.max(1, startX)
      gc.fillRectangle(start, 1, endX - start, rect.height - 2)
      gc.dispose()
   }

   private fun setStatusColor(gc: GC) =
      when {
         fStopped -> gc.background = fStoppedColor
         fError -> gc.background = fFailureColor
         else -> gc.background = fOKColor
      }

   fun stopped() {
      fStopped = true
      redraw()
   }

   private fun scale(value: Int): Int {
      if (fMaxTickCount > 0) {
         val r = getClientArea()
         if (r.width != 0){
            return Math.max(0, value * (r.width - 2) / fMaxTickCount)
         }
      }
      return value
   }

   private fun drawBevelRect(gc: GC, rect: Rectangle, topleft: Color, bottomright: Color) {
      val x = rect.x
      val y = rect.y
      val w = rect.width - 1
      val h = rect.height - 1

      gc.setForeground(topleft)
      gc.drawLine(x, y, x + w - 1, y)
      gc.drawLine(x, y, x, y + h - 1)

      gc.setForeground(bottomright)
      gc.drawLine(x + w, y, x + w, y + h)
      gc.drawLine(x, y + h, x + w, y + h)
   }

   private fun paint(event: PaintEvent) {
      val gc = event.gc
      val disp = getDisplay()

      val rect = getClientArea()
      gc.fillRectangle(rect)
      drawBevelRect(gc, rect, disp.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW), disp.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW))

      setStatusColor(gc)
      fColorBarWidth = Math.min(rect.width - 2, fColorBarWidth)
      gc.fillRectangle(1, 1, fColorBarWidth, rect.height - 2)
   }

   override fun computeSize(wHint: Int, hHint: Int, changed: Boolean): Point {
      checkWidget()
      val size = Point(DEFAULT_WIDTH, DEFAULT_HEIGHT)
      if (wHint != SWT.DEFAULT) size.x = wHint
      if (hHint != SWT.DEFAULT) size.y = hHint
      return size
   }

   private fun update(run: Int, failures: Int) {
      fCurrentTickCount = run
      var x = fColorBarWidth

      fColorBarWidth = scale(fCurrentTickCount)

      if (!fError && failures > 0) {
         fError = true
         x = 1
      }
      if (fCurrentTickCount == fMaxTickCount) fColorBarWidth = getClientArea().width - 1
      paintStep(x, fColorBarWidth)
   }

   private fun update(run: Int, failures: Int, total: Int) {
      fCurrentTickCount = run
      fMaxTickCount = total

      fColorBarWidth = scale(fCurrentTickCount)

      if (!fError && failures > 0) {
         fError = true
      }
      if (fCurrentTickCount == fMaxTickCount) fColorBarWidth = getClientArea().width - 1
      redraw()
   }

   public fun refresh(hasErrors: Boolean) {
      fError = hasErrors
      redraw()
   }

   override fun modelCanged(source: TestElement, event: NotifyEvent) {
      if (event.type == NotifyEvent.EventType.testFinished || event.type == NotifyEvent.EventType.suiteFinished) {
         object : UIJob(msg.getString("CuteProgressBar.UpdateProgressbar")) {

            override fun belongsTo(family: Any) = TestFrameworkPlugin.PLUGIN_ID.equals(family)

            override fun runInUIThread(monitor: IProgressMonitor): IStatus {
               update(session.getRun(), session.getError() + session.getFailure())
               return Status(IStatus.OK, TestFrameworkPlugin.PLUGIN_ID, IStatus.OK, "", null)
            }

         }.schedule()
      }
   }

   override fun sessionStarted(session: TestSession) {
      this.session = session
      session.addListener(this)
     object : UIJob(msg.getString("CuteProgressBar.ResetProgressbar")) {

         override fun belongsTo(family: Any) = TestFrameworkPlugin.PLUGIN_ID.equals(family)

         override fun runInUIThread(monitor: IProgressMonitor): IStatus {
            reset()
            return Status(IStatus.OK, TestFrameworkPlugin.PLUGIN_ID, IStatus.OK, "", null)
         }

      }.schedule()

   }

   override fun sessionFinished(session: TestSession) {}

   override fun newTestElement(source: ITestComposite, newElement: TestElement) {
      newElement.addTestElementListener(this)
      object : UIJob(msg.getString("CuteProgressBar.UpdateProgressbar")) {

         override fun belongsTo(family: Any) = TestFrameworkPlugin.PLUGIN_ID.equals(family)

         override fun runInUIThread(monitor: IProgressMonitor): IStatus {
            update(session.getRun(), session.getError() + session.getFailure(), session.getTotalTests())
            return Status(IStatus.OK, TestFrameworkPlugin.PLUGIN_ID, IStatus.OK, "", null)
         }

      }.schedule()
   }

}
