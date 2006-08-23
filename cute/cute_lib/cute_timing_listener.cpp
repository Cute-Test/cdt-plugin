#include "cute_timing_listener.h"
#include <time.h>
#include <sstream>
namespace cute {
void timing_listener_helper::start(){
	laststart= clock();
}
long long  timing_listener_helper::stop()
{
	return clock()-laststart;
}
std::string  timing_listener_helper::timingMessage(char const *msg,long long time){
			std::ostringstream os;
	os << msg << " took " << time << " ms";
	return os.str();
}
}
