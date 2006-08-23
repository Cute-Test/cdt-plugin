#ifndef CUTE_SUITE_H_
#define CUTE_SUITE_H_
#include "cute_test.h"
#include <vector>
namespace cute {
typedef std::vector<test> suite;
// convenience operator for appending suites
suite &operator+=(suite &left, suite const &right);
suite &operator+=(suite &left, test const &right);

// TODO: make a suite from a testing class, no idea how this could be possible?
// in a convenient and intuitive way?
//template <typename TestClass>
//suite makeSuiteFromTestClass(TestClass &t,...){
//// can we vararg template functions?
//	return suite(); 
//}
}
#endif /*CUTE_SUITE_H_*/
