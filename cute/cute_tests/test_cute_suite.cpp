#include "test_cute_suite.h"
#include "cute_suite.h"
#include "cute_equals.h"

namespace {
	void atest(){}
}
void test_cute_suite(){
	cute::suite s;
	assertEquals(0u,s.size());
	s += CUTE(atest);
	s += CUTE(atest);
	assertEquals(2u,s.size());
	s += s;
	assertEquals(4u,s.size());
}
