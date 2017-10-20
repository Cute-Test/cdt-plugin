#include "cute.h"
#include "ide_listener.h"
#include "cute_runner.h"

void thisIsATest() {
	ASSERTM("start writing tests", false);	
}

bool runAllTests() {
	cute::suite s { };
	//TODO add your test here
	s.push_back(CUTE(thisIsATest));
	cute::ide_listener lis { };
	auto runner = cute::makeRunner(lis);
	bool success = runner(s, "The Suite");
	return success;
}

int main() {
    return runAllTests() ? EXIT_SUCCESS : EXIT_FAILURE;
}
