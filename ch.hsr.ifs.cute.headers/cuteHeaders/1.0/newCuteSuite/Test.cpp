#include "cute.h"
#include "ide_listener.h"
#include "cute_runner.h"
#include "$suitename$.h"


void runTest(){
	cute::ide_listener lis;
	cute::suite s=make_suite_$suitename$();
	cute::makeRunner(lis)(s, "The Suite");
}

int main(){
    runTest();
}



