#ifndef CUTE_SIGNALER_H_
#define CUTE_SIGNALER_H_
#include "cute.h"
#include "cute_test.h"
struct null_signaler{ // defines Contract of runner parameter
	void start(test const &t){}
	void success(test const &t, char const *){}
	void failure(test const &t,cute_exception const &e){}
	void error(test const &t,char const *what){}
};
#endif
