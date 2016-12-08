package ch.hsr.ifs.cute.namespactor.checker.tests;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.core.runtime.Plugin;

import ch.hsr.ifs.cute.namespactor.checkers.UsingChecker;
import ch.hsr.ifs.cute.namespactor.test.TestActivator;

public class UsingCheckerTest extends CheckerTestCase {
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(UsingChecker.UDEC_IN_HEADER_PROBLEM_ID);
	}

	@Override
	protected Plugin getPlugin() {
		return TestActivator.getDefault();
	}

	@Override
	public boolean isCpp() {
		return true;
	}
	
	//@file:derived.h
	//struct Base {
	//  Base(int){}
	//};
	//struct Derived : Base {
	//  using Base::Base;
	//};
	public void testNoMarkerForInheritingConstructor() throws Exception {
		checkSampleAbove();
	}
}
