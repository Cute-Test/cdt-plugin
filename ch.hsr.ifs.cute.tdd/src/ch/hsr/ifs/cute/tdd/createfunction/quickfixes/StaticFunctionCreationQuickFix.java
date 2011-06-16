/*******************************************************************************
 * Copyright (c) 2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *  
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *******************************************************************************/
package ch.hsr.ifs.cute.tdd.createfunction.quickfixes;

import ch.hsr.ifs.cute.tdd.CodanArguments;
import ch.hsr.ifs.cute.tdd.createfunction.strategies.IFunctionCreationStrategy;
import ch.hsr.ifs.cute.tdd.createfunction.strategies.StaticFunctionCreationStrategy;

public class StaticFunctionCreationQuickFix extends
		AbstractFunctionCreationQuickFix {

	@Override
	protected IFunctionCreationStrategy getStrategy() {
		return new StaticFunctionCreationStrategy();
	}

	@Override
	public String getLabel() {
		ca = new CodanArguments(marker);
		return "Create static function " + ca.getName();
	}
}
