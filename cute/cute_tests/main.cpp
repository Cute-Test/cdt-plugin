#include "cute.h"
#include "cute_runner.h"
#include "ostream_listener.h"
#include "cute_counting_listener.h"
#include "ide_listener.h"
#include <iostream>

#include "test_cute_equals.h"
#include "test_cute_expect.h"
#include "test_repeated_test.h"
#include "test_cute_runner.h"
#include "test_cute_suite_test.h"
#include "test_cute_suite.h"
#include "test_cute_test_incarnate.h"
#include "test_cute_test.h"
#include "test_cute_testmember.h"
#include "test_cute.h"

using namespace cute;
static int simpleTestfunctionCalled=0;
void simpleTestFunction(){
	++simpleTestfunctionCalled;
	std::cerr << "simpleTestFunction run no:"<< simpleTestfunctionCalled << std::endl;
	ASSERT(true);
	throw std::exception();
}
struct SimpleTestFunctionCalledTest {
	void operator()(){
		ASSERT_EQUALM("look at cute::test ctor overload",2,simpleTestfunctionCalled);
	}
};
void shouldFailButNotThrowStdException(){
	ASSERT(false);
	throw std::exception();
}
void test2(){
	ASSERT_EQUAL(1,1);
	ASSERT_EQUAL(1,2);
}
struct test3{
	void operator()(){
		throw std::exception();
	}
};

struct to_incarnate{
	std::ostream &out;
	to_incarnate(std::ostream &os):out(os){
		out << "born" << std::endl;
	}
	~to_incarnate() {
		out << "killed" << std::endl;
	}
	void operator()(){
		out << "tested" << std::endl;
	}
};
struct to_incarnate_without : to_incarnate {
	to_incarnate_without():to_incarnate(std::cout){}
};
// TODO: more tests for infrastructure


int main(){
	using namespace std;
	suite s;
	s += test_cute_equals();
	s += CUTE(simpleTestFunction);
	s += CUTE_EXPECT(CUTE(simpleTestFunction),std::exception);
	s += SimpleTestFunctionCalledTest();
	s += CUTE_EXPECT(CUTE(shouldFailButNotThrowStdException),cute::test_failure);
	s += CUTE_SUITE_TEST(test_cute_expect());
	s += CUTE_SUITE_TEST(test_repeated_test());
	s += CUTE(test_cute_runner);
	s += CUTE_SUITE_TEST(test_cute_suite_test());
	s += CUTE(test_cute_suite);
	s += CUTE_SUITE_TEST(test_cute_test_incarnate());
	s += CUTE_SUITE_TEST(test_cute_test());
	s += CUTE_SUITE_TEST(test_cute_testmember());
	s += CUTE_SUITE_TEST(test_cute());
	//---
	s += CUTE_INCARNATE(to_incarnate_without);
	s += CUTE_INCARNATE_WITH_CONTEXT(to_incarnate,boost::ref(std::cout));
	s += CUTE_CONTEXT_MEMFUN(boost::ref(std::cerr),to_incarnate,operator());
	// TODO: test_ostream_listener
	// TODO: test_counting_listener
	// TODO: collecting listener?
	runner<counting_listener<ide_listener> > run;
	run(s);

	cerr << flush;
	cerr << run.numberOfTests << " Tests " << endl;
	cerr << run.failedTests << " failed - expect 0 failures" << endl;
	cerr << run.errors << " errors - expect 1 error" << endl;
	return run.failedTests;
}
