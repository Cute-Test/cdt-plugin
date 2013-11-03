/*******************************************************************************
 * Copyright (c) 2007-2012, IFS Institute for Software, HSR Rapperswil,
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
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import ch.hsr.ifs.cute.ui.test.UiTestPlugin;

/**
 * @author Emanuel Graf IFS
 * @author Thomas Corbat IFS
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
	// using cute::suite;
	// suite s;
	// }
	public void testUnregisteredTestFunctionWithUsingDeclaration() {
		runProjectForCommentCode();
		checkErrorLine(4, ID);
	}
	// #define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	// #define ASSERT(cond) ASSERTM(#cond,cond)
	//
	// void thisIsAUnregisteredTest(){
	// ASSERT(true);
	// }
	//
	// void runSuite() {
	// using namespace cute;
	// suite s;
	// }
	public void testUnregisteredTestFunctionWithUsingNamespace() {
		runProjectForCommentCode();
		checkErrorLine(4, ID);
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

	// @file:test.h
	// #define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	// #define ASSERT(cond) ASSERTM(#cond,cond)
	//
	// void thisIsAUnregisteredTest(){
	// ASSERT(true);
	// }
	//
	// @file:test.cpp
	// #include"test.h"
	// void runSuite() {
	// cute::suite s;
	// }
	public void testUnregisteredTestFunctionExternalFile() {
		runProjectForCommentCode();
		checkErrorLine(4, ID);
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
	// namespace std { struct vector{void push_back(...){}}; }
	// namespace cute { typedef std::vector suite; }
	//
	// void thisIsATest(){
	// ASSERT(true);
	// }
	//
	// void runSuite() {
	// using cute::suite;
	// suite s;
	// s.push_back(CUTE(thisIsATest));
	// }
	public void testRegisteredTestFunctionWithUsingDeclaration() {
		runProjectForCommentCode();
		checkNoErrors();
	}

	// #define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	// #define ASSERT(cond) ASSERTM(#cond,cond)
	// #define CUTE(name) cute::test((&name),(#name))
	// namespace cute { struct suite{};}
	// void thisIsATest(){
	// ASSERT(true);
	// }
	//
	// void runSuite() {
	// using namespace cute;
	// suite s;
	// s.push_back(CUTE(thisIsATest));
	// }
	public void testRegisteredTestFunctionWithUsingNamespace() {
		runProjectForCommentCode();
		checkNoErrors();
	}


	// #define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
	// #define ASSERT(cond) ASSERTM(#cond,cond)
	// #define CUTE(name) cute::test((&name),(#name))
	//
	// cute::suite s;
	// void thisIsATest(){
	// ASSERT(true);
	// }
	//
	// void runSuite() {
	// s.push_back(CUTE(thisIsATest));
	// }
	public void testNonLocalSuite() {
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
	// #define CUTE_MEMFUN(testobject,TestClass,MemberFunctionName) \
	//	cute::makeMemberFunctionTest(testobject,\
	//			&TestClass::MemberFunctionName,\
	//			#MemberFunctionName)
	//
	// namespace cute{
	// template <typename TestClass, typename MemFun>
	// test makeSimpleMemberFunctionTest(MemFun fun,char const *name);
	// }
	//
	//	struct TestBase
	//	{
	//	    std::string suiteName;
	//	    cute::suite suite;
	//
	//	    TestBase(const std::string& suiteName)
	//	    : suiteName(suiteName)
	//	    {
	//	    }
	//	    virtual void operator() ()
	//	    {
	//	        cute::ide_listener lis;
	//	        cute::makeRunner(lis)(suite, suiteName.c_str());
	//	    }
	//
	//	    virtual ~TestBase()
	//	    {
	//	    }
	//	};
	//	
	// struct DerivTest : public TestBase
	// {
	//    void test()
	//    {
	//        ASSERTM("bad", false);
	//    }
	//
	//    DerivTest() : Base("DerivTest")
	//    {
	//        suite.push_back(CUTE_MEMFUN(*this, DerivTest, test));
	//    }
	// };
	//
	// void runSuite() {
	//     cute::suite s;
	//     s.push_back(DerivTest());
	//     cute::ide_listener lis;
	//     cute::makeRunner(lis)(s, "The Suite");
	// }
	public void testRegisteredMemberFunctionTypeHierarchy() {
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

	@Override
	protected void indexFiles() throws CoreException, InterruptedException {

		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

		super.indexFiles();
	}

	@Override
	protected void runCodan() {
		checkIndexer();
		cleanMarkers();
		super.runCodan();
	}

	private void cleanMarkers() {
		try {
			IMarker[] existingMarkers = cproject.getProject().findMarkers(IProblemReporter.GENERIC_CODE_ANALYSIS_MARKER_TYPE, true, 1);
			assertEquals(0, existingMarkers.length);
			cproject.getProject().deleteMarkers(IProblemReporter.GENERIC_CODE_ANALYSIS_MARKER_TYPE, true, 1);
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}

	private void checkIndexer() {
		IIndexManager indexManager = CCorePlugin.getIndexManager();
		if (!indexManager.isIndexerIdle()) {
			System.err.println(getName() + ": Indexer Running!");
		}
		if (indexManager.isIndexerSetupPostponed(cproject)) {
			System.err.println(getName() + ": Indexer Setup Postponed");
		}

		if (!indexManager.isProjectIndexed(cproject)) {
			System.err.println(getName() + ": Project is not Indexed");
		}

		try {
			IIndex index = indexManager.getIndex(cproject);

			if (index.getLastWriteAccess() <= 0) {
				System.err.println(getName() + ": Last write access: " + index.getLastWriteAccess());
			}
			if (index.getAllFiles().length <= 0) {
				System.err.println(getName() + ": No files indexed!");
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		int waitCount = 0;
		while (!indexManager.isIndexerIdle() && waitCount++ < 60) {
			indexManager.joinIndexer(1000, new NullProgressMonitor());
			System.err.println(getName() + ": Indexer Running!");
		}
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
		checkErrorLine(12, ID);
	}

	private void runProjectForCommentCode() {
		try {
			loadcode(getAboveComment());
		} catch (Exception e) {
			fail(e.getMessage());
		}
		runOnProject();
	}

	@Override
	public boolean isCpp() {
		return true;
	}

	@Override
	protected StringBuilder[] getContents(int sections) {
		try {
			UiTestPlugin plugin = UiTestPlugin.getDefault();
			return TestSourceReader.getContentsForTest(plugin.getBundle(), "src", getClass(), getName(), sections);
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}
}
