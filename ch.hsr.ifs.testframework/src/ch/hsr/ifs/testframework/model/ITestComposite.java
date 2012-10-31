/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.model;

import java.util.Vector;

/**
 * @author Emanuel Graf
 *
 */
public interface ITestComposite {

	public abstract int getError();

	public abstract int getFailure();

	public abstract int getSuccess();

	public abstract int getTotalTests();

	public abstract int getRun();
	
	public abstract Vector<? extends TestElement> getElements();
	
	public void addTestElement(TestElement element);
	
	public boolean hasErrorOrFailure();
	
	public void addListener(ITestCompositeListener listener);
	
	public void removeListener(ITestCompositeListener listener);

}