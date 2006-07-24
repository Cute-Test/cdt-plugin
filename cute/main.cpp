#include "cute_expect.h"
#include "cute.h"
#include "cute_runner.h"
#include "ostream_signaler.h"
#include "cute_testmember.h"
#include "cute_counting_signaler.h"
#include "cute_suite_test.h"
#include "cute_timing_signaler.h"
#include <iostream>
#include <boost/assign.hpp>
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
	using namespace boost::assign;
	suite s;
	s += CUTE(test1);
	s += test3();
	s += CUTE(test0);
	s.push_back( CUTE(test1));
	s.push_back(CUTE(test2));
	s += CUTE_EXPECT(test3(),std::exception);
	s += CUTE_EXPECT(CUTE(test0),std::exception);
	ostream_signaler coutsig(cout);
	suite s2;
	runner<timing_signaler<counting_signaler<ostream_signaler> > >  run;
	run(s);
	cerr << "first run done" << endl;
	s2 += s;
	s2 += CUTE(test1);
	//makeRunner(coutsig)(s2);
	TestClass tc;
	suite s3;
	s3+= suite_test(s2);
	s3 += CUTE_SMEMFUN(TestClass,test1);
	s3 += CUTE_SMEMFUN(TestClass,test2);
	s3 += CUTE_SMEMFUN(TestClass,test3);
	s3 += CUTE_MEMFUN(tc,TestClass,test1);
	s3 += CUTE_MEMFUN(tc,TestClass,test2);
	s3 += CUTE_MEMFUN(tc,TestClass,test3);
	s3 += CUTE_MEMFUN(tc,TestClass,test3);
	run(s3);
	cerr << flush;
	cerr << run.numberOfTests << " Tests " << endl;
	cerr << run.failedTests << " failed" << endl;
}
