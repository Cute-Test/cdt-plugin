/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.test.checkers;

import java.io.IOException;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;

import ch.hsr.ifs.cute.ui.test.UiTestPlugin;

/**
 * @author Emanuel Graf IFS
 *
 */
public class UnregisteredTestFunctionCheckerTest extends CheckerTestCase {

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//#define ASSERT(cond) ASSERTM(#cond,cond)
	//
	//void thisIsAUnregisteredTest(){
	//	ASSERT(true);
	//}
	//
	//void runSuite() {
	//	cute::suite s;
	//}
	public void testUnregisteredTestFunction() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(4);
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//#define ASSERT(cond) ASSERTM(#cond,cond)
	//#define CUTE(name) cute::test((&name),(#name))
	//
	//void thisIsATest(){
	//	ASSERT(true);
	//}
	//
	//void runSuite() {
	//	cute::suite s;
	//	s.push_back(CUTE(thisIsATest));
	//}
	public void testRegisteredTestFunction() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//#define ASSERT(cond) ASSERTM(#cond,cond)
	//#define CUTE(name) cute::test((&name),(#name))
	//
	//void thisIsATest(){
	//	ASSERT(true);
	//}
	//
	//void unregisteredTest(){
	//    ASSERT(true);
	//}
	//
	//void runSuite() {
	//	cute::suite s;
	//	s.push_back(CUTE(thisIsATest));
	//}
	public void testRegisteredTestFunction2() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(9);
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//#define ASSERT(cond) ASSERTM(#cond,cond)
	//
	//struct testFunctor{
	//	void operator()(){
	//		ASSERT(true);
	//	}
	//};
	//
	//void runSuite() {
	//	cute::suite s;
	//}
	public void testUnregisteredFunctor() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(5);
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//#define ASSERT(cond) ASSERTM(#cond,cond)
	//
	//struct testFunctor{
	//	void operator()(){
	//		ASSERT(true);
	//	}
	//};
	//
	//void runSuite() {
	//	cute::suite s;
	//	s.push_back(testFunctor());
	//}
	public void testRegisteredFunctor() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//#define ASSERT(cond) ASSERTM(#cond,cond)
	//#define CUTE_SMEMFUN(TestClass,MemberFunctionName) \
	//cute::makeSimpleMemberFunctionTest<TestClass>(\
	//		&TestClass::MemberFunctionName,\
	//		#MemberFunctionName)
	//
	//struct testStruct{
	//	void testIt(){
	//		ASSERT(true);
	//	}
	//};
	//
	//void runSuite() {
	//	cute::suite s;
	//	s.push_back(CUTE_SMEMFUN(testStruct, testIt));
	//}
	public void testRegisteredMemberFunctionInline() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//#define ASSERT(cond) ASSERTM(#cond,cond)
	//#define CUTE_SMEMFUN(TestClass,MemberFunctionName) \
	//cute::makeSimpleMemberFunctionTest<TestClass>(\
	//		&TestClass::MemberFunctionName,\
	//		#MemberFunctionName)
	//
	//struct testStruct{
	//	void testIt(){
	//		ASSERT(true);
	//	}
	//};
	//
	//void runSuite() {
	//	cute::suite s;
	//}
	public void testUnregisteredMemberFunctionInline() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(9);
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//#define ASSERT(cond) ASSERTM(#cond,cond)
	//#define CUTE_SMEMFUN(TestClass,MemberFunctionName) \
	//cute::makeSimpleMemberFunctionTest<TestClass>(\
	//		&TestClass::MemberFunctionName,\
	//		#MemberFunctionName)
	//
	//struct testStruct{
	//	void testIt();
	//	}
	//};
	//
	//void testStruct::testIt(){
	//		ASSERT(true);
	//}
	//
	//void runSuite() {
	//	cute::suite s;
	//	s.push_back(CUTE_SMEMFUN(testStruct, testIt));
	//}
	public void testRegisteredMemberFunction() {
		loadCodeAndRun(getAboveComment());
		checkNoErrors();
	}

	//#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	//#define ASSERT(cond) ASSERTM(#cond,cond)
	//#define CUTE_SMEMFUN(TestClass,MemberFunctionName) \
	//cute::makeSimpleMemberFunctionTest<TestClass>(\
	//		&TestClass::MemberFunctionName,\
	//		#MemberFunctionName)
	//
	//struct testStruct{
	//	void testIt();
	//	}
	//};
	//
	//void testStruct::testIt(){
	//		ASSERT(true);
	//}
	//
	//void runSuite() {
	//	cute::suite s;
	//}
	public void testUnregisteredMemberFunction() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(13);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	protected StringBuffer[] getContents(int sections) {
		try {
			UiTestPlugin plugin = UiTestPlugin.getDefault();
			return TestSourceReader.getContentsForTest(plugin.getBundle(),
					"src", getClass(), getName(), sections); //$NON-NLS-1$
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}

}
