/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui

import org.eclipse.jface.action.Action
import org.eclipse.swt.widgets.Shell

import ch.hsr.ifs.testframework.model.TestCase


/**
 * @author Emanuel Graf
 *
 */
class CuteCompareResultAction(private val test: TestCase, private val shell: Shell) : Action() {

   private var dialog: CuteCompareResultDialog? = null

   override fun run() {
      dialog?.setCompareViewerInput(test) ?: with(CuteCompareResultDialog(shell, test), {
         dialog = this
         create()
         getShell().addDisposeListener{ dialog = null }
         setBlockOnOpen(false)
         open()
      })
   }

}
