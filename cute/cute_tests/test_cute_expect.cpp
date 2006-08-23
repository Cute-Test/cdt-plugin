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
}
cute::suite test_cute_expect() {
	cute::suite s;
	cute::test fails=CUTE_EXPECT(CUTE(no_exception), std::exception);
	s += CUTE_EXPECT(fails,cute::cute_exception);
	s += CUTE_EXPECT(CUTE(throws_std_exception),std::exception);
	return s; 
}

