/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule für Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Emanuel Graf
 *
 */
public class TestFailure extends TestResult {
	
	private static final String REG_EXP = "((.*)(\t)(.*)(\t)(.*)(\t)(.*)(\t))";
	
	protected String expected;
	protected String was;
	

	public TestFailure(String msg) {
		super();
		Pattern pattern = Pattern.compile(REG_EXP);
		Matcher matcher = pattern.matcher(msg);
		if(matcher.find()) {
			this.msg = unquoteMsg(matcher.group(2));
			expected = unquote(matcher.group(4));
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
			strBuild.append(" but was: ");
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
		String ret = text.replaceAll("\\\\{2}+", "\\\\");
		return ret;
	}
	
	private String unquote(String text) {
		String ret = text.replaceAll("\\\\t", "\t");
		ret = ret.replaceAll("\\\\n", "\n");
		ret = ret.replaceAll("\\{2}+", "\\");
		return ret;
	}

}