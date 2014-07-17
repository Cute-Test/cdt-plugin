/*******************************************************************************
 * Copyright (c) 2011-2014, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createfunction.quickfixes;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.TddCRefactoring;
import ch.hsr.ifs.cute.tdd.LinkedMode.ChangeRecorder;
import ch.hsr.ifs.cute.tdd.createfunction.CreateMemberFunctionRefactoring;
import ch.hsr.ifs.cute.tdd.createfunction.LinkedModeInformation;
import ch.hsr.ifs.cute.tdd.createfunction.strategies.IFunctionCreationStrategy;
import ch.hsr.ifs.cute.tdd.createfunction.strategies.MemberOperatorCreationStrategy;

public class MemberOperatorCreationQuickFix extends AbstractFunctionCreationQuickFix {

	@Override
	protected TddCRefactoring getRefactoring(ITextSelection selection) {
		return new CreateMemberFunctionRefactoring(selection, ca, getStrategy());
	}

	@Override
	protected IFunctionCreationStrategy getStrategy() {
		return new MemberOperatorCreationStrategy();
	}

	@Override
	public String getLabel() {
		ca = new CodanArguments(marker);
		return Messages.MemberOperatorCreationQuickFix_0 + ca.getName();
	}

	@Override
	protected void configureLinkedMode(ChangeRecorder rec, LinkedModeInformation lmi) throws BadLocationException {
		configureLinkedModeWithConstAndCtor(rec, lmi);
	}

	@Override
	public boolean isApplicable(IMarker marker) {
		if (super.isApplicable(marker)) {
			return ca.isMemberOperator();
		}
		return false;
	}
}
