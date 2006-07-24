#ifndef CUTE_SUITE_TEST_H_
#define CUTE_SUITE_TEST_H_
#include "cute_test.h"
#include "cute_suite.h"
#include <algorithm>
// make a whole suite a test, failure stops the suite's execution
struct suite_test {
	suite theSuite;
	suite_test(suite const &s):theSuite(s){}
	void operator()(){
		std::for_each(theSuite.begin(),theSuite.end(),boost::bind(&test::operator(),_1));
	}
};
#endif /*CUTE_SUITE_TEST_H_*/
