/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.UnsupportedEncodingException

import org.eclipse.compare.CompareConfiguration
import org.eclipse.compare.IEncodedStreamContentAccessor
import org.eclipse.compare.ITypedElement
import org.eclipse.compare.structuremergeviewer.DiffNode
import org.eclipse.core.runtime.CoreException
import org.eclipse.jface.action.ToolBarManager
import org.eclipse.jface.dialogs.IDialogConstants
import org.eclipse.jface.dialogs.TrayDialog
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.ViewForm
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Control
import org.eclipse.swt.widgets.Shell
import org.eclipse.swt.widgets.ToolBar

import ch.hsr.ifs.testframework.Messages
import ch.hsr.ifs.testframework.TestFrameworkPlugin
import ch.hsr.ifs.testframework.model.TestCase
import ch.hsr.ifs.testframework.model.TestFailure
import ch.hsr.ifs.testframework.model.TestResult


/**
 * @author Emanuel Graf
 *
 */
class CuteCompareResultDialog(shell: Shell, private var test: TestCase) : TrayDialog(shell) {

   private val msg = TestFrameworkPlugin.messages

   private inner class CompareElement(private val fContent: String) : ITypedElement, IEncodedStreamContentAccessor {

      override fun getName() = "<no name>"

      override fun getImage() = null

      override fun getType() = "txt"

      override fun getContents() =
         try {
            ByteArrayInputStream(fContent.toByteArray(Charsets.UTF_8))
         } catch (e: UnsupportedEncodingException) {
            ByteArrayInputStream(fContent.toByteArray())
         }

	   @Throws(CoreException::class)
      override fun getCharset() = Charsets.UTF_8.toString()

   }

   private lateinit var compareViewer: CuteTextMergeViewer

   init {
      setHelpAvailable(false)
      setShellStyle(SWT.DIALOG_TRIM or SWT.RESIZE or SWT.MAX)
   }

   @Override
   protected override fun createDialogArea(parent: Composite): Control {
      val composite = super.createDialogArea(parent) as Composite
      val pane = ViewForm(composite, SWT.BORDER or SWT.FLAT)
      val control = createCompareViewer(pane)
      with(GridData(GridData.FILL_HORIZONTAL or GridData.FILL_VERTICAL), {
         widthHint = convertWidthInCharsToPixels(120)
         heightHint = convertHeightInCharsToPixels(13)
         pane.setLayoutData(this)
      })

      val tb = ToolBar(pane, SWT.BORDER or SWT.FLAT)
      val tbm = ToolBarManager(tb)
      val action = ShowWhiteSpaceAction(compareViewer)
      tbm.add(action)
      tbm.update(true)
      pane.setTopRight(tb)

      pane.setContent(control)
      control.setLayoutData(GridData(GridData.FILL_BOTH))
      return composite
   }

   private fun createCompareViewer(pane: ViewForm) =
      with(CompareConfiguration(), {
         setLeftLabel(msg.getString("CuteCompareResultDialog.Expected"))
         setLeftEditable(false)
         setRightLabel(msg.getString("CuteCompareResultDialog.Actual"))
         setRightEditable(false)
         setProperty(CompareConfiguration.IGNORE_WHITESPACE, false)
         compareViewer = CuteTextMergeViewer(pane, SWT.NONE, this)
         this@CuteCompareResultDialog.setCompareViewerInput(test)
         val control = compareViewer.getControl()
         control.addDisposeListener{ this.dispose() }
         control
      })

   protected override fun createButtonsForButtonBar(parent: Composite) {
      createButton(parent, IDialogConstants.OK_ID, msg.getString("CuteCompareResultDialog.Ok"), true)
   }

   fun setCompareViewerInput(test: TestCase) {
      this.test = test
      if (!compareViewer.getControl().isDisposed()) {
         val result = test.getResult()
         if (result is TestFailure) {
            val expected = CompareElement(result.expected?:"no data")
            val was = CompareElement(result.was?:"no data")
            compareViewer.setInput(DiffNode(expected, was))
         }
      }
   }

   protected override fun configureShell(newShell: Shell) {
      super.configureShell(newShell)
      newShell.setText(msg.getString("CuteCompareResultDialog.ResultComparison"))
   }

   fun refresh() = compareViewer.refresh()

}
