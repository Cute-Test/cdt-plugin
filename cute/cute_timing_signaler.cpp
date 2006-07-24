#include "cute_timing_signaler.h"
#include <time.h>
#include <sstream>
void timing_signaler_helper::start(){
	laststart= clock();
}
long long  timing_signaler_helper::stop()
{
	return clock()-laststart;
}
std::string  timing_signaler_helper::timingMessage(char const *msg,long long time){
			std::ostringstream os;
	os << msg << " took " << time << " ms";
	return os.str();
}

