#include "Die.h"
#include <cstdlib>

int Die::roll() const {
	return rand() % 6 + 1;
}
