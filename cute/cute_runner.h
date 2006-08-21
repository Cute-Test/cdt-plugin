#ifndef CUTE_RUNNER_H_
#define CUTE_RUNNER_H_
#include "cute_test.h"
#include "cute_suite.h"
#include "cute_listener.h"
namespace cute {
template <typename Listener=null_listener>
struct runner : Listener{
	runner():Listener(){}
	runner(Listener &s):Listener(s){}
	bool operator()(test &t){
		return runit(t);
	}
	bool operator()(suite &s){
		Listener::begin(s);
		bool result=true;
		for(suite::iterator it=s.begin();
		// avoid bind dependency: std::for_each(s.begin(),s.end(),boost::bind(&runner::runit,this,_1));
		    it != s.end();
		    ++it){
		    	result = this->runit(*it) && result;
		    }
		Listener::end(s);
		return result;
	}
private:
	bool runit(test &t){
		try {
			Listener::start(t);
			t();
			Listener::success(t,"OK");
			return true;
		} catch (cute_exception const &e){
			Listener::failure(t,e);
		} catch (std::exception const &exc){
			Listener::error(t,test::demangle(exc.what()).c_str());
		} catch (std::string &s){
			Listener::error(t,s.c_str());
		} catch (char const *&cs) {
			Listener::error(t,cs);
		} catch(...) {
			Listener::error(t,"unknown exception thrown");
		}
		return false;
	}
};
template <typename Listener>
runner<Listener> makeRunner(Listener &s){
	return runner<Listener>(s);
}
}
#endif /*CUTE_RUNNER_H_*/
