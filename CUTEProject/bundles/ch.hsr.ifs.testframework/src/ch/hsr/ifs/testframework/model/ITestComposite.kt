/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.testframework.model;

/**
 * @author Emanuel Graf
 *
 */
interface ITestComposite {

   fun getError(): Int;

   fun getFailure(): Int;

   fun getSuccess(): Int;

   fun getTotalTests(): Int;

   fun getRun(): Int;

   fun getElements(): List<TestElement> 

   fun addTestElement(element: TestElement);

   fun hasErrorOrFailure(): Boolean;

   fun addListener(listener: ITestCompositeListener);

   fun removeListener(listener: ITestCompositeListener);

   fun getRerunName(): String;

}
