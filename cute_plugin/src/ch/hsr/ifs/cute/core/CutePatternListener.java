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
package ch.hsr.ifs.cute.core;

import java.util.Vector;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class CutePatternListener implements IPatternMatchListener{
	
	private static final String REGEX =  TestEventHandler.LINE_QUALIFIER + "("+ TestEventHandler.BEGINNING + "|" + TestEventHandler.ENDING + "|" + TestEventHandler.SUCCESS + "|"+ TestEventHandler.STARTTEST + "|" + TestEventHandler.FAILURE + "|" + TestEventHandler.ERROR + ")(.*)(\\n)";
	private TextConsole console; 
	private Vector<TestEventHandler> handlers;

	public CutePatternListener() {
		handlers = new Vector<TestEventHandler>();
	}

	public int getCompilerFlags() {
		return Pattern.UNIX_LINES;
	}

	public String getLineQualifier() {
		return TestEventHandler.LINE_QUALIFIER;
	}

	public String getPattern() {
		return REGEX;
	}

	public void connect(TextConsole console) {
		this.console = console;
		for (TestEventHandler handler : handlers) {
			handler.handleSessionStart();
		}
	}

	public void disconnect() {
		for (TestEventHandler handler : handlers) {
			handler.handleSessionEnd();
		}
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
			for (TestEventHandler handler : handlers) {
				handler.handle(reg,line);
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

}
