#include "cute.h"
#include "ide_listener.h"
#include "xml_listener.h"
#include "cute_runner.h"

void thisIsATest() {
	ASSERTM("start writing tests", false);	
}
void newTestFunction(){
	ASSERTM("start writing tests", false);
}

cute::suite asuite=cute::suite{
	{"first test",[](){ ASSERTM("a test lambda",true);}}
	,{"second test",[](){ ASSERTM("a test lambda",false);}}
};

void runAllTests(int argc, char const *argv[]){
	cute::suite s;
	//TODO add your test here
	s.push_back(CUTE(thisIsATest));
	s.push_back(CUTE(newTestFunction));
	cute::xml_file_opener xmlfile(argc, argv);
	cute::xml_listener<cute::ide_listener<> > lis(xmlfile.out);
	auto runner=cute::makeRunner(lis);
	runner(s, "AllTests");
	runner(asuite,"asuite_tests");
}

int main(int argc, char const *argv[]){
    runAllTests(argc,argv);
    return 0;
}



