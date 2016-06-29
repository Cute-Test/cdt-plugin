#include "cute.h"
#include "ide_listener.h"
#include "cute_runner.h"
#include "$suitename$.h"

bool runSuite() {
	cute::ide_listener lis { };
	auto runner { cute::makeRunner(lis) };
	cute::suite s { make_suite_$suitename$() };
	bool success = runner(s, "$suitename$");
	return success;
}

int main() {
    return runSuite() ? EXIT_SUCCESS : EXIT_FAILURE;
}
