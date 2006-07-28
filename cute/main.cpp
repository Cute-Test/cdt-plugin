#include "cute_expect.h"
#include "cute.h"
#include "cute_equals.h"
#include "cute_runner.h"
#include "ostream_listener.h"
#include "cute_testmember.h"
#include "cute_counting_listener.h"
#include "cute_suite_test.h"
#include "cute_timing_listener.h"
#include "cute_test_incarnate.h"
#include <iostream>

#include "test_cute_equals.h"
#include "test_cute_expect.h"
#include "test_repeated_test.h"
#include "test_cute_runner.h"


using namespace cute;
void test0(){
	sleep(2);
	t_assert(true);
}
void test1(){
	t_assert(false);
}
void test2(){
	assertEquals(1,1);
	assertEquals(1,2);
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
struct TestClass{
	int i;
	TestClass():i(1){}
	void test1()const{ assertEquals(1,i);}
	void test2() { t_assert(false);}
	void test3() { assertEquals(2,++i);}
};
// TODO: more tests for infrastructure
int main(){
	using namespace std;
	suite s;
	s += test_cute_equals();
	s += CUTE_SUITE_TEST(test_cute_expect());
	s += CUTE_SUITE_TEST(test_repeated_test());
	s += CUTE(test_cute_runner);
	//---
	s += CUTE_INCARNATE(to_incarnate_without);
	s += CUTE_INCARNATE_WITH_CONTEXT(to_incarnate,boost::ref(std::cout));
	s += CUTE_CONTEXT_MEMFUN(boost::ref(std::cerr),to_incarnate,operator());
	// TODO: test_cute_equals
	// TODO: test_cute_suite_test
	// TODO: test_cute_suite
	// TODO: test_cute_test
	// TODO: test_cute_membertests
	// TODO: test_timing_listener
	// TODO: test_ostream_listener
	// TODO: test_counting_listener
	// TODO: collecting listener?
//	s += CUTE(test1);
//	s += test3();
//	s += CUTE(test0);
//	s.push_back( CUTE(test1));
//	s.push_back(CUTE(test2));
//	s += CUTE_EXPECT(test3(),std::exception);
//	s += CUTE_EXPECT(CUTE(test0),std::exception);
	ostream_listener coutsig(cout);
	suite s2;
	runner<timing_listener<counting_listener<ostream_listener> > >  run;
	run(s);
	cerr << "first run done" << endl;
//	s2 += s;
//	s2 += CUTE(test1);
//	//makeRunner(coutsig)(s2);
//	TestClass tc;
//	suite s3;
//	s3+= suite_test(s2);
//	s3 += CUTE_SMEMFUN(TestClass,test1);
//	s3 += CUTE_SMEMFUN(TestClass,test2);
//	s3 += CUTE_SMEMFUN(TestClass,test3);
//	s3 += CUTE_MEMFUN(tc,TestClass,test1);
//	s3 += CUTE_MEMFUN(tc,TestClass,test2);
//	s3 += CUTE_MEMFUN(tc,TestClass,test3);
//	s3 += CUTE_MEMFUN(tc,TestClass,test3);
//	run(s3);
	cerr << flush;
	cerr << run.numberOfTests << " Tests " << endl;
	cerr << run.failedTests << " failed" << endl;
}
