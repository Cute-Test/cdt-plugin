/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.addArgument;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.cute.tdd.CRefactoring3;
import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddQuickFix;
import ch.hsr.ifs.cute.tdd.LinkedMode.ChangeRecorder;
import ch.hsr.ifs.cute.tdd.createfunction.LinkedModeInformation;

@SuppressWarnings("restriction")
public class AddArgumentQuickFix extends TddQuickFix {

	private String label;
	private int candidatenr;
	private Image image;
	
	public AddArgumentQuickFix(String label, int candidatenr, Image image) {
		this.label = label;
		this.candidatenr = candidatenr;
		this.image = image;
	}
		
	@Override
	public String getLabel() {
		ca = new CodanArguments(marker);
		return label;
	}
	
	@Override
	public Image getImage() {
		return image;
	}

	@Override
	protected CRefactoring3 getRefactoring(RefactoringASTCache astCache, ITextSelection selection) {
		return new AddArgumentRefactoring(selection, astCache, candidatenr);
	}

	@Override
	protected void configureLinkedMode(ChangeRecorder rec,
			LinkedModeInformation lmi) throws BadLocationException {
		lmi.addPositions(rec.getParameterPositions());
	}
}