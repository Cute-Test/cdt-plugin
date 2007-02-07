#include "test_cute_equals.h"
#include "cute_base.h"
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
	ASSERT_EQUAL(42,fourtytwo);
	ASSERT_EQUAL(42UL,fourtytwoL);
	ASSERT_EQUAL(42,fourtytwoL);
	ASSERT_EQUAL(42UL,fourtytwo);
	ASSERT_EQUAL('\042',fourtytwoC);
	ASSERT_EQUAL(042,fourtytwoC);
	char const * f42s = "42";
	ASSERT_EQUAL("42",f42s);
	ASSERT_EQUAL("42",std::string(f42s));
	ASSERT_EQUAL(std::string("42"),f42s);
	ASSERT_EQUAL(std::string("42"),std::string(f42s));
}

void test_assertEqualsDelta() {
	ASSERT_EQUAL_DELTA(42,45,5); // roughly the same...
}
void test_equals_int_fails() {
	try {
		ASSERT_EQUAL(42,43);
		throw "should have failed"; // make this another error!
	} catch(cute::test_failure &){
	}
}
const double eps=std::numeric_limits<double>::epsilon();
void test_equals_double_fails() {
	try {
		ASSERT_EQUAL(1.0,1.0+11*eps);
		throw "should have failed"; // make this another error!
	} catch(cute::test_failure &){
	}
}
void test_equals_double(){
	ASSERT_EQUAL(0.0,0.0);
	ASSERT_EQUAL(1.0,1.0+eps);
	ASSERT_EQUAL(-1.0,-1.0+eps);
	ASSERT_EQUAL_DELTA(1e3,1001.0,1.0);
	ASSERT_EQUAL(10e14,10e14+1);
}
void test_equals_strings_fails(){
	try {
		ASSERT_EQUAL("error",std::string("eror"));
		throw "should have failed";
	}catch(cute::test_failure &e){
		ASSERT_EQUAL_DELTA(__LINE__,e.lineno,5);
		ASSERT_EQUAL(__FILE__,e.filename);
	}
}
void test_diff_values(){
	ASSERT_EQUAL(" expected:\t1\tbut was:\t2\t",cute::diff_values(1,2));
	ASSERT_EQUAL(" expected:\t" "error\\n" "\tbut was:\t" "eror\\t\t",cute::diff_values("error\n",std::string("eror\t")));
}
void test_backslashQuoteTabNewline(){
	std::string in("Hallo");
	ASSERT(in == cute::backslashQuoteTabNewline(in));
	std::string shouldQuote("Hi\nPeter\\tab\t ");
	std::string shouldQuoteQuoted("Hi\\nPeter\\\\tab\\t ");
	ASSERT(shouldQuoteQuoted == cute::backslashQuoteTabNewline(shouldQuote));
}

} // namespace
cute::suite test_cute_equals(){
	cute::suite s;
	s += CUTE(test_backslashQuoteTabNewline); 
	s += CUTE(test_equals_OK);
	s += CUTE(test_equals_int_fails);
	s += CUTE(test_assertEqualsDelta);
	s += CUTE(test_equals_double);
	s += CUTE(test_equals_double_fails);
	s += CUTE(test_equals_strings_fails);
	s += CUTE(test_diff_values);
	return s;
}
