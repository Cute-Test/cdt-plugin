#ifndef CUTE_TEST_H_
#define CUTE_TEST_H_
#include <boost/function.hpp>
// make plain functions as tests more 'cute':
#define CUTE(name) cute::test((name),#name)
namespace cute {
template <typename> struct cute_expect;

struct test{
	typedef void(*testfunction)(); 
	std::string name()const{ return name_;}
	
	// for CUTE, where a name is given explicitely
	test(testfunction t, std::string n):theTest(t),name_(n){}
	
	// functor types can (almost) spell their name
	template <typename VoidFunctor>
	test(VoidFunctor const &t):theTest(t),name_(demangle(typeid(t).name())){}
	
	// this is for functors derived from member function pointers, i.e. test classes
	template <typename VoidFunctor>
	test(VoidFunctor const &t,std::string name):theTest(t),name_(name){}
	
	void operator()(){ theTest(); }
	
	// this shouldn't belong here, but where?
	static std::string demangle(char const *name);
private:
	typedef boost::function<void()> a_test;
	a_test theTest;
	std::string name_;
};
}
#endif /*CUTE_TEST_H_*/
