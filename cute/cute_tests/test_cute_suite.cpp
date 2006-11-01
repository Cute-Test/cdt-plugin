#include "test_cute_suite.h"
#include "cute_suite.h"
#include "cute_equals.h"

namespace {
	void atest(){}
}
void test_cute_suite(){
	cute::suite s;
	ASSERT_EQUAL(0u,s.size());
	s += CUTE(atest);
	s += CUTE(atest);
	ASSERT_EQUAL(2u,s.size());
	s += s;
	ASSERT_EQUAL(4u,s.size());
}
