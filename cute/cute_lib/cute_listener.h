#ifndef CUTE_SIGNALER_H_
#define CUTE_SIGNALER_H_
#include "cute.h"
#include "cute_test.h"
#include "cute_suite.h"
namespace cute {
struct null_listener{ // defines Contract of runner parameter
	void begin(suite const &s, char const *info){}
	void end(suite const &s, char const *info){}
	void start(test const &t){}
	void success(test const &t,char const *msg){}
	void failure(test const &t,cute_exception const &e){}
	void error(test const &t,char const *what){}
};
}
#endif