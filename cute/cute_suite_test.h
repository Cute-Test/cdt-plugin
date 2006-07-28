#ifndef CUTE_SUITE_TEST_H_
#define CUTE_SUITE_TEST_H_
#include "cute_suite.h"
namespace cute{
// make a whole suite a test, failure stops the suite's execution
struct suite_test {
	suite theSuite;
	suite_test(suite const &s):theSuite(s){}
	void operator()();
};
}
#define CUTE_SUITE_TEST(s) cute::test(cute::suite_test((s)),#s)
#endif /*CUTE_SUITE_TEST_H_*/
