#ifndef ECLIPSE_LISTENER_H_
#define ECLIPSE_LISTENER_H_
#include "cute_listener.h"
#include <iostream>
namespace cute {

	class eclipse_listener
	{
	public:
		eclipse_listener(){}
		
		void begin(suite const &t,char const *info){
			std::cout << "#beginning:" << info << ":" << t.size() << std::endl;
		}
		void end(suite const &t, char const *info){
			std::cout << "#ending:" << info << std::endl;
		}
		void success(test const &t, char const *msg){
			std::cout << "#success:" <<  t.name() <<":" << msg<< std::endl;
		}
		void failure(test const &t,test_failure const &e){
			std::cout << "#failure:" << e.filename << ":" << e.lineno << ":" <<e.reason << ":" << t.name()<< std::endl;
		}
		void error(test const &t, char const *what){
			std::cout << "#error:" << what << ":" << t.name() << std::endl;
		}
		void start(test const &t){
			std::cout << "#starting:" << t.name() << std::endl;
		}
	};
}

#endif /*ECLIPSE_LISTENER_H_*/
