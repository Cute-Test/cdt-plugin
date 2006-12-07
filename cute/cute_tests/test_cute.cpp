#include "test_cute.h"
#include "cute_base.h"
#include "cute_equals.h"

namespace {
	void test_fail_macro(){
		try {
			FAIL();
			throw "should have failed";
		} catch (cute::test_failure &e){
			ASSERT_EQUAL(__FILE__,e.filename);
			std::string what=e.what();
			std::string fail("FAIL()");
			ASSERT_EQUAL(fail,e.reason);
			ASSERT_EQUAL(fail,what.substr(what.size()-fail.size()));
			ASSERT_EQUAL_DELTA(__LINE__,e.lineno,10);
		}
	}
	void test_t_assert_macro(){
		try {
			ASSERT(0);
			throw "should have failed";
		} catch (cute::test_failure &e){
			ASSERT_EQUAL(__FILE__,e.filename);
			std::string what=e.what();
			std::string msg("0");
			ASSERT_EQUAL(msg,e.reason);
			ASSERT_EQUAL(msg,what.substr(what.size()-msg.size()));
			ASSERT_EQUAL_DELTA(__LINE__,e.lineno,10);
		}
	}
	void test_what(){
		cute::test_failure ex("foo","file",42);
		ASSERT_EQUAL(std::string("foo"),ex.what());
		ASSERT_EQUAL(42,ex.lineno);
		ASSERT_EQUAL("file",ex.filename);
	}
}


cute::suite test_cute(){
	cute::suite s;
	s += CUTE(test_what);
	s += CUTE(test_fail_macro);
	s += CUTE(test_t_assert_macro);
	return s;
	
}
