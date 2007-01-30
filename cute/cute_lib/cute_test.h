#ifndef CUTE_TEST_H_
#define CUTE_TEST_H_
#include <boost/function.hpp>
// make plain functions as tests more 'cute':
namespace cute {
	typedef void (*VoidFunction)();
	struct test{
		void operator()()const{ theTest(); }
		std::string name()const{ return name_;}
		
		// this shouldn't belong here, but where?
		// needs adaptation for different compilers
		// dependency to demangle is a given, 
		// otherwise we have to use macros everywhere
		static std::string demangle(char const *name);
	
		// (real) functor types can (almost) spell their name
		// but a name can also be given explicitely, e.g. for CUTE()
		template <typename VoidFunctor>
		test(VoidFunctor const &t, std::string name = demangle(typeid(VoidFunctor).name()))
		:theTest(t),name_(name){}
#ifndef __GNUG__ /* might no longer be needed after fixed CUTE macro */
#if !defined(__GNUG__) || (__GNUG__ >= 4) 
        /* overload for dumber MSVC that won't deduce above ctor when using CUTE() macro: */
        /* and also for newer version 4 GNU compilers that seem to share that deficiency more silently */
		test(VoidFunction const &t, std::string const &name):theTest(t),name_(name){}
#endif	
	private:
		boost::function<void()> theTest;
		std::string name_;
	};
#define CUTE(name) cute::test((&name),(#name))
	
	// TODO: provide platform independent means of demangling, 
	// or at least support for different compilers
	// this is platform dependant for gnu compilers
#ifdef __GNUG__
#include <cxxabi.h> // __cxa_demangle
	inline std::string test::demangle(char const *name){
		char *toBeFreed = __cxxabiv1::__cxa_demangle(name,0,0,0);
		std::string result(toBeFreed);
		::free(toBeFreed);
		return result;
	}
	
#else
	// this default works reasonably with MSVC71 and 8
	inline std::string test::demangle(char const *name){
		return std::string(name);
	}
#endif
	
}
#endif /*CUTE_TEST_H_*/
