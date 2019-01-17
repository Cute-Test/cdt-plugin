/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui

import java.lang.reflect.Field
import org.eclipse.compare.CompareConfiguration
import org.eclipse.compare.contentmergeviewer.TextMergeViewer
import org.eclipse.compare.internal.MergeSourceViewer
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.jface.text.PaintManager
import org.eclipse.jface.text.TextViewer
import org.eclipse.jface.text.WhitespaceCharacterPainter
import org.eclipse.swt.widgets.Composite
import ch.hsr.ifs.testframework.TestFrameworkPlugin
import ch.hsr.ifs.testframework.preference.SHOW_WHITESPACES

/**
 * @author Emanuel Graf
 */
@SuppressWarnings("restriction")
class CuteTextMergeViewer(parent: Composite?, style: Int, configuration: CompareConfiguration?) : TextMergeViewer(parent, style, configuration) {
   private var leftWhitespaceCharacterPainter: WhitespaceCharacterPainter? = null
   private var rightWhitespaceCharacterPainter: WhitespaceCharacterPainter? = null

   override protected fun createControls(composite: Composite?) = super.createControls(composite)

   override protected fun configureTextViewer(textViewer: TextViewer?) {
      super.configureTextViewer(textViewer)
      if (TestFrameworkPlugin.default.getPreferenceStore().getBoolean(SHOW_WHITESPACES)) {
         textViewer?.addPainter(WhitespaceCharacterPainter(textViewer))
      }
   }

   private enum class ViewerLocation {
      LEFT, CENTER, RIGHT
   }

   private fun getSourceViewer(loc: ViewerLocation): TextViewer? {
      val fieldName = when (loc) {
         ViewerLocation.LEFT -> "fLeft"
         ViewerLocation.RIGHT -> "fRight"
         ViewerLocation.CENTER -> "fAncestor"
      }

      try {
         javaClass.getSuperclass().getDeclaredField(fieldName)?.let { field ->
            field.setAccessible(true)
            val instanceField = field.get(this)
            if (instanceField is MergeSourceViewer) {
               val viewer = instanceField as MergeSourceViewer?
               return viewer!!.getSourceViewer()
            }
         }
      } catch (e: NoSuchFieldException) {
         logException(e)
      } catch (e: SecurityException) {
         logException(e)
      } catch (e: IllegalAccessException) {
         logException(e)
      }
      return null
   }

   /**
    * Installs the painter on the editor.
    */
   private fun installPainter() {
      leftWhitespaceCharacterPainter = installPainter(ViewerLocation.LEFT)
      rightWhitespaceCharacterPainter = installPainter(ViewerLocation.RIGHT)
   }

   private fun installPainter(location: ViewerLocation): WhitespaceCharacterPainter? = getSourceViewer(location)?.let {
      WhitespaceCharacterPainter(it).apply {
         it.addPainter(this)
      }
   }

   private fun uninstallPainter() {
      uninstallPainter(ViewerLocation.LEFT, leftWhitespaceCharacterPainter)
      uninstallPainter(ViewerLocation.RIGHT, rightWhitespaceCharacterPainter)
   }

   private fun uninstallPainter(location: ViewerLocation, painter: WhitespaceCharacterPainter?) = getSourceViewer(location)?.let{ viewer ->
      viewer.removePainter(painter ?: getWhitespaceCharacterPainter(viewer))
   }

   private fun getWhitespaceCharacterPainter(viewer: Any?): WhitespaceCharacterPainter? {
      try {
         val viewerClass = Class.forName("org.eclipse.jface.text.TextViewer")
         val painterMgField = viewerClass!!.getDeclaredField("fPaintManager")
         painterMgField!!.setAccessible(true)
         val pm = painterMgField.get(viewer) as PaintManager
         val classPm = pm.javaClass
         val painterListField = classPm.getDeclaredField("fPainters")
         painterListField!!.setAccessible(true)
         val painters = painterListField.get(pm) as List<*>
         for (`object` in painters) {
            if (`object` is WhitespaceCharacterPainter) {
               val whitePainter = `object` as WhitespaceCharacterPainter?
               return whitePainter
            }
         }
      } catch (e: Exception) {
         logException(e)
      }
      return null
   }

   private fun logException(e: Exception) {
      TestFrameworkPlugin.log(Status(IStatus.ERROR, TestFrameworkPlugin.PLUGIN_ID, e.message, e))
   }

   fun showWhitespaces(show: Boolean) {
      if (show) {
         installPainter()
      } else {
         uninstallPainter()
      }
      invalidateTextPresentation()
      refresh()
   }
}