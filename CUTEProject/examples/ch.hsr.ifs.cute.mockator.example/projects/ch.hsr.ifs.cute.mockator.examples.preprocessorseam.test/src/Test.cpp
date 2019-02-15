#include "cute.h"
#include "ide_listener.h"
#include "cute_runner.h"
#include "leapyear.h"

void testLeapYear() {
	//1350770400 2012 is a leap year; date -d "Oct 21 2012" +%s
	ASSERT(isLeapYear());
}

void runSuite(){
	cute::suite s;
	s.push_back(CUTE(testLeapYear));
	cute::ide_listener<> lis;
	cute::makeRunner(lis)(s, "The Suite");
}

int main(){
    runSuite();
    return 0;
}
