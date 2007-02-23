#include "cute.h"
#include "eclipse_listener.h"
#include "cute_runner.h"


void runSuite(){
	cute::suite s;
	//TODO add your test here
	
	cute::eclipse_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}

int main(){
    runSuite();
}



