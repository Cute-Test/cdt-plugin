#include "cute_runner.h"
#include "cute.h"
#include "cute_counting_listener.h"
#include "test_cute_filter_runner.h"

void test(){
}

void thisIsAtest_cute_filter_runnerTest() {
	char const *argv[]={
			"dummy","testsuite1","testsuite2#test1",0,"testsuite2#test3",0,0
	};
	cute::suite s;
	s += CUTE(test);
	s += cute::test(test,"test1");
	s += cute::test(test,"test2");
	s += cute::test(test,"test3");
	s += cute::test(test,"test4");

	cute::counting_listener<> l;
	cute::runner<cute::counting_listener<> > run=cute::makeRunner(l,sizeof(argv)/sizeof(*argv)-1,argv);
	run(s,"testsuite1");
	run(s,"testsuite2");
	ASSERT_EQUAL(2,l.numberOfSuites);
	ASSERT_EQUAL(s.size()+2,l.numberOfTests);
	ASSERT_EQUAL(0,l.errors);
	ASSERT_EQUAL(0,l.failedTests);
	ASSERT_EQUAL(7,l.successfulTests);
}

void test_cute_filter_runner_ArgvFilter(){
	char const *argv[]={
			"dummy","testsuite1","testsuite2#test1",0,"testsuite2#test3",0,0
	};
	cute::runner_aux::ArgvTestFilter filter(sizeof(argv)/sizeof(*argv)-1,argv);
	ASSERT(filter.shouldRun("any"));
	ASSERT(filter.shouldRunSuite("testsuite1"));
	ASSERT(filter.shouldRun("test"));
	ASSERT(filter.shouldRun("test1"));
	ASSERT(filter.shouldRun("test2"));
	ASSERT(filter.shouldRun("test3"));
	ASSERT(filter.shouldRun("test4"));
	ASSERT(!filter.shouldRunSuite("dummy"));
	ASSERT(filter.shouldRunSuite("testsuite2"));
	ASSERT(!filter.shouldRun("test"));
	ASSERT(filter.shouldRun("test1"));
	ASSERT(!filter.shouldRun("test2"));
	ASSERT(filter.shouldRun("test3"));
	ASSERT(!filter.shouldRun("test4"));

}



cute::suite make_suite_test_cute_filter_runner(){
	cute::suite s;
	s.push_back(CUTE(thisIsAtest_cute_filter_runnerTest));
	s.push_back(CUTE(test_cute_filter_runner_ArgvFilter));
	return s;
}



