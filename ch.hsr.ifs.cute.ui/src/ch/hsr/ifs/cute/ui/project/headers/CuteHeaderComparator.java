/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.headers;

import java.util.Comparator;

/**
 * @author egraf
 * @since 4.0
 * 
 */
public class CuteHeaderComparator implements Comparator<ICuteHeaders> {

	public int compare(ICuteHeaders first, ICuteHeaders second) {
		int[] firstVersion = getVersionInts(first);
		int[] secondVersion = getVersionInts(second);
		if (firstVersion[0] != secondVersion[0]) {
			return secondVersion[0] - firstVersion[0];
		} else if (firstVersion[1] != secondVersion[1]) {
			return secondVersion[1] - firstVersion[1];
		} else {
			return secondVersion[2] - firstVersion[2];
		}
	}

	private int[] getVersionInts(ICuteHeaders headers) {
		String[] parts = headers.getVersionNumber().split("\\.");
		int major = Integer.parseInt(parts[0]);
		int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
		int revision = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
		return new int[] { major, minor, revision };
	}
}
