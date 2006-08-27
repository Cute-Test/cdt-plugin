#include "ostream_listener.h"
#include "cute.h"
#include <iostream>
namespace cute {
ostream_listener::ostream_listener()
:out(std::cerr){
}
void ostream_listener::begin(suite const &s, char const *info){
	out << "beginning: " << info<<std::endl;
}
void ostream_listener::end(suite const &s, char const *info){
	out << "ending: " << info<<std::endl;
}
void ostream_listener::start(test const &t){
	out << "starting: " <<t.name()<< std::endl;
}
void ostream_listener::success(test const &t,char const *msg){
		out <<  t.name() <<" " << msg<< std::endl;
}
void ostream_listener::failure(test const &t, cute_exception const &e){
		out << e.filename << ":" << e.lineno << ": testcase failed: " <<e.reason << " in " << t.name()<< std::endl;
}
void ostream_listener::error(test const &t, char const * what){
		out << what << " in " << t.name() << std::endl;
}
}
