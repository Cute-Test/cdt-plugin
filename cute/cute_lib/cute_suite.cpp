#include "cute_suite.h"
namespace cute {
suite &operator+=(suite &left, suite const &right){
	left.insert(left.end(),right.begin(),right.end());
	return left;
}
suite &operator+=(suite &left, test const &right){
	left.push_back(right);
	return left;
}
}
