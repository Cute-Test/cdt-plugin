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

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.ICheckersRegistry;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;

import ch.hsr.ifs.cute.ui.test.UiTestPlugin;

/**
 * @author Emanuel Graf IFS
 * 
 */
public class UnregisteredTestFunctionCheckerTest extends CheckerTestCase {

	private final String ID = "ch.hsr.ifs.cute.unregisteredTestMarker";

	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableUnregisteredTestCheckMarker();
	}

	private void enableUnregisteredTestCheckMarker() {
		ICheckersRegistry p = CodanRuntime.getInstance().getCheckersRegistry();
		IProblem[] problems = p.getWorkspaceProfile().getProblems();
		for (IProblem problem : problems) {
			if (problem instanceof IProblemWorkingCopy) {
				IProblemWorkingCopy problemWorkingCopy = (IProblemWorkingCopy) problem;
				if (problemWorkingCopy.getId().equals(ID)) {
					problemWorkingCopy.setEnabled(true);
				} else {
					problemWorkingCopy.setEnabled(false);
				}
			}
		}
	}

	// #define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	// #define ASSERT(cond) ASSERTM(#cond,cond)
	//
	// void thisIsAUnregisteredTest(){
	// ASSERT(true);
	// }
	//
	// void runSuite() {
	// cute::suite s;
	// }
	public void testUnregisteredTestFunction() {
		runProjectForCommentCode();
		checkErrorLine(4, ID);
	}

	private void runProjectForCommentCode() {
		try {
			loadcode(getAboveComment());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		runOnProject();
	}

	// #define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	// #define ASSERT(cond) ASSERTM(#cond,cond)
	// #define CUTE(name) cute::test((&name),(#name))
	//
	// void thisIsATest(){
	// ASSERT(true);
	// }
	//
	// void runSuite() {
	// cute::suite s;
	// s.push_back(CUTE(thisIsATest));
	// }
	public void testRegisteredTestFunction() {
		runProjectForCommentCode();
		checkNoErrors();
	}

	// #define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	// #define ASSERT(cond) ASSERTM(#cond,cond)
	// #define CUTE(name) cute::test((&name),(#name))
	//
	// void thisIsATest(){
	// ASSERT(true);
	// }
	//
	// void unregisteredTest(){
	// ASSERT(true);
	// }
	//
	// void runSuite() {
	// cute::suite s;
	// s.push_back(CUTE(thisIsATest));
	// }
	public void testRegisteredTestFunction2() {
		runProjectForCommentCode();
		checkErrorLine(9, ID);
	}

	// #define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	// #define ASSERT(cond) ASSERTM(#cond,cond)
	//
	// struct testFunctor{
	// void operator()(){
	// ASSERT(true);
	// }
	// };
	//
	// void runSuite() {
	// cute::suite s;
	// }
	public void testUnregisteredFunctor() {
		runProjectForCommentCode();
		checkErrorLine(5, ID);
	}

	// #define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	// #define ASSERT(cond) ASSERTM(#cond,cond)
	//
	// struct testFunctor{
	// void operator()(){
	// ASSERT(true);
	// }
	// };
	//
	// void runSuite() {
	// cute::suite s;
	// s.push_back(testFunctor());
	// }
	public void testRegisteredFunctor() {
		runProjectForCommentCode();
		checkNoErrors();
	}

	// #define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	// #define ASSERT(cond) ASSERTM(#cond,cond)
	// #define CUTE_SMEMFUN(TestClass,MemberFunctionName) \
	// cute::makeSimpleMemberFunctionTest<TestClass>(\
	// &TestClass::MemberFunctionName,\
	// #MemberFunctionName)
	//
	// namespace cute{
	// template <typename TestClass, typename MemFun>
	// test makeSimpleMemberFunctionTest(MemFun fun,char const *name);
	// }
	//
	// struct testStruct{
	// void testIt(){
	// ASSERT(true);
	// }
	// };
	//
	// void runSuite() {
	// cute::suite s;
	// s.push_back(CUTE_SMEMFUN(testStruct, testIt));
	// }
	public void testRegisteredMemberFunctionInline() {
		runProjectForCommentCode();
		checkNoErrors();
	}

	// #define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	// #define ASSERT(cond) ASSERTM(#cond,cond)
	// #define CUTE_SMEMFUN(TestClass,MemberFunctionName) \
	// cute::makeSimpleMemberFunctionTest<TestClass>(\
	// &TestClass::MemberFunctionName,\
	// #MemberFunctionName)
	//
	// struct testStruct{
	// void testIt(){
	// ASSERT(true);
	// }
	// };
	//
	// void runSuite() {
	// cute::suite s;
	// }
	public void testUnregisteredMemberFunctionInline() {
		runProjectForCommentCode();
		checkErrorLine(9, ID);
	}

	// #define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	// #define ASSERT(cond) ASSERTM(#cond,cond)
	// #define CUTE_SMEMFUN(TestClass,MemberFunctionName) \
	// cute::makeSimpleMemberFunctionTest<TestClass>(\
	// &TestClass::MemberFunctionName,\
	// #MemberFunctionName)
	//
	// namespace cute{
	// template <typename TestClass, typename MemFun>
	// test makeSimpleMemberFunctionTest(MemFun fun,char const *name);
	// }
	//
	// struct testStruct{
	// void testIt();
	// }
	// };
	//
	// void testStruct::testIt(){
	// ASSERT(true);
	// }
	//
	// void runSuite() {
	// cute::suite s;
	// s.push_back(CUTE_SMEMFUN(testStruct, testIt));
	// }
	public void testRegisteredMemberFunction() {
		runProjectForCommentCode();
		checkNoErrors();
	}

	// #define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	// #define ASSERT(cond) ASSERTM(#cond,cond)
	// #define CUTE_SMEMFUN(TestClass,MemberFunctionName) \
	// cute::makeSimpleMemberFunctionTest<TestClass>(\
	// &TestClass::MemberFunctionName,\
	// #MemberFunctionName)
	//
	// struct testStruct{
	// void testIt();
	// }
	// };
	//
	// void testStruct::testIt(){
	// ASSERT(true);
	// }
	//
	// void runSuite() {
	// cute::suite s;
	// }
	public void testUnregisteredMemberFunction() {
		runProjectForCommentCode();
		checkErrorLine(13, ID);
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	protected StringBuffer[] getContents(int sections) {
		try {
			UiTestPlugin plugin = UiTestPlugin.getDefault();
			return TestSourceReader.getContentsForTest(plugin.getBundle(), "src", getClass(), getName(), sections); //$NON-NLS-1$
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}

}
