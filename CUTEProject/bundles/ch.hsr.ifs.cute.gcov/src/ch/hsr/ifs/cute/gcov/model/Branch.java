/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.model;

/**
 * @author Emanuel Graf IFS
 *
 */
public class Branch {

    private CoverageStatus status = CoverageStatus.Uncovered;
    private final int      taken;

    public Branch(int taken) {
        super();
        if (taken < 0 || taken > 100) {
            throw new IllegalArgumentException("Coverage must be between 0 and 100%");
        }
        this.taken = taken;
        switch (taken) {
        case 0:
            status = CoverageStatus.Uncovered;
            break;
        case 100:
            status = CoverageStatus.Covered;
            break;
        default:
            status = CoverageStatus.PartiallyCovered;
        }
    }

    public CoverageStatus getStatus() {
        return status;
    }

    public int getCovered() {
        return taken;
    }

    @Override
    public String toString() {
        return "Branch taken: " + taken + " " + status;
    }
}
