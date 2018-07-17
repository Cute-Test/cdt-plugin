/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.model;

import java.util.ArrayList;


abstract class TestElement {

   private val listeners = mutableListOf<ITestElementListener>()
   private var parent: ITestComposite? = null

   public abstract fun getName(): String;

   public abstract fun getStatus(): TestStatus;

   fun addTestElementListener(lis: ITestElementListener) {
      if (!listeners.contains(lis)) {
         listeners.add(lis);
      }
   }

   fun removeTestElementListener(lis: ITestElementListener) {
      listeners.remove(lis);
   }

   fun notifyListeners(event: NotifyEvent) {
	   listeners.forEach{ it.modelCanged(this, event) }
   }

   fun getParent() = parent

   fun setParent(parent: ITestComposite) {
      this.parent = parent;
   }

   fun getRerunName(): String {
      val result = StringBuilder();
      parent?.let{
         if(it.getRerunName().length > 0) {
            result.append(it.getRerunName()).append('#')
         }
      }
      result.append(getName());
      return result.toString();
   }

}
