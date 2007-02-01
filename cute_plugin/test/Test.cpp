#include "cute.h"
#include "eclipse_listener.h"
#include "cute_runner.h"


void runSuite(){
	cute::suite s;
	//TODO add your test here
	
	cute::eclipse_listener lis;
	cute::runner<cute::eclipse_listener> run = cute::makeRunner(lis);
	run(s, "The Suite");
}

int main(){
	runSuite();
}



