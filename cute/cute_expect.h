#ifndef CUTE_EXPECT_H_
#define CUTE_EXPECT_H_
#include "cute.h"
#include "cute_test.h"
namespace cute{
template <typename EXCEPTION>
struct cute_expect{
	test theTest;
	char const *filename;
	int  lineno;
	cute_expect(test const &t,char const *file,int line)
	:theTest(t), filename(file), lineno(line){}
	void operator()(){
		try{
			theTest();
			throw cute_exception(what(),filename,lineno);
		} catch(EXCEPTION &e) {
		}
	}
	std::string name() const { return theTest.name();}
	std::string what() const{
		return theTest.name() + " expecting " 
		       + test::demangle(typeid(EXCEPTION).name());
	}
};
}
#define CUTE_EXPECT(tt,exc) cute::cute_expect<exc>(tt,__FILE__,__LINE__)

#endif /*CUTE_EXPECT_H_*/
