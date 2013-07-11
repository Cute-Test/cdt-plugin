/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Emanuel Graf
 *
 */
public class TestFailure extends TestResult {
	
	private static final String REG_EXP = "((.*)(\t)(.*)(\t)(.*)(\t)(.*)(\t))"; //$NON-NLS-1$
	
	protected String expected;
	protected String was;
	protected String middle;

	public TestFailure(String msg) {
		super();
		Pattern pattern = Pattern.compile(REG_EXP);
		Matcher matcher = pattern.matcher(msg);
		if(matcher.find()) {
			this.msg = unquoteMsg(matcher.group(2));
			expected = unquote(matcher.group(4));
			middle = unquote(matcher.group(6));
			was = unquote(matcher.group(8));
		}else {
			this.msg = msg;
		}
	}


	@Override
	public String getMsg() {
		StringBuilder strBuild = new StringBuilder();
		strBuild.append(msg);
		if(expected != null && was != null) {
			strBuild.append(' ');
			strBuild.append(expected);
			strBuild.append(' ');
			strBuild.append(middle);
			strBuild.append(' ');
			strBuild.append(was);
		}
		return strBuild.toString();
	}
	
	public String getExpected() {
		return expected;
	}
	
	public String getWas() {
		return was;
	}
	
	private String unquoteMsg(String text) {
		String ret = text.replaceAll("\\\\{2}+", "\\\\");  //$NON-NLS-1$//$NON-NLS-2$
		return ret;
	}
	
	private String unquote(String text) {
		String ret = text.replaceAll("\\\\t", "\t"); //$NON-NLS-1$ //$NON-NLS-2$
		ret = ret.replaceAll("\\\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
		ret = ret.replaceAll("\\\\r", "\r"); //$NON-NLS-1$ //$NON-NLS-2$
		ret = ret.replaceAll("\\{2}+", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
		return ret;
	}

}
