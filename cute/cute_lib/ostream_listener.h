#ifndef STREAM_SIGNALER_H_
#define STREAM_SIGNALER_H_
#include "cute_listener.h"
#include <iostream>
namespace cute {
// a "root" listener displaying output
class ostream_listener
{
	std::ostream &out;
public:
	ostream_listener():out(std::cerr){}
	ostream_listener(std::ostream &os):out(os) {} 
	void begin(suite const &t,char const *info){
		out << "beginning: " << info<<std::endl;
	}
	void end(suite const &t, char const *info){
		out << "ending: " << info<<std::endl;
	}
	void start(test const &t){
		out << "starting: " <<t.name()<< std::endl;
	}
	void success(test const &t, char const *msg){
		out <<  t.name() <<" " << msg<< std::endl;
	}
	void failure(test const &t,cute_exception const &e){
		out << e.filename << ":" << e.lineno << ": testcase failed: " <<e.reason << " in " << t.name()<< std::endl;
	}
	void error(test const &t, char const *what){
		out << what << " in " << t.name() << std::endl;
	}
};
}
#endif /*STREAM_SIGNALER_H_*/
