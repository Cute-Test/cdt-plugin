#include "cute_runner.h"
#include "cute_listener.h"
#include "cute_suite.h"
#include "cute_base.h"
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
	std::vector<std::string> infomessages;
	std::vector<std::string>  errormessages;
	std::vector<std::string>  successmessages;
	mock_listener()
	:begincount(0),endcount(0),startcount(0)
	,successcount(0),failurecount(0),errorcount(0){}
	void begin(suite const &s,char const *info){
		++begincount;
		infomessages.push_back(info);
	}
	void end(suite const &s,char const *info){++endcount;}
	void start(test const &t){++startcount;}
	void success(test const &t, char const *ok){
		++successcount;
		successmessages.push_back(ok);
	}
	void failure(test const &t,test_failure const &e){++failurecount;}
	void error(test const &t,char const *what){
		++errorcount;
		errormessages.push_back(what);
	}
};

void test_success(){}
void test_failing(){ ASSERT(false);}
void test_error_cstr(){ throw "error";}
void test_error_exception() { throw std::exception();}
void test_error_string(){ throw std::string("error");}

}
void test_cute_runner(){
	cute::runner<mock_listener> run;
	suite s;
	s += CUTE(test_success);
	ASSERT(run(s,"single success test suite"));
	s += CUTE(test_failing);
	s += CUTE(test_error_cstr);
	s += CUTE(test_error_string);
	s += CUTE(test_error_exception);
	bool result=run(s,"test_cute_runner_suite");
	ASSERT(!result); 
	ASSERT_EQUAL(2,run.begincount);
	ASSERT_EQUAL(2,run.endcount);
	ASSERT_EQUAL(2,run.successcount);
	ASSERT_EQUAL(1,run.failurecount);
	ASSERT_EQUAL(3,run.errorcount);
	ASSERT_EQUAL(2,run.infomessages.size());
	ASSERT_EQUAL("single success test suite",run.infomessages[0]);
	ASSERT_EQUAL("test_cute_runner_suite",run.infomessages[1]);
	ASSERT_EQUAL(3u,run.errormessages.size());
	ASSERT_EQUAL("error",run.errormessages[0]);
	ASSERT_EQUAL("error",run.errormessages[1]);
	std::string errormsg2=run.errormessages[2];
	std::string errmsgexpected="exception";
	ASSERT_EQUAL(errmsgexpected,errormsg2.substr(errormsg2.size()-errmsgexpected.size()));
	ASSERT_EQUAL(2u,run.successmessages.size());
	ASSERT_EQUAL("OK",run.successmessages[0]);
	
}
