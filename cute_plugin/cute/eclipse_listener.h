#ifndef ECLIPSE_LISTENER_H_
#define ECLIPSE_LISTENER_H_
#include "ostream_listener.h"
#include <iostream>
namespace cute {

	class eclipse_listener 
	{
	public:
		eclipse_listener() {}
		void start(test const &t){
			std::cout << "#starting\x1F" <<t.name()<< std::endl;
		}
		
		void begin(suite const &t,char const *info){
			std::cout << "#beginning\x1F" << info << "\x1F" << t.size() << std::endl;
		}
		void end(suite const &t, char const *info){
			std::cout << "#ending\x1F" << info << std::endl;
		}
		void success(test const &t, char const *msg){
			std::cout << "#success\x1F" <<  t.name() <<"\x1F" << msg<< std::endl;
		}
		void failure(test const &t,test_failure const &e){
			std::cout << "#failure\x1F" << e.filename << "\x1F" << e.lineno << "\x1F" <<e.reason << "§" << t.name()<< std::endl;
		}
		void error(test const &t, char const *what){
			std::cout << "#error\x1F" << what << "\x1F" << t.name() << std::endl;
		}
	};
}
#endif /*ECLIPSE_LISTENER_H_*/
