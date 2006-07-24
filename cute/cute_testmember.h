#ifndef CUTE_TESTMEMBER_H_
#define CUTE_TESTMEMBER_H_
#include "cute_test.h"
#include <boost/bind.hpp>

template <typename TestClass>
test makeMemberFunctionTest(TestClass &t,void (TestClass::*fun)(),char const *name){
	return test(boost::bind(fun,boost::ref(t)),test::demangle(typeid(t).name())+"::"+name);
}
template <typename TestClass>
test makeMemberFunctionTest(TestClass &t,void (TestClass::*fun)()const,char const *name){
	return test(boost::bind(fun,boost::cref(t)),test::demangle(typeid(t).name())+"::"+name);
}
template <typename TestClass>
test makeSimpleMemberFunctionTest(void (TestClass::*fun)()const,char const *name){
	return test(boost::bind(fun,TestClass()),test::demangle(typeid(TestClass).name())+"::"+name);
}
template <typename TestClass>
test makeSimpleMemberFunctionTest(void (TestClass::*fun)(),char const *name){
	return test(boost::bind(fun,TestClass()),test::demangle(typeid(TestClass).name())+"::"+name);
}
#define CUTE_tm(TestClass,MemberFunctionName) &TestClass::MemberFunctionName,#MemberFunctionName
#define CUTE_MEMFUN(testobject,TestClass,MemberFunctionName) makeMemberFunctionTest(testobject,CUTE_tm(TestClass,MemberFunctionName))
#define CUTE_SMEMFUN(TestClass,MemberFunctionName) makeSimpleMemberFunctionTest(CUTE_tm(TestClass,MemberFunctionName))
#endif /*CUTE_TESTMEMBER_H_*/
