#include "test_cute_equals.h"
#include "cute.h"
#include "cute_equals.h"
#include "cute_expect.h"
#include <limits>
namespace {
// the following code triggers some warnings about equating signed/unsigned
// that is intentional
void test_equals_OK() {
	int fourtytwo = 42;
	unsigned long fourtytwoL = 42UL;
	char fourtytwoC = '\042';
	assertEquals(42,fourtytwo);
	assertEquals(42UL,fourtytwoL);
	assertEquals(42,fourtytwoL);
	assertEquals(42UL,fourtytwo);
	assertEquals('\042',fourtytwoC);
	assertEquals(042,fourtytwoC);
	char const * f42s = "42";
	assertEquals("42",f42s);
	assertEquals("42",std::string(f42s));
	assertEquals(std::string("42"),f42s);
	assertEquals(std::string("42"),std::string(f42s));
}

void test_assertEqualsDelta() {
	assertEqualsDelta(42,45,5); // roughly the same...
}
void test_equals_int_fails() {
	try {
		assertEquals(42,43);
		throw "should have failed"; // make this another error!
	} catch(cute::cute_exception &e){
	}
}
const double eps=std::numeric_limits<double>::epsilon();
void test_equals_double_fails() {
	try {
		assertEquals(1.0,1.0+11*eps);
		throw "should have failed"; // make this another error!
	} catch(cute::cute_exception &e){
	}
}
void test_equals_double(){
	assertEquals(0.0,0.0);
	assertEquals(1.0,1.0+eps);
	assertEquals(-1.0,-1.0+eps);
	assertEqualsDelta(1e3,1001.0,1.0);
	assertEquals(10e14,10e14+1);
}
void test_equals_strings_fails(){
	try {
		assertEquals("error",std::string("eror"));
		throw "should have failed";
	}catch(cute::cute_exception &e){
		assertEqualsDelta(__LINE__,e.lineno,5);
		assertEquals(__FILE__,e.filename);
	}
}
void test_diff_values(){
	assertEquals("(1,2)",cute::diff_values(1,2));
	assertEquals(" pos 2 (\"ror\",\"or\")",cute::diff_values("error",std::string("eror")));
}
} // namespace
cute::suite test_cute_equals(){
	cute::suite s;
	s += CUTE(test_equals_OK);
	s += CUTE(test_equals_int_fails);
	s += CUTE(test_assertEqualsDelta);
	s += CUTE(test_equals_double);
	s += CUTE(test_equals_double_fails);
	s += CUTE(test_equals_strings_fails);
	s += CUTE(test_diff_values);
	return s;
}
