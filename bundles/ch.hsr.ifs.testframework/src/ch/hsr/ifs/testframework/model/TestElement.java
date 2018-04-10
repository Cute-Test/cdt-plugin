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
import java.util.List;


public abstract class TestElement {

   protected List<ITestElementListener> listeners = new ArrayList<>();
   private ITestComposite               parent;

   public abstract String getName();

   public abstract TestStatus getStatus();

   public void addTestElementListener(ITestElementListener lis) {
      if (!listeners.contains(lis)) {
         listeners.add(lis);
      }
   }

   public void removeTestElementListener(ITestElementListener lis) {
      listeners.remove(lis);
   }

   protected void notifyListeners(NotifyEvent event) {
      for (ITestElementListener lis : listeners) {
         lis.modelCanged(this, event);
      }
   }

   public ITestComposite getParent() {
      return parent;
   }

   public void setParent(ITestComposite parent) {
      this.parent = parent;
   }

   public String getRerunName() {
      StringBuilder result = new StringBuilder();
      if (getParent() != null && getParent().getRerunName().length() > 0) {
         result.append(getParent().getRerunName());
         result.append('#');
      }
      result.append(getName());
      return result.toString();
   }

}
