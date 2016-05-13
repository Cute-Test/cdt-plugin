#include "cute.h"
#include "ide_listener.h"
#include "xml_listener.h"
#include "cute_runner.h"
#include "$suitename$.h"

bool runSuite(int argc, char const *argv[]) {
	cute::xml_file_opener xmlfile(argc, argv);
	cute::xml_listener<cute::ide_listener<>> lis(xmlfile.out);
	cute::suite s {make_suite_$suitename$()};
	const auto runner {cute::makeRunner(lis, argc, argv)};
	return runner(s, "$suitename$");
}

int main(int argc, char const *argv[]) {
    return runSuite(argc, argv) ? EXIT_SUCCESS : EXIT_FAILURE;
}
