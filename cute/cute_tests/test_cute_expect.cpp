#include "test_cute_expect.h"
#include "cute_expect.h"
#include <exception>
using namespace cute;
namespace {
void no_exception() {
}

void throws_std_exception () {
	throw std::exception();
}
void test_throws() {
	ASSERT_THROWS( throws_std_exception() , std::exception);
}
void test_doesntthrow() {
	ASSERT_THROWS(1+1,std::exception);
}
void test_throws_with_code(){
	ASSERT_THROWS( throw std::string("oops"), std::string);
}
void test_throws_with_message() {
	ASSERT_THROWSM("oops",throws_std_exception(),std::exception);
}
}
cute::suite test_cute_expect() {
	cute::suite s;
	cute::test fails=CUTE_EXPECT(CUTE(no_exception), std::exception);
	s += CUTE_EXPECT(fails,cute::test_failure);
	s += CUTE_EXPECT(CUTE(throws_std_exception),std::exception);
	s += CUTE(test_throws);
	s += CUTE_EXPECT(CUTE(test_doesntthrow),cute::test_failure);
	s += CUTE(test_throws_with_code);
	s += CUTE(test_throws_with_message);
	return s; 
}

