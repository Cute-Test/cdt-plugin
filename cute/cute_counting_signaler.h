#ifndef CUTE_COUNTING_SIGNALER_H_
#define CUTE_COUNTING_SIGNALER_H_
#include "cute_signaler.h"
// a signaler that can wrap others, like ostream_signaler
template <typename Signaler=null_signaler>
struct counting_signaler:Signaler{
	counting_signaler()
	:Signaler()
	,numberOfTests(0),successfulTests(0),failedTests(0),errors(0){}
	counting_signaler(Signaler const &s)
	:Signaler(s)
	,numberOfTests(0),successfulTests(0),failedTests(0),errors(0){}
	void start(test const &t){
		Signaler::start(t);
		++numberOfTests;
	}
	void success(test const &t,const char *msg){
		Signaler::success(t,msg);
		++successfulTests;
	}
	void failure(test const &t,cute_exception const &e){
		Signaler::failure(t,e);
		++failedTests;
	}
	void error(test const &t,char const *what){
		Signaler::error(t,what);
		++errors;
	}
	int numberOfTests;
	int successfulTests;
	int failedTests;
	int errors;
};
#endif /*CUTE_COUNTING_SIGNALER_H_*/
