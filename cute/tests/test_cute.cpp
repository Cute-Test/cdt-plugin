#include "test_cute.h"
#include "cute.h"
#include "cute_equals.h"

namespace {
	void test_fail_macro(){
		try {
			t_fail();
			throw "should have failed";
		} catch (cute::cute_exception &e){
			assertEquals(__FILE__,e.filename);
			std::string what=e.what();
			std::string fail("fail()");
			assertEquals(fail,e.reason);
			assertEquals(fail,what.substr(what.size()-fail.size()));
			assertEqualsDelta(__LINE__,e.lineno,10);
		}
	}
	void test_t_assert_macro(){
		try {
			t_assert(0);
			throw "should have failed";
		} catch (cute::cute_exception &e){
			assertEquals(__FILE__,e.filename);
			std::string what=e.what();
			std::string msg("0");
			assertEquals(msg,e.reason);
			assertEquals(msg,what.substr(what.size()-msg.size()));
			assertEqualsDelta(__LINE__,e.lineno,10);
		}
	}
	// TODO: check if the error-message format is useful and reasonable
	// might be too "elaborate" with "testcase failed:"
	void test_what(){
		cute::cute_exception ex("foo","file",42);
		assertEquals("file:42: testcase failed: foo",ex.what());
	}
}


cute::suite test_cute(){
	cute::suite s;
	s += CUTE(test_what);
	s += CUTE(test_fail_macro);
	s += CUTE(test_t_assert_macro);
	return s;
	
}
