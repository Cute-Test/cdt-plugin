#include "test_repeated_test.h"
#include "cute_repeated_test.h"
#include "cute.h"
#include "cute_equals.h"
using namespace cute;
namespace {
struct TestRepetition {
	int n;
	TestRepetition(){ n = 0;}
	void operator()(){ ++n; }
};
void repeat_test(){
	TestRepetition toRepeat;
	CUTE_REPEAT(boost::ref(toRepeat),5)();
	assertEquals(5,toRepeat.n);
}
void would_fail_if_run(){
	t_fail();
}
void repeat_0_test(){
	CUTE_REPEAT(CUTE(would_fail_if_run),0)();
}
}

suite test_repeated_test(){
	suite s;
	s += CUTE(repeat_test);
	s += CUTE(repeat_0_test);
	return s;
}
