/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil, Switzerland,
 * http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any purpose without fee is hereby
 * granted, provided that the above copyright notice and this permission notice appear in all
 * copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.mockator.mockobject.function.suite.wizard;

import java.util.Collection;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

import ch.hsr.ifs.iltis.core.core.exception.ILTISException;


// Copied and adapted from CUTE
class TypedElementSelectionValidator implements ISelectionStatusValidator {

   private final IStatus       fgErrorStatus = new StatusInfo(IStatus.ERROR, "");
   private final IStatus       fgOKStatus    = new StatusInfo();
   private final Class<?>[]    fAcceptedTypes;
   private final boolean       fAllowMultipleSelection;
   private final Collection<?> fRejectedElements;

   public TypedElementSelectionValidator(final Class<?>[] acceptedTypes, final boolean allowMultipleSelection) {
      this(acceptedTypes, allowMultipleSelection, null);
   }

   public TypedElementSelectionValidator(final Class<?>[] acceptedTypes, final boolean allowMultipleSelection,
                                         final Collection<Object> rejectedElements) {
      ILTISException.Unless.notNull("accepted types must not be null", acceptedTypes);
      fAcceptedTypes = acceptedTypes;
      fAllowMultipleSelection = allowMultipleSelection;
      fRejectedElements = rejectedElements;
   }

   @Override
   public IStatus validate(final Object[] elements) {
      if (isValid(elements)) return fgOKStatus;
      return fgErrorStatus;
   }

   private boolean isOfAcceptedType(final Object o) {
      for (final Class<?> fAcceptedType : fAcceptedTypes) {
         if (fAcceptedType.isInstance(o)) return true;
      }
      return false;
   }

   private boolean isRejectedElement(final Object elem) {
      return fRejectedElements != null && fRejectedElements.contains(elem);
   }

   private boolean isValid(final Object[] selection) {
      if (selection.length == 0) return false;

      if (!fAllowMultipleSelection && selection.length != 1) return false;

      for (final Object element : selection) {
         final Object o = element;
         if (!isOfAcceptedType(o) || isRejectedElement(o)) return false;
      }
      return true;
   }
}
