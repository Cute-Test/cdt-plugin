#include "cute.h"

#include <sstream>

namespace cute {
using namespace std;
std::string cute_exception::what()const{
		ostringstream out;
		out << filename << ":" << lineno << ": " << reason ;
		return out.str();
}	
}
