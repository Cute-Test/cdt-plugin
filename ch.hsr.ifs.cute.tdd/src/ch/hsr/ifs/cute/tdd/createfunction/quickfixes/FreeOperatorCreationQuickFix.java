/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createfunction.quickfixes;

import org.eclipse.jface.text.BadLocationException;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.LinkedMode.ChangeRecorder;
import ch.hsr.ifs.cute.tdd.createfunction.LinkedModeInformation;
import ch.hsr.ifs.cute.tdd.createfunction.strategies.IFunctionCreationStrategy;
import ch.hsr.ifs.cute.tdd.createfunction.strategies.OperatorCreationStrategy;

public class FreeOperatorCreationQuickFix extends
		AbstractFunctionCreationQuickFix {

	public FreeOperatorCreationQuickFix() {
		setFree(true);
	}

	@Override
	protected IFunctionCreationStrategy getStrategy() {
		return new OperatorCreationStrategy(true);
	}

	@Override
	public String getLabel() {
		ca = new CodanArguments(marker);
		return "Create free operator " + ca.getName();
	}

	@Override
	protected void configureLinkedMode(ChangeRecorder rec,
			LinkedModeInformation lmi) throws BadLocationException {
		configureLinkedModeWithDeclSpec(rec, lmi);
	}
}
