#ifndef CUTE_TIMING_SIGNALER_H_
#define CUTE_TIMING_SIGNALER_H_
#include "cute_listener.h"
// a listener that can wrap others, like ostream_listener
// TODO: should have system dependent parts in cpp file.
namespace cute {
struct timing_listener_helper{
	long long laststart;
	void start();
	long long stop();
	std::string timingMessage(char const *msg,long long time);
};
template <typename Listener=null_listener>
struct timing_listener:Listener{
	timing_listener()
	:Listener()
	,currentTest(0),allTests(0){}
	timing_listener(Listener const &s)
	:Listener(s)
	,currentTest(0),allTests(0){}
	void start(test const &t){
		Listener::start(t);
		timer.start();
	}
	void taketime(){
		currentTest= timer.stop();
		allTests+=currentTest;
	}
	void success(test const &t, char const *msg){
		taketime();
		Listener::success(t,timer.timingMessage(msg,currentTest).c_str());
	}
	void failure(test const &t,cute_exception const &e){
		taketime();
		Listener::failure(t,e);
	}
	void error(test const &t,char const *what){
		taketime();
		Listener::error(t,what);
	}
	timing_listener_helper timer;
	long long currentTest;
	long long allTests;
};
}
#endif /*CUTE_TIMING_SIGNALER_H_*/
