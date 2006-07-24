#ifndef CUTE_REPEATED_TEST_H_
#define CUTE_REPEATED_TEST_H_
#include "cute_test.h"
template <int N>
struct repeated_test {
	repeated_test(test const &t):theTest(t){}
	void operator(){
		for (int i=0;i<N;++i){
			theTest();
		}
	}
	test theTest;
};
#define CUTE_REPEAT(aTest,n) test(repeated_test<n>(aTest),#aTest " " #n " times repeated")
#define CUTE_REPEAT_TEST(aTest,n) test(repeated_test<n>(aTest),aTest.name()+" " #n " times repeated")
#endif /*CUTE_REPEATED_TEST_H_*/
