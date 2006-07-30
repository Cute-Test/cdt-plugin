#include "cute_equals.h"
#include <cmath>
#include <limits>
namespace cute{
	std::string diff_values(std::string const &exp,std::string const &act){
		typedef std::string::const_iterator iter;
		std::pair<iter,iter> differ=mismatch(exp.begin(),exp.end(),act.begin());
		std::ostringstream os;
		os << " pos " << std::distance(exp.begin(),differ.first) << " (\""
		   <<exp.substr(differ.first-exp.begin())<<"\",\""
		   <<act.substr(differ.second-act.begin())<<"\")";
		return os.str();
	}
	std::string diff_values(char const * const &exp,std::string const &act){
		return diff_values(std::string(exp),act);
	}
	template <>
	void assert_equal<double,double>(double const &expected
				,double const &actual
				,char const *msg
				,char const *file
				,int line) {
	// allow for one digit error on equality, apps might need different...
	// and should use assert_equal_delta
	const double delta=10*std::numeric_limits<double>::epsilon()*expected;
	assert_equal_delta(expected,actual,delta,msg,file,line);
	}
	template <>
	void assert_equal_delta<double,double,double>(double const &expected
				,double const &actual
				,double const &delta
				,char const *msg
				,char const *file
				,int line) {
	if (std::abs(expected-actual) <= std::abs(delta) ) return;
	throw cute_exception(msg + diff_values(expected,actual),file,line);
}
	
}
