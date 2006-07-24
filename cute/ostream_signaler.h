#ifndef STREAM_SIGNALER_H_
#define STREAM_SIGNALER_H_
#include "cute_signaler.h"
#include <iosfwd>
// a "root" signaler displaying output
class ostream_signaler
{
	std::ostream &out;
public:
	ostream_signaler(); // use cerr!
	ostream_signaler(std::ostream &os):out(os) {}
	void start(test const &t);
	void success(test const &t, char const *msg);
	void failure(test const &t,cute_exception const &e);
	void error(test const &t, char const *what);
};

#endif /*STREAM_SIGNALER_H_*/
