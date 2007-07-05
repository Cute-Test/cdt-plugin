/*******************************************************************************
 * Copyright (c) 2007 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Emanuel Graf - initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cutelauncher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IRegion;

public abstract class TestEventHandler {

	public static final String LINE_QUALIFIER = "#";
	private static final int LINEPREFIXLENGTH = LINE_QUALIFIER.length();
	public static final String BEGINNING = "beginning";
	public static final String ENDING = "ending";
	public static final String FAILURE = "failure";
	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
	public static final String STARTTEST = "starting";

	protected abstract void handleBeginning(IRegion reg, String suitename, String suitesize);

	protected abstract void handleTestStart(IRegion reg, String testname);

	protected abstract void handleError(IRegion reg, String testName, String msg);

	protected abstract void handleSuccess(IRegion reg, String name, String msg);

	protected abstract void handleEnding(IRegion reg, String suitename);

	protected abstract void handleFailure(IRegion reg, String testName, String fileName, String lineNo, String reason);
	
	public abstract void handleSessionStart();
	
	public abstract void handleSessionEnd();
	

	private static Pattern SUITEBEGINNINGLINE = Pattern.compile(LINE_QUALIFIER+BEGINNING+" (.*) (\\d+)$");
	private static Pattern SUITEENDINGLINE = Pattern.compile(LINE_QUALIFIER+ENDING+" (.*)$");
	private static Pattern TESTSTARTLINE = Pattern.compile(LINE_QUALIFIER+STARTTEST+" (.*)$");
	private static Pattern TESTFAILURELINE = Pattern.compile(LINE_QUALIFIER+FAILURE+" (.*) (.*):(\\d+) (.*)$");
	private static Pattern TESTSUCESSLINE = Pattern.compile(LINE_QUALIFIER+SUCCESS+" (.*) (.*)$");
	private static Pattern TESTERRORLINE = Pattern.compile(LINE_QUALIFIER+ERROR+" (.*?) (.*)$");


	public  void handle(IRegion reg, String line) {
		try {
			if(line.startsWith(BEGINNING,LINEPREFIXLENGTH)) {
				Matcher m = createMatcher(SUITEBEGINNINGLINE, line);
				this.handleBeginning(reg, m.group(1), m.group(2));
			}else if(line.startsWith(ENDING,LINEPREFIXLENGTH)) {
				Matcher m = createMatcher(SUITEENDINGLINE, line);
				this.handleEnding(reg, m.group(1));
			} else if(line.startsWith(STARTTEST,LINEPREFIXLENGTH)) {
				Matcher m = createMatcher(TESTSTARTLINE, line);
				this.handleTestStart(reg, m.group(1));
			}else if(line.startsWith(FAILURE,LINEPREFIXLENGTH)) {
				Matcher m = createMatcher(TESTFAILURELINE, line);
				handleFailure(reg, m.group(1), m.group(2), m.group(3), m.group(4));
			}else if(line.startsWith(SUCCESS,LINEPREFIXLENGTH)) {
				Matcher m = createMatcher(TESTSUCESSLINE, line);
				handleSuccess(reg, m.group(1), m.group(2));
			}else if(line.startsWith(ERROR, LINEPREFIXLENGTH)) {
				Matcher m = createMatcher(TESTERRORLINE, line);
				handleError(reg, m.group(1), m.group(2));
			}
		}catch(CoreException e) {
			CuteLauncherPlugin.getDefault().getLog().log(e.getStatus());
		}
	}

	private Matcher createMatcher(Pattern pattern, String line) throws CoreException {
		Matcher m = pattern.matcher(line);
		if(!m.matches()) {
			throw new CoreException(new Status(Status.ERROR, CuteLauncherPlugin.PLUGIN_ID, 1, "Pattern don't match", null));
		}
		return m;
	}

}