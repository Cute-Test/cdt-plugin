#include "test_cute_test.h"
#include "cute_test.h"
#include "cute_equals.h"

namespace for_cute_equals_test {
struct Test{
	void operator()(){}
};
void aTestFunction(){
}	
}
namespace {
void test_cute_macro(){	
	using namespace for_cute_equals_test;
	cute::test t = CUTE(aTestFunction);
	assertEquals("aTestFunction",t.name());
}
void test_functor(){
	cute::test t = for_cute_equals_test::Test();
	assertEquals("for_cute_equals_test::Test",t.name()); 
}
}
cute::suite test_cute_test(){
	cute::suite s;
	s += CUTE(test_cute_macro);
	s += CUTE(test_functor);
	return s;
}
