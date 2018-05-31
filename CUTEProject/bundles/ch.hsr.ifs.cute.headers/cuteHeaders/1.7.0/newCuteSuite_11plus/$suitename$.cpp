#include "$suitename$.h"
#include "cute.h"
#include "ide_listener.h"
#include "cute_runner.h"

void thisIsA$suitename$Test() {
	ASSERTM("start writing tests", false);	
}

cute::suite make_suite_$suitename$() {
	cute::suite s { };
	s.push_back(CUTE(thisIsA$suitename$Test));
	return s;
}
