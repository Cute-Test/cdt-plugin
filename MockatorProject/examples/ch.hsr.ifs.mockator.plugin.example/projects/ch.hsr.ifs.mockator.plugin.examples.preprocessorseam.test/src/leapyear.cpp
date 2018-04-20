#include "leapyear.h"
#include <ctime>

unsigned int thisYear() {
	time_t now = time(0);
	tm* z = localtime(&now);
	return z->tm_year + 1900;
}

bool isLeapYear() {
	unsigned int year = thisYear();

	if ((year % 400) == 0) {
		return true;
	}

	if ((year % 100) == 0) {
		return false;
	}

	if ((year % 4) == 0) {
		return true;
	}

	return false;
}
