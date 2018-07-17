/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui

import org.eclipse.jface.viewers.ISelectionChangedListener
import org.eclipse.jface.viewers.SelectionChangedEvent
import org.eclipse.jface.viewers.TreeSelection
import ch.hsr.ifs.testframework.model.TestElement

/**
 * @author egraf
 *
 */
class CuteTestSelecetionListener(private val viewer: TestViewer?) : ISelectionChangedListener {

   override fun selectionChanged(event: SelectionChangedEvent?) {
      if (event?.getSelection() is TreeSelection) {
         val treeSel = event.getSelection() as TreeSelection
         if (treeSel.getFirstElement() is TestElement) {
            val testElement = treeSel.getFirstElement() as TestElement
            viewer?.showTestDetails(testElement)
         }
      }
   }

}