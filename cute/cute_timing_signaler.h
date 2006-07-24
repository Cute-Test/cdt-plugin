#ifndef CUTE_TIMING_SIGNALER_H_
#define CUTE_TIMING_SIGNALER_H_
#include "cute_signaler.h"
// a signaler that can wrap others, like ostream_signaler
// TODO: should have system dependent parts in cpp file.
struct timing_signaler_helper{
	long long laststart;
	void start();
	long long stop();
	std::string timingMessage(char const *msg,long long time);
};
template <typename Signaler=null_signaler>
struct timing_signaler:Signaler{
	timing_signaler()
	:Signaler()
	,currentTest(0),allTests(0){}
	timing_signaler(Signaler const &s)
	:Signaler(s)
	,currentTest(0),allTests(0){}
	void start(test const &t){
		Signaler::start(t);
		timer.start();
	}
	void success(test const &t, char const *msg){
		currentTest= timer.stop();
		allTests+=currentTest;
		Signaler::success(t,timer.timingMessage(msg,currentTest).c_str());
	}
	void failure(test const &t,cute_exception const &e){
		Signaler::failure(t,e);
	}
	void error(test const &t,char const *what){
		Signaler::error(t,what);
	}
	timing_signaler_helper timer;
	long long currentTest;
	long long allTests;
};
#endif /*CUTE_TIMING_SIGNALER_H_*/
