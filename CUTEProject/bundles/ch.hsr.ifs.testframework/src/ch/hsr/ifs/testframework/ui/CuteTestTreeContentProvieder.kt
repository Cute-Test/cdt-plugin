/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.ui

import org.eclipse.jface.viewers.ITreeContentProvider
import org.eclipse.jface.viewers.Viewer
import ch.hsr.ifs.testframework.model.ITestComposite
import ch.hsr.ifs.testframework.model.TestElement

/**
 * @author egraf
 *
 */
class CuteTestTreeContentProvieder : ITreeContentProvider {

   override fun getChildren(parentElement: Any?): Array<Any>? = if (parentElement is ITestComposite) {
      parentElement.getElements().toTypedArray()
   } else {
      null
   }

   override fun getParent(element: Any?): Any? = if (element is TestElement) {
      element.getParent()
   } else {
      null
   }

   override fun hasChildren(element: Any?): Boolean = if (element is ITestComposite) {
      element.getElements().size > 0
   } else {
      false
   }

   override fun getElements(inputElement: Any?): Array<Any>? = if (inputElement is ITestComposite) {
      inputElement.getElements().toTypedArray()
   } else {
      null
   }

   override fun dispose() = Unit

   override fun inputChanged(viewer: Viewer?, oldInput: Any?, newInput: Any?) = Unit
}