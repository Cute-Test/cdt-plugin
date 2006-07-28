#include "test_cute_suite_test.h"

#include "cute_suite_test.h"
#include "cute.h"
#include "cute_equals.h"

namespace {
// dummy tests	
int counter=0;
void testOK(){ ++counter; }
void testFails() { --counter; t_fail();}
// tests:
void test_suite_OK(){
	cute::suite s;
	counter =0;
	s += CUTE(testOK);
	s += CUTE(testOK);
	s += CUTE(testOK);
	cute::test t = CUTE_SUITE_TEST(s);
	t();
	assertEquals(s.size(),counter);
}
void test_suite_fails(){
	cute::suite suite_that_fails;
	counter =0;
	suite_that_fails += CUTE(testOK); // 1
	suite_that_fails += CUTE(testOK); // 2
	suite_that_fails += CUTE(testFails); // 1
	suite_that_fails += CUTE(testFails); // 0 -> should not be reached
	cute::test t = CUTE_SUITE_TEST(suite_that_fails);
	try {
		t();
		throw "should have failed";
	} catch (cute::cute_exception &e){
		assertEquals(1,counter);
	}
}
}
cute::suite test_cute_suite_test(){
	cute::suite s;
	s += CUTE(test_suite_OK);
	s += CUTE(test_suite_fails);
	return s;
}
