/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil, Switzerland,
 * http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any purpose without fee is hereby
 * granted, provided that the above copyright notice and this permission notice appear in all
 * copies.
 ******************************************************************************/
package ch.hsr.ifs.mockator.plugin.mockobject.function.suite.wizard;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;


// Copied and adapted from CUTE
class StatusUtil {

   private StatusUtil() {
      throw new AssertionError();
   }

   public static IStatus getMostSevere(IStatus[] status) {
      IStatus max = null;

      for (IStatus curr : status) {
         if (curr.matches(IStatus.ERROR)) return curr;
         if (max == null || curr.getSeverity() > max.getSeverity()) {
            max = curr;
         }
      }

      return max;
   }

   public static void applyToStatusLine(DialogPage page, IStatus status) {
      String message = status.getMessage();
      switch (status.getSeverity()) {
      case IStatus.OK:
         page.setMessage(message, IMessageProvider.NONE);
         page.setErrorMessage(null);
         break;
      case IStatus.WARNING:
         page.setMessage(message, IMessageProvider.WARNING);
         page.setErrorMessage(null);
         break;
      case IStatus.INFO:
         page.setMessage(message, IMessageProvider.INFORMATION);
         page.setErrorMessage(null);
         break;
      default:
         if (message.length() == 0) {
            message = null;
         }
         page.setMessage(null);
         page.setErrorMessage(message);
         break;
      }
   }
}
