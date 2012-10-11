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
	
	public int compare(ICuteHeaders ch1, ICuteHeaders ch2) {
		double dif= ch1.getVersionNumber() - ch2.getVersionNumber();
		if(dif < 0.001)return 0;
		if(dif < 0)return 1;
		return -1;
	}

}
