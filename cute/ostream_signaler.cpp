#include "ostream_signaler.h"
#include "cute.h"
#include <iostream>
ostream_signaler::ostream_signaler()
:out(std::cerr){
}
void ostream_signaler::start(test const &t){
}
// TODO: signal also success?
void ostream_signaler::success(test const &t, char const *msg){
		out <<  t.name() <<" "<< msg<< std::endl;

}
void ostream_signaler::failure(test const &t, cute_exception const &e){
		out << e.what() << " in " << t.name()<< std::endl;
}
void ostream_signaler::error(test const &t, char const * what){
		out << what << " in " << t.name() << std::endl;
}

