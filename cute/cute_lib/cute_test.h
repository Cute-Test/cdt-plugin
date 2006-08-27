#ifndef CUTE_TEST_H_
#define CUTE_TEST_H_
#include <boost/function.hpp>
// make plain functions as tests more 'cute':
#define CUTE(name) cute::test((name),#name)
namespace cute {

struct test{
	// (real) functor types can (almost) spell their name
	template <typename VoidFunctor>
	test(VoidFunctor const &t):theTest(t),name_(demangle(typeid(t).name())){}

	// for CUTE, or where a name is given explicitely
	// or for functors derived from member function pointers, i.e. test classes
	template <typename VoidFunctor>
	test(VoidFunctor const &t,std::string name):theTest(t),name_(name){}
	
	void operator()()const{ theTest(); }

	std::string name()const{ return name_;}
	
	// this shouldn't belong here, but where?
	// needs adaptation for different compilers
	// dependency to demangle is a given, 
	// otherwise we have to use macros everywhere
	static std::string demangle(char const *name);
private:
	boost::function<void()> theTest;
	std::string name_;
};
}
#endif /*CUTE_TEST_H_*/
