#ifndef CUTE_EQUALS_H_
#define CUTE_EQUALS_H_

#include "cute.h"
#include <sstream>
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
std::string diff_values(std::string const &,std::string const &);
std::string diff_values(char const * const &exp,std::string const &act);
				
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
void assert_equal<double,double>(double const &expected
				,double const &actual
				,char const *msg
				,char const *file
				,int line);
template <>
void assert_equal_delta<double,double,double>(double const &expected
				,double const &actual
				,double const &delta
				,char const *msg
				,char const *file
				,int line);

}

#define assertEqualsm(msg,expected,actual) cute::assert_equal((expected),(actual),msg,__FILE__,__LINE__)
#define assertEquals(expected,actual) assertEqualsm(#expected " expected but was " #actual, expected,actual)
#define assertEqualsDeltam(msg,expected,actual,delta) cute::assert_equal_delta((expected),(actual),(delta),msg,__FILE__,__LINE__)
#define assertEqualsDelta(expected,actual,delta) assertEqualsDeltam(#expected " expected with error " #delta " but was " #actual,expected,actual,delta)
#endif /*CUTE_EQUALS_H_*/
