#ifndef CUTE_EQUALS_H_
#define CUTE_EQUALS_H_

#include "cute.h"
#include <sstream>
#include <cmath>
#include <limits>

namespace cute {
// overload the following for your purpose of presenting a difference
// TODO: should I provide overloads for EXPECTED == ACTUAL?
	template <typename EXPECTED, typename ACTUAL>
	std::string diff_values(EXPECTED const &expected
						,ACTUAL const & actual){
		// construct a simple message...
	std::ostringstream os;
	os << "(" << expected<<","<<actual<<")";
	return os.str();
	}
// special cases for strings
	inline	std::string diff_values(std::string const &exp,std::string const &act){
		typedef std::string::const_iterator iter;
		std::string const *expp = &exp;
		std::string const *actp = &act;
		if (exp.length() > act.length()) std::swap(expp,actp);
		std::pair<iter,iter> differ=mismatch(expp->begin(),expp->end(),actp->begin());
		std::ostringstream os;
		if (exp.length() > act.length()) std::swap(differ.first,differ.second);
		os << " pos " << std::distance(exp.begin(),differ.first) << " (\""
		   <<exp.substr(differ.first-exp.begin())<<"\",\""
		   <<act.substr(differ.second-act.begin())<<"\")";
		return os.str();
	}
	inline	std::string diff_values(char const * const &exp,std::string const &act){
		return diff_values(std::string(exp),act);
	}
	inline	std::string diff_values(char const * const &exp,char const *act){
		return diff_values(std::string(exp),std::string(act));
	}
//std::string diff_values(std::string const &,std::string const &);
//std::string diff_values(char const * const &exp,std::string const &act);

// TODO: some magic might be possible with boost::mpl... leave that for the moment
	template <typename EXPECTED, typename ACTUAL>
	void assert_equal(EXPECTED const &expected
				,ACTUAL const &actual
				,char const *msg
				,char const *file
				,int line) {
	if (expected == actual) return;
	throw cute_exception(msg + diff_values(expected,actual),file,line);
	}
	template <typename EXPECTED, typename ACTUAL, typename DELTA>
	void assert_equal_delta(EXPECTED const &expected
				,ACTUAL const &actual
				,DELTA const &delta
				,char const *msg
				,char const *file
				,int line) {
	if (std::abs(expected-actual)< std::abs(delta)) return;
	throw cute_exception(msg + diff_values(expected,actual),file,line);
	}
// TODO: provide this for float as well. (and combinations?)
	template <>
	inline
	void assert_equal<double,double>(double const &expected
				,double const &actual
				,char const *msg
				,char const *file
				,int line);
	template <>
	inline
	void assert_equal_delta<double,double,double>(double const &expected
				,double const &actual
				,double const &delta
				,char const *msg
				,char const *file
				,int line);

	template <>
	inline
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
	inline
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

#define ASSERT_EQUALM(msg,expected,actual) cute::assert_equal((expected),(actual),msg,__FILE__,__LINE__)
#define ASSERT_EQUAL(expected,actual) ASSERT_EQUALM(#expected " expected but was " #actual, expected,actual)
#define ASSERT_EQUAL_DELTAM(msg,expected,actual,delta) cute::assert_equal_delta((expected),(actual),(delta),msg,__FILE__,__LINE__)
#define ASSERT_EQUAL_DELTA(expected,actual,delta) ASSERT_EQUAL_DELTAM(#expected " expected with error " #delta " but was " #actual,expected,actual,delta)
#endif /*CUTE_EQUALS_H_*/
