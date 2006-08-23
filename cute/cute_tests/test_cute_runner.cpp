#include "cute_runner.h"
#include "cute_listener.h"
#include "cute_suite.h"
#include "cute.h"
#include "cute_equals.h"
using namespace cute;
namespace {
struct mock_listener {
	int begincount;
	int endcount;
	int startcount;
	int successcount;
	int failurecount;
	int errorcount;
	std::vector<std::string>  errormessages;
	std::vector<std::string>  successmessages;
	mock_listener()
	:begincount(0),endcount(0),startcount(0)
	,successcount(0),failurecount(0),errorcount(0){}
	void begin(suite const &s){++begincount;}
	void end(suite const &s){++endcount;}
	void start(test const &t){++startcount;}
	void success(test const &t, char const *ok){
		++successcount;
		successmessages.push_back(ok);
	}
	void failure(test const &t,cute_exception const &e){++failurecount;}
	void error(test const &t,char const *what){
		++errorcount;
		errormessages.push_back(what);
	}
};

void test_success(){}
void test_failure(){ t_assert(false);}
void test_error_cstr(){ throw "error";}
void test_error_exception() { throw std::exception();}
void test_error_string(){ throw std::string("error");}

}
void test_cute_runner(){
	cute::runner<mock_listener> run;
	suite s;
	s += CUTE(test_success);
	t_assert(run(s));
	s += CUTE(test_failure);
	s += CUTE(test_error_cstr);
	s += CUTE(test_error_string);
	s += CUTE(test_error_exception);
	bool result=run(s);
	t_assert(!result);
	assertEquals(2,run.begincount);
	assertEquals(2,run.endcount);
	assertEquals(2,run.successcount);
	assertEquals(1,run.failurecount);
	assertEquals(3,run.errorcount);
	assertEquals(3u,run.errormessages.size());
	assertEquals("error",run.errormessages[0]);
	assertEquals("error",run.errormessages[1]);
	std::string errormsg2=run.errormessages[2];
	std::string errmsgexpected="exception";
	assertEquals(errmsgexpected,errormsg2.substr(errormsg2.size()-errmsgexpected.size()));
	assertEquals(2u,run.successmessages.size());
	assertEquals("OK",run.successmessages[0]);
	
}
