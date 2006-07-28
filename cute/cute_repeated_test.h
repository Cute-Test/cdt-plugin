#ifndef CUTE_REPEATED_TEST_H_
#define CUTE_REPEATED_TEST_H_
#include "cute_test.h"
namespace cute{

struct repeated_test {
	repeated_test(test const &t,int n):theTest(t),repetitions(n){}
	void operator()(){
		for (int i=0;i<repetitions;++i){
			theTest();
		}
	}
	test theTest;
	const int repetitions;
};
}
#define CUTE_REPEAT(aTest,n) cute::test(cute::repeated_test(aTest,(n)),#aTest " " #n " times repeated")
#define CUTE_REPEAT_TEST(aTest,n) cute::test(cute::repeated_test(aTest,(n)),aTest.name()+" " #n " times repeated")
#endif /*CUTE_REPEATED_TEST_H_*/
