/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.ui.tests;

public class TestContext {

	private String markerText;
	private int markerBegin;
	private String expectedResult;

	public TestContext(String markerText, int markerBegin, String expectedResult) {
		this.markerText = markerText;
		this.markerBegin = markerBegin;
		this.expectedResult = expectedResult;
	}

	public String getMarkerText() {
		return markerText;
	}

	public int getMarkerBegin() {
		return markerBegin;
	}

	public String getExpectedResult() {
		return expectedResult;
	}

}
