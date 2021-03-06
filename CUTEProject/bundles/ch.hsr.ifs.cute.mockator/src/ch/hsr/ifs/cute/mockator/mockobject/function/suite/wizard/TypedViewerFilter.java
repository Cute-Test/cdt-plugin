/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil, Switzerland,
 * http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any purpose without fee is hereby
 * granted, provided that the above copyright notice and this permission notice appear in all
 * copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.mockator.mockobject.function.suite.wizard;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


// /Copied and adapted from CUTE
class TypedViewerFilter extends ViewerFilter {

    private final Class<?>[] fAcceptedTypes;
    private final Object[]   fRejectedElements;

    public TypedViewerFilter(final Class<?>[] acceptedTypes) {
        this(acceptedTypes, null);
    }

    public TypedViewerFilter(final Class<?>[] acceptedTypes, final Object[] rejectedElements) {
        Assert.isNotNull(acceptedTypes);
        fAcceptedTypes = acceptedTypes;
        fRejectedElements = rejectedElements;
    }

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        if (fRejectedElements != null) {
            for (final Object fRejectedElement : fRejectedElements) {
                if (element.equals(fRejectedElement)) return false;
            }
        }

        for (final Class<?> fAcceptedType : fAcceptedTypes) {
            if (fAcceptedType.isInstance(element)) return true;
        }
        return false;
    }
}
