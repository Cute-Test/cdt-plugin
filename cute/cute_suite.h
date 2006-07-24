#ifndef CUTE_SUITE_H_
#define CUTE_SUITE_H_
#include "cute_test.h"
#include <vector>
typedef std::vector<test> suite;
// convenience operator for appending suites
// boost::assign provides it for test already
suite &operator+=(suite &left, suite const &right){
	left.insert(left.end(),right.begin(),right.end());
	return left;
}
// avoid dependency on boost::assign, but it can be used also
suite &operator+=(suite &left, test const &right){
	left.push_back(right);
	return left;
}
// TODO: make a suite from a testing class, no idea how this could be possible?
// in a convenient and intuitive way?
//template <typename TestClass>
//suite makeSuiteFromTestClass(TestClass &t,...){
//// can we vararg template functions?
//	return suite(); 
//}
#endif /*CUTE_SUITE_H_*/
