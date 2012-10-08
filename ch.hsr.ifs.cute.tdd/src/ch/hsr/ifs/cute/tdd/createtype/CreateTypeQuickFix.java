/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createtype;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup.Proposal;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.graphics.Image;

import ch.hsr.ifs.cute.tdd.CRefactoring3;
import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddQuickFix;
import ch.hsr.ifs.cute.tdd.LinkedMode.ChangeRecorder;
import ch.hsr.ifs.cute.tdd.createfunction.LinkedModeInformation;

/**
 * Interface between UI and refactoring. Controls the CreateClassRefactoring and sets up linked mode editing after the changes have been performed.
 */
public class CreateTypeQuickFix extends TddQuickFix {

	@Override
	public String getLabel() {
		ca = new CodanArguments(marker);
		return Messages.CreateTypeQuickFix_0 + ca.getName() + Messages.CreateTypeQuickFix_1;
	}

	@Override
	public Image getImage() {
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_STRUCT);
	}

	@Override
	protected CRefactoring3 getRefactoring(ITextSelection selection) {
		return new CreateTypeRefactoring(selection, ca);
	}

	@Override
	protected void configureLinkedMode(ChangeRecorder rec, LinkedModeInformation lmi) throws BadLocationException {
		lmi.addPosition(rec.getSpecBegin(), rec.getSpecLength());
		lmi.addPosition(rec.getBracketPosition(), 0);
		lmi.addPosition(rec.getEndOfMarkedLine(), 0);
		lmi.addProposal(rec.getSpecBegin(), getTypeProposals());
	}

	public static Proposal[] getTypeProposals() {
		return new Proposal[] { new Proposal("class", CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CLASS), 0), //$NON-NLS-1$
				new Proposal("struct", CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_STRUCT), 0), //$NON-NLS-1$
				new Proposal("enum", CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_ENUMERATION), 0) //$NON-NLS-1$
		};
	}
}
