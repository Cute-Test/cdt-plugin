/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.LinkedMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static java.lang.Math.max;

import org.eclipse.cdt.internal.ui.refactoring.togglefunction.NotSupportedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ltk.core.refactoring.Change;

/**
 * Using basic position information of a refactoring, this class can observe
 * changes of a refactoring and calculate positions of certain code elements
 * after the change.
 */
public class ChangeRecorder {

	private static final String _ = "_"; //$NON-NLS-1$
	private static final String RETURN = "return "; //$NON-NLS-1$
	private static final String SEPARATOR = ", "; //$NON-NLS-1$
	private int markerOffset;
	private final IDocument document;
	private final Change change;
	private final NestedEdit edit;
	private final String markerText;
	private boolean success;

	public ChangeRecorder(int markerOffset, IDocument documentWithMarker,
			Change toBePerformed, String markerText) {
		this.markerOffset = markerOffset;
		this.document = documentWithMarker;
		this.change = toBePerformed;
		this.markerText = markerText;
		this.edit = new NestedEdit(change);
		execute();
	}

	/**
	 * Observes position change while executing the refactoring changes.
	 */
	private void execute() {
		try {
			int lengthBefore = document.getLength();
			change.perform(new NullProgressMonitor());
			calculateOffsets(lengthBefore);
			success = true;
		} catch (CoreException e) {
			throw new RuntimeException(Messages.ChangeRecorder_1);
		}
	}

	private void calculateOffsets(int lengthBefore) {
		markerOffset += (document.getLength() - lengthBefore);
	}

	private int getMarkerOffset() {
		return markerOffset;
	}

	public int getSpecEnd() throws BadLocationException {
		for (int i = getDeclaratorOffset(); i >= 0; i--) {
			// Don't include reference symbol because the text needs to be the same as in the return statement
			boolean isReference = false;
			if (i > 0 && document.getChar(i-1) == '&') {
				isReference = true;
			}
			if (Character.isWhitespace(document.getChar(i)) && !isReference) {
				return i;
			}
		}
		throw new NotSupportedException(Messages.ChangeRecorder_2);
	}

	public int getSpecBegin() throws BadLocationException {
		int offset = getDeclaratorOffset();
		for (; offset >= 0; offset--) {
			if (document.getChar(offset) == '\n') {
				break;
			}
		}
		offset++;
		for (; offset <= document.getLength(); offset++) {
			if (!Character.isWhitespace(document.getChar(offset))) {
				break;
			}
		}
		return offset;
	}

	private int getDeclaratorOffset() {
		int offset = edit.absoluteIndexOf(markerText);
		if (markerText.matches(".*[()-+|=<>]+.*")) { //$NON-NLS-1$
			offset = edit.absoluteIndexOf("operator"); //$NON-NLS-1$
		}
		return offset;
	}

	public int getBracketPosition() throws BadLocationException {
		int begin = edit.getOffset();
		while (begin < document.getLength() && document.getChar(begin) != '{') {
			begin ++;
		}
		return begin + 1;
	}

	public int getEndOfMarkedLine() throws BadLocationException {
		for (int i = getMarkerOffset(); i < document.getLength(); i++) {
			if (document.getChar(i) == ';') {
				return i + 1;
			}
			if (document.getChar(i) == '\n'){
				return i;
			}
		}
		throw new NotSupportedException(Messages.ChangeRecorder_5);
	}

	public String getSpecifier() throws BadLocationException {
		int length = getSpecEnd() - getSpecBegin();
		return document.get(getSpecBegin(), length);
	}

	public int getSpecLength() throws BadLocationException {
		return max(getSpecEnd() - getSpecBegin(), 0);
	}

	public int getRetLength() {
		return (edit.getOffset() + edit.getText().indexOf(';')) - getRetBegin();
	}

	public int getRetBegin() {
		return edit.getOffset() + edit.getText().indexOf(RETURN) + RETURN.length();
	}

	public IDocument getDocument() {
		return document;
	}

	public List<Position> getParameterPositions() throws BadLocationException {
		List<Position> list = new ArrayList<Position>();
		List<String> params = Arrays.asList(extractParameters());
		if (params.get(0).isEmpty()) {
			return list;
		}
		if (params.contains(_)) {
			list.addAll(getAddArgumentPositions(params));
			return list;
		}
		list.addAll(getParameterNamePositions(params));
		list.addAll(getParameterSpecPositions(params));
		return list;
	}

	private String[] extractParameters() throws BadLocationException {
		int end = getParameterEnd();
		int begin = getParameterOffset();
		String params = document.get(begin, end - begin);
		return params.split(SEPARATOR);
	}

	private List<Position> getParameterSpecPositions(List<String> params) throws BadLocationException {
		List<Position> list = new ArrayList<Position>();
		int offset = getParameterOffset();
		for (String param : params) {
			if (param.indexOf(' ') == -1) {
				list.add(new Position(offset, param.length()));
			} else {
				list.add(new Position(offset, param.lastIndexOf(' ')));
			}
			offset += param.length() + SEPARATOR.length();
		}
		return list;
	}

	private List<Position> getParameterNamePositions(List<String> params) throws BadLocationException {
		List<Position> list = new ArrayList<Position>();
		int offset = getParameterOffset();
		for (String param : params) {
			if (param.indexOf(' ') == -1) {
				continue;
			}
			int beginOfName = offset + param.lastIndexOf(' ') + 1;
			int nameLength = param.length() - param.lastIndexOf(' ') - 1;
			list.add(new Position(beginOfName, nameLength));
			offset += param.length() + SEPARATOR.length();
		}
		return list;
	}

	private List<Position> getAddArgumentPositions(List<String> params) throws BadLocationException {
		List<Position> list = new ArrayList<Position>();
		int offset = getParameterOffset();
		for (String param : params) {
			int beginOfName = offset + param.lastIndexOf(' ') + 1;
			int nameLength = param.length() - param.lastIndexOf(' ') - 1;
			offset += param.length() + SEPARATOR.length();
			if (!param.equals(_)) {
				continue;
			}
			list.add(new Position(beginOfName, nameLength));
		}
		return list;
	}

	private int getParameterOffset() throws BadLocationException {
		int begin = getParameterEnd();
		while (begin > 0 && document.getChar(begin) != '(') {
			begin--;
		}
		return begin + 1;
	}

	private int getParameterEnd() throws BadLocationException {
		int end = edit.getOffset();
		while (end < document.getLength() && document.getChar(end) != ')') {
			end++;
		}
		return end;
	}

	public int getConstOffset() {
		return edit.getOffset() + edit.getText().indexOf(Messages.ChangeRecorder_10) + 2;
	}

	public boolean isSuccessful() {
		return success;
	}
}
