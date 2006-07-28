#include "cute_test.h"
// TODO: provide platform independent means of demangling, 
// or at least support for different compilers
// this is platform dependant for gnu compilers
#ifdef __GNUG__
#include <cxxabi.h> // __cxa_demangle
namespace cute {
std::string test::demangle(char const *name){
	char *toBeFreed = __cxxabiv1::__cxa_demangle(name,0,0,0);
	std::string result(toBeFreed);
	::free(toBeFreed);
	return result;
}
}
#else
namespace cute {
std::string test::demangle(char const *name){
	return std::string(name);
}
}
#endif
