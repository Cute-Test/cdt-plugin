/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createvariable;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.cute.tdd.TddCRefactoring;
import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddQuickFix;
import ch.hsr.ifs.cute.tdd.LinkedMode.ChangeRecorder;
import ch.hsr.ifs.cute.tdd.createfunction.LinkedModeInformation;

public class CreateMemberVariableQuickFix extends TddQuickFix {

	@Override
	public String getLabel() {
		ca = new CodanArguments(marker);
		return Messages.CreateMemberVariableQuickFix_0 + ca.getName();
	}

	@Override
	public Image getImage() {
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_PUBLIC_METHOD);
	}

	@Override
	protected TddCRefactoring getRefactoring(ITextSelection selection) {
		return new CreateMemberVariableRefactoring(selection, ca);
	}

	@Override
	protected void configureLinkedMode(ChangeRecorder rec, LinkedModeInformation lmi) throws BadLocationException {
		lmi.addPosition(rec.getSpecBegin(), rec.getSpecLength());
		lmi.addPosition(rec.getEndOfMarkedLine(), 0);
	}
}
