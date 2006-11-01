#include "cute.h"

int lifeTheUniverseAndEverything = 41;

void mysimpletest(){
    ASSERT(lifeTheUniverseAndEverything == 6*7);
}
#include <iostream>
#include "cute_runner.h"
int main1(){
	using namespace std;

	if (cute::runner<>()(mysimpletest)){
		cout << "OK" << endl;
	} else {
		cout << "failed" << endl;
	}	
	return 0;
}

#include "ostream_listener.h"
int main2(){
	using namespace std;

	return cute::runner<cute::ostream_listener>()(mysimpletest);
}


#include "cute_test.h"
#include "cute_equals.h"
int anothertest(){
	ASSERT_EQUAL(42,lifeTheUniverseAndEverything);
	return 0;
}

cute::test tests[]={
	CUTE(mysimpletest)
	,mysimpletest
	,CUTE(anothertest)
};

struct ATestFunctor {
	void operator()(){
		ASSERT_EQUAL_DELTA(42.0,static_cast<double>(lifeTheUniverseAndEverything),0.001);
	}
};
#include "cute_suite.h"
int main3(){
	using namespace std;

	cute::runner<cute::ostream_listener> run;
	cute::suite s(tests,tests+(sizeof(tests)/sizeof(tests[0])));
	s+=ATestFunctor();
	return run(s,"suite");
}

int main(){
	main1();
	main2();
	main3();
}
