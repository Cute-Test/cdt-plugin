#ifndef STREAM_SIGNALER_H_
#define STREAM_SIGNALER_H_
#include "cute_listener.h"
#include <iosfwd>
namespace cute {
// a "root" listener displaying output
class ostream_listener
{
	std::ostream &out;
public:
	ostream_listener(); // use cerr!
	ostream_listener(std::ostream &os):out(os) {} 
	void begin(suite const &t,char const *info);
	void end(suite const &t, char const *info);
	void start(test const &t);
	void success(test const &t, char const *msg);
	void failure(test const &t,cute_exception const &e);
	void error(test const &t, char const *what);
};
}
#endif /*STREAM_SIGNALER_H_*/
