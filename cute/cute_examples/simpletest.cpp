#include "cute.h"

int lifeTheUniverseAndEverything = 41;

void mysimpletest(){
    t_assert(lifeTheUniverseAndEverything == 6*7);
}

#include <iostream>
#include "cute_runner.h"
#include "ostream_listener.h"
int main(){
	using namespace std;

	cute::runner<cute::ostream_listener>()(CUTE(mysimpletest));
}
