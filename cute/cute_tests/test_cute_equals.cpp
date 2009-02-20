/*********************************************************************************
 * This file is part of CUTE.
 *
 * CUTE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CUTE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CUTE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2007 Peter Sommerlad
 *
 *********************************************************************************/

#include "test_cute_equals.h"
#include "cute_base.h"
#include "cute_equals.h"
#include "cute_throws.h"
#include <limits>



void test_equals_OK() {
	int fourtytwo = 42;
	unsigned long fourtytwoL = 42UL;
	char fourtytwoC = '\042';
	ASSERT_EQUAL(42,fourtytwo);
	ASSERT_EQUAL(42UL,fourtytwoL);
	ASSERT_EQUAL(42u,fourtytwoL);
	ASSERT_EQUAL(42UL,fourtytwo);
	ASSERT_EQUAL('\042',fourtytwoC);
	ASSERT_EQUAL(042,fourtytwoC);
	char const * f42s = "42";
	ASSERT_EQUAL("42",f42s);
	ASSERT_EQUAL("42",std::string(f42s));
	ASSERT_EQUAL(std::string("42"),f42s);
	ASSERT_EQUAL(std::string("42"),std::string(f42s));
}

void test_assertEqualsDelta() {
	ASSERT_EQUAL_DELTA(42,45,5); // roughly the same...
}
void test_equals_int_fails() {
	try {
		ASSERT_EQUAL(42,43);
		throw "should have failed"; // make this another error!
	} catch(cute::test_failure &){
	}
}
const double eps=std::numeric_limits<double>::epsilon();
void test_equals_double_fails() {
	try {
		ASSERT_EQUAL(1.0,1.0+11*eps);
		throw "should have failed"; // make this another error!
	} catch(cute::test_failure &){
	}
}void test_equals_double_zero(){
	ASSERT_EQUAL(0.0,0.0);
}
void test_equals_double_one_one_plus_eps(){
		ASSERT_EQUAL(1.0,1.0+eps);
}
void test_equals_double_minusone_minusone_plus_eps(){
		ASSERT_EQUAL(-1.0,-1.0+eps);
}




void test_equals_double(){
	ASSERT_EQUAL_DELTA(1e3,1001.0,1.0);
	ASSERT_EQUAL(10e14,1+10e14);
}
void test_equals_float(){
	ASSERT_EQUAL(float(10e7),float(100+10e7)); // assume 6 digit "precision" delta
}
void test_equals_strings_fails(){
	try {
		ASSERT_EQUAL("error",std::string("eror"));
		throw "should have failed";
	}catch(cute::test_failure &e){
		ASSERT_EQUAL_DELTA(__LINE__,e.lineno,5);
		ASSERT_EQUAL(__FILE__,e.filename);
	}
}
void test_diff_values(){
	ASSERT_EQUAL(" expected:\t1\tbut was:\t2\t",cute::diff_values(1,2));
	ASSERT_EQUAL(" expected:\t" "error\\n" "\tbut was:\t" "eror\\t\t",cute::diff_values("error\n",std::string("eror\t")));
}
void test_backslashQuoteTabNewline(){
	std::string in("Hallo");
	ASSERT(in == cute::equals_impl::backslashQuoteTabNewline(in));
	std::string shouldQuote("Hi\nPeter\\tab\t ");
	std::string shouldQuoteQuoted("Hi\\nPeter\\\\tab\\t ");
	ASSERT(shouldQuoteQuoted == cute::equals_impl::backslashQuoteTabNewline(shouldQuote));
}

cute::suite test_cute_equals(){
	cute::suite s;
	s.push_back(CUTE(test_equals_float));
	s.push_back(CUTE(test_equals_double_zero));
	s.push_back(CUTE(test_equals_double_one_one_plus_eps));
	s.push_back(CUTE(test_equals_double_minusone_minusone_plus_eps));
	s += CUTE(test_backslashQuoteTabNewline);
	s += CUTE(test_equals_OK);
	s += CUTE(test_equals_int_fails);
	s += CUTE(test_assertEqualsDelta);
	s += CUTE(test_equals_double);
	s += CUTE(test_equals_double_fails);
	s += CUTE(test_equals_strings_fails);
	s += CUTE(test_diff_values);
	return s;
}
