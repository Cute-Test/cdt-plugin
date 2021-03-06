/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.dialogs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;


/**
 * Viewer filter used in selection dialogs.
 *
 * @since 4.0
 */
public class TypedViewerFilter extends ViewerFilter {

    private final Class<?>[] fAcceptedTypes;
    private final Object[]   fRejectedElements;

    public TypedViewerFilter(Class<?>[] acceptedTypes) {
        this(acceptedTypes, null);
    }

    public TypedViewerFilter(Class<?>[] acceptedTypes, Object[] rejectedElements) {
        Assert.isNotNull(acceptedTypes);
        fAcceptedTypes = acceptedTypes;
        fRejectedElements = rejectedElements;
    }

    /**
     * @see ViewerFilter#select
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (fRejectedElements != null) {
            for (Object fRejectedElement : fRejectedElements) {
                if (element.equals(fRejectedElement)) {
                    return false;
                }
            }
        }
        for (Class<?> fAcceptedType : fAcceptedTypes) {
            if (fAcceptedType.isInstance(element)) {
                return true;
            }
        }
        return false;
    }

}
