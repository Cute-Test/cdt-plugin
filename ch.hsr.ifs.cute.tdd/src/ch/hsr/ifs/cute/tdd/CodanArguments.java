/*******************************************************************************
 * Copyright (c) 2011-2014, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd;

import java.util.ArrayList;

import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.core.resources.IMarker;

@SuppressWarnings("restriction")
public class CodanArguments {

	private static final String EMPTY_STRING = "";
	private String name;
	private String message;
	private String strategy;
	private int candNr = 0;
	private String candidates;
	private String templateargs;
	private int nodeOffset = -1;
	private int nodeLength = -1;

	public CodanArguments(IMarker marker) {
		if (marker != null && CodanProblemMarker.getProblemArguments(marker).length != 8) {
			return;
		}
		try {
			setMessage(CodanProblemMarker.getMessage(marker));
			setName(CodanProblemMarker.getProblemArgument(marker, 1));
			setStrategy(CodanProblemMarker.getProblemArgument(marker, 2));
			try {
				int candidateNr = new Integer(CodanProblemMarker.getProblemArgument(marker, 3));
				setCandidate(candidateNr);
				if (candidateNr > 0) {
					setCandidates(CodanProblemMarker.getProblemArgument(marker, 4));
				}
			} catch (NumberFormatException e) {
				setCandidate(-1);
			}
			setTemplateArgs(CodanProblemMarker.getProblemArgument(marker, 5));
			setNodeOffset(Integer.parseInt(CodanProblemMarker.getProblemArgument(marker, 6)));
			setNodeLength(Integer.parseInt(CodanProblemMarker.getProblemArgument(marker, 7)));
		} catch (ArrayIndexOutOfBoundsException e) {
		}
	}

	public CodanArguments(String missingName, String message, String strategy) {
		setName(missingName);
		setMessage(message);
		setStrategy(strategy);
		setCandidate(0);
		setCandidates(EMPTY_STRING);
		setTemplateArgs(EMPTY_STRING);
	}

	public CodanArguments(String missingName, String message, String strategy, String templateArguments) {
		this(missingName, message, strategy);
		setCandidate(0);
		setCandidates(EMPTY_STRING);
		setTemplateArgs(templateArguments);
	}

	public Object[] toArray() {
		ArrayList<String> result = new ArrayList<String>();
		result.add(getMessage());
		result.add(getName());
		result.add(getStrategy());
		result.add(Integer.toString(getCandidateNr()));
		result.add(getCandidates());
		result.add(getTemplateArgs());
		result.add(Integer.toString(getNodeOffset()));
		result.add(Integer.toString(getNodeLength()));
		String[] s = new String[8];
		return result.toArray(s);
	}

	public String getTemplateArgs() {
		return templateargs;
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getMessage() {
		return message;
	}

	private void setMessage(String message) {
		this.message = message;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public int getCandidateNr() {
		return candNr;
	}

	public void setCandidate(int cand) {
		this.candNr = cand;
	}

	public void setCandidates(String candidates) {
		this.candidates = candidates;
	}

	public String getCandidates() {
		return candidates;
	}

	public void setTemplateArgs(String templateargs) {
		this.templateargs = templateargs;
	}

	public boolean isFreeOperator() {
		return getStrategy().equals(":freeoperator") || getStrategy().equals(":anyoperator");
	}

	public boolean isMemberOperator() {
		return getStrategy().equals(":memberoperator") || getStrategy().equals(":anyoperator");
	}

	public boolean isStaticCase() {
		return getStrategy().equals(":staticfreefunc");
	}

	public boolean isCtorCase() {
		return getStrategy().equals(":ctor");
	}

	public boolean isMemberVariableCase() {
		return getStrategy().equals(":memberVariable");
	}

	public int getNodeOffset() {
		return nodeOffset;
	}

	public void setNodeOffset(int nodeOffset) {
		this.nodeOffset = nodeOffset;
	}

	public int getNodeLength() {
		return nodeLength;
	}

	public void setNodeLength(int nodeLength) {
		this.nodeLength = nodeLength;
	}
}
