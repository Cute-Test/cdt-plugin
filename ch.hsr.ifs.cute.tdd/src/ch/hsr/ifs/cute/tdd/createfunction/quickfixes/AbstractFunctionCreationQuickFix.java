/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createfunction.quickfixes;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.cute.tdd.CRefactoring3;
import ch.hsr.ifs.cute.tdd.TddQuickFix;
import ch.hsr.ifs.cute.tdd.LinkedMode.ChangeRecorder;
import ch.hsr.ifs.cute.tdd.createfunction.CreateFreeFunctionRefactoring;
import ch.hsr.ifs.cute.tdd.createfunction.CreateMemberFunctionRefactoring;
import ch.hsr.ifs.cute.tdd.createfunction.LinkedModeInformation;
import ch.hsr.ifs.cute.tdd.createfunction.strategies.IFunctionCreationStrategy;

public abstract class AbstractFunctionCreationQuickFix extends TddQuickFix {

	private boolean free;

	@Override
	public org.eclipse.swt.graphics.Image getImage() {
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_FUNCTION);
	};

	public void setFree(boolean free) {
		this.free = free;
	}

	@Override
	protected CRefactoring3 getRefactoring(ITextSelection selection) {
		if (free) {
			return new CreateFreeFunctionRefactoring(selection, ca, getStrategy());
		}
		return new CreateMemberFunctionRefactoring(selection, ca, getStrategy());
	}

	public void handleReturn(ChangeRecorder rec, LinkedModeInformation lmi) throws BadLocationException {
		if (lmi.getReturnStatment()) {
			lmi.addPosition(rec.getRetBegin(), rec.getRetLength() - "()".length(), rec.getSpecBegin()); //$NON-NLS-1$
			lmi.addPosition(rec.getRetBegin() + rec.getRetLength() - "(".length(), 0); //$NON-NLS-1$
		} else {
			lmi.addPosition(rec.getBracketPosition(), 0);
		}
	}

	protected abstract IFunctionCreationStrategy getStrategy();

	public void configureLinkedModeWithDeclSpec(ChangeRecorder rec, LinkedModeInformation lmi) throws BadLocationException {
		lmi.addPosition(rec.getSpecBegin(), rec.getSpecLength());
		handleReturn(rec, lmi);
		lmi.addPositions(rec.getParameterPositions());
		lmi.addPosition(rec.getEndOfMarkedLine(), 0);
	}

	public void configureLinkedModeWithConstAndCtor(ChangeRecorder rec, LinkedModeInformation lmi) throws BadLocationException {
		if (lmi.getDeclSpec()) {
			lmi.addPosition(rec.getSpecBegin(), rec.getSpecLength());
		}
		lmi.addPositions(rec.getParameterPositions());
		handleReturn(rec, lmi);
		if (lmi.getConst()) {
			lmi.addPosition(rec.getConstOffset(), "const".length()); //$NON-NLS-1$
		}
	}
}
