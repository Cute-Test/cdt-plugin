#ifndef CUTE_RUNNER_H_
#define CUTE_RUNNER_H_
#include "cute_test.h"
#include "cute_suite.h"
#include "cute_signaler.h"
template <typename Signaler=null_signaler>
struct runner : Signaler{
	runner():Signaler(){}
	runner(Signaler &s):Signaler(s){}
	void operator()(test &t){
		runit(t);
	}
	void operator()(suite &s){
		for(suite::iterator it=s.begin();
		    it != s.end();
		    ++it){
		    	this->runit(*it);
		    }
		// avoid bind dependency: std::for_each(s.begin(),s.end(),boost::bind(&runner::runit,this,_1));
	}
private:
	void runit(test &t){
		try {
			Signaler::start(t);
			t();
			Signaler::success(t,"OK");
		} catch (cute_exception const &e){
			Signaler::failure(t,e);
		} catch (std::exception const &exc){
			Signaler::error(t,test::demangle(exc.what()).c_str());
		} catch (std::string &s){
			Signaler::error(t,s.c_str());
		} catch (char const *&cs) {
			Signaler::error(t,cs);
		} catch(...) {
			Signaler::error(t,"unknown exception thrown");
		}
	}
};
template <typename Signaler>
runner<Signaler> makeRunner(Signaler &s){
	return runner<Signaler>(s);
}

#endif /*CUTE_RUNNER_H_*/
