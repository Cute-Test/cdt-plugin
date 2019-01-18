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
import ch.hsr.ifs.testframework.preference.SHOW_WHITESPACES

/**
 * @author Emanuel Graf
 *
 */
class ShowWhiteSpaceAction(private val viewer: CuteTextMergeViewer) : Action(msg.getString("ShowWhiteSpaceAction.ShowWhitespaceChar"), AS_CHECK_BOX) {

   companion object {
      private val msg = TestFrameworkPlugin.messages
   }

   init {
      setImageDescriptor(TestFrameworkPlugin.getImageDescriptor("dlcl16/show_whitespace_chars.gif"))
      setToolTipText(msg.getString("ShowWhiteSpaceAction.ShowWhitespaceChar"))
      setChecked(TestFrameworkPlugin.default.getPreferenceStore().getBoolean(SHOW_WHITESPACES))
   }

   override fun run() {
      val show = !TestFrameworkPlugin.default.getPreferenceStore().getBoolean(SHOW_WHITESPACES)
      TestFrameworkPlugin.default.getPreferenceStore().setValue(SHOW_WHITESPACES, show)
      viewer.showWhitespaces(show)
   }

}