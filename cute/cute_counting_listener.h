#ifndef CUTE_COUNTING_SIGNALER_H_
#define CUTE_COUNTING_SIGNALER_H_
#include "cute_listener.h"
namespace cute{
// a listener that can wrap others, like ostream_listener
template <typename Listener=null_listener>
struct counting_listener:Listener{
	counting_listener()
	:Listener()
	,numberOfTests(0),successfulTests(0),failedTests(0),errors(0){}
	counting_listener(Listener const &s)
	:Listener(s)
	,numberOfTests(0),successfulTests(0),failedTests(0),errors(0){}
	void start(test const &t){
		Listener::start(t);
		++numberOfTests;
	}
	void success(test const &t,const char *msg){
		Listener::success(t,msg);
		++successfulTests;
	}
	void failure(test const &t,cute_exception const &e){
		Listener::failure(t,e);
		++failedTests;
	}
	void error(test const &t,char const *what){
		Listener::error(t,what);
		++errors;
	}
	int numberOfTests;
	int successfulTests;
	int failedTests;
	int errors;
};
}
#endif /*CUTE_COUNTING_SIGNALER_H_*/
