#include "cute.h"

#include <sstream>

using namespace std;

std::string cute_exception::what()const{
		ostringstream out;
		out << filename << ":" << lineno << ": testcase failed: " << reason ;
		return out.str();
}	
