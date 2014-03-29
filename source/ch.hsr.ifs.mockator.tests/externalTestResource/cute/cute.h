#ifndef CUTE_H_
#define CUTE_H_

#include "stl_fake.h"

namespace cute {
     struct test {
    	 template <typename VoidFunctor>
    	 test(VoidFunctor const &t, std::string sname) {}
     };
     typedef std::vector<test> suite;
	 template <typename ExpectedValue, typename ActualValue>
	 void assert_equal(ExpectedValue const &expected
				,ActualValue const &actual
				,char const *msg
				,char const *file
				,int line) {
	 }

	 struct test_failure {
		std::string reason;
		std::string filename;
		int lineno;

		test_failure(std::string const &r,char const *f, int line)
		:reason(r),filename(f),lineno(line)
		{ 	}
		char const * what() const { return reason.c_str(); }
	};
}

#define CUTE(name) cute::test((&name),(#name))
#define ASSERT_EQUALM(msg,expected,actual) cute::assert_equal((expected),(actual),msg,__FILE__,__LINE__)
#define ASSERT_EQUAL(expected,actual) ASSERT_EQUALM(#expected " == " #actual, (expected),(actual))
#define ASSERTM(msg,cond) if (!(cond)) throw cute::test_failure((msg),__FILE__,__LINE__)
#define ASSERT(cond) ASSERTM(#cond,cond)

#endif
