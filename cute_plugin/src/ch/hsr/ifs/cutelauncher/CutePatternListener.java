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
package ch.hsr.ifs.cutelauncher;

import java.util.Vector;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class CutePatternListener implements IPatternMatchListener{
	
	private static final String BEGINNING = "#beginning";
	private static final String ENDING = "#ending";
	private static final String FAILURE = "#failure";
	private static final String SUCCESS = "#success";
	private static final String ERROR = "#error";
	private static final String STARTSUITE = "#starting";
	private static final String SEP_STRING = new String(new char[] {0x1F});
	
	private static final String REGEX = "("+ BEGINNING + "|" + ENDING + "|" + SUCCESS + "|"+ STARTSUITE + "|" + FAILURE + "|" + ERROR + ")(.*)(\\n)";
	private TextConsole console; 
	private Vector<TestEventHandler> handlers;

	public CutePatternListener() {
		handlers = new Vector<TestEventHandler>();
	}

	public int getCompilerFlags() {
		return Pattern.UNIX_LINES;
	}

	public String getLineQualifier() {
		return "#";
	}

	public String getPattern() {
		return REGEX;
	}

	public void connect(TextConsole console) {
		this.console = console; 
	}

	public void disconnect() {
		console = null;
	}
	
	public void addHandler(TestEventHandler handler) {
		handlers.add(handler);
	}
	
	public void removeHandler(TestEventHandler handler) {
		handlers.remove(handler);
	}

	public void matchFound(PatternMatchEvent event) {
		
		try {
			IDocument doc = console.getDocument();
			IRegion reg = doc.getLineInformation(doc.getLineOfOffset(event.getOffset()));
			String line = doc.get(reg.getOffset(), reg.getLength());;

			String[] parts = line.split(SEP_STRING);
			if(parts[0].equals(BEGINNING)) {
				for (TestEventHandler handler : handlers) {
					handler.handleBeginning(reg, parts);
				}
			}else if(parts[0].equals(ENDING)) {
				for (TestEventHandler handler : handlers) {
					handler.handleEnding(reg, parts);
				}
			}else if(parts[0].equals(FAILURE)) {
				for (TestEventHandler handler : handlers) {
					handler.handleFailure(reg, parts);
				}
			}else if(parts[0].equals(SUCCESS)) {
				for (TestEventHandler handler : handlers) {
					handler.handleSuccess(reg, parts);
				}
			}else if(parts[0].equals(ERROR)) {
				for (TestEventHandler handler : handlers) {
					handler.handleError(reg, parts);
				}
			}else if(parts[0].equals(STARTSUITE)) {
				for (TestEventHandler handler : handlers) {
					handler.handleTestStart(reg, parts);
				}
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

}
