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
import ch.hsr.ifs.testframework.Messages
import ch.hsr.ifs.testframework.TestFrameworkPlugin

/**
 * @author Emanuel Graf
 *
 */
class ScrollLockAction(private val view: TestRunnerViewPart) : Action(msg!!.getString("ScrollLockAction.ScrollLock")) {

   companion object {
      private val msg = TestFrameworkPlugin.getMessages()
   }


   init {
      setToolTipText(msg!!.getString("ScrollLockAction.ScrollLock"))
      setDisabledImageDescriptor(TestFrameworkPlugin.getImageDescriptor("dlcl16/lock.gif"))
      setHoverImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/lock.gif"))
      setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("obj16/lock.gif"))
      setChecked(false)
   }

   override fun run(){
      view.isAutoScroll = !isChecked()
   }

}