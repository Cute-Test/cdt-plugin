#include "cute_suite_test.h"
#include <boost/bind.hpp>
namespace cute {
void suite_test::operator()(){
	std::for_each(theSuite.begin(),theSuite.end(),boost::bind(&test::operator(),_1));
}
	
}
