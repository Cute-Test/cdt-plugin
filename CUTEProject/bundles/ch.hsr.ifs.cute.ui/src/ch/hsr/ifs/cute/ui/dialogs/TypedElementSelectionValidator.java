/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.dialogs;

import java.util.Collection;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

import ch.hsr.ifs.cute.ui.utilities.StatusInfo;


/**
 * Empty selections are not accepted.
 *
 * @since 4.0
 */
public class TypedElementSelectionValidator implements ISelectionStatusValidator {

    private final IStatus fgErrorStatus = new StatusInfo(IStatus.ERROR, "");
    private final IStatus fgOKStatus    = new StatusInfo();

    private final Class<?>[]    fAcceptedTypes;
    private final boolean       fAllowMultipleSelection;
    private final Collection<?> fRejectedElements;

    public TypedElementSelectionValidator(Class<?>[] acceptedTypes, boolean allowMultipleSelection) {
        this(acceptedTypes, allowMultipleSelection, null);
    }

    public TypedElementSelectionValidator(Class<?>[] acceptedTypes, boolean allowMultipleSelection, Collection<Object> rejectedElements) {
        Assert.isNotNull(acceptedTypes);
        fAcceptedTypes = acceptedTypes;
        fAllowMultipleSelection = allowMultipleSelection;
        fRejectedElements = rejectedElements;
    }

    @Override
    public IStatus validate(Object[] elements) {
        if (isValid(elements)) {
            return fgOKStatus;
        }
        return fgErrorStatus;
    }

    private boolean isOfAcceptedType(Object o) {
        for (Class<?> fAcceptedType : fAcceptedTypes) {
            if (fAcceptedType.isInstance(o)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRejectedElement(Object elem) {
        return (fRejectedElements != null) && fRejectedElements.contains(elem);
    }

    private boolean isValid(Object[] selection) {
        if (selection.length == 0) {
            return false;
        }

        if (!fAllowMultipleSelection && selection.length != 1) {
            return false;
        }

        for (Object element : selection) {
            Object o = element;
            if (!isOfAcceptedType(o) || isRejectedElement(o)) {
                return false;
            }
        }
        return true;
    }
}
