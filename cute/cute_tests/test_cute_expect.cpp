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
 * Copyright 2008 Peter Sommerlad
 *
 *********************************************************************************/

#include "test_cute_expect.h"
#include "cute_throws.h"
#include <exception>
using namespace cute;
namespace {
void no_exception() {
}

void throws_std_exception () {
	throw std::exception();
}
void test_throws() {
	ASSERT_THROWS( throws_std_exception() , std::exception);
}
void test_doesntthrow() {
	// the following code will issue a warning when compiled!
	ASSERT_THROWS(1+1,std::exception);
}
void test_throws_with_code(){
	ASSERT_THROWS( throw std::string("oops"), std::string);
}
void test_throws_with_message() {
	ASSERT_THROWSM("oops",throws_std_exception(),std::exception);
}
void test_throwing_with_demangle_failure() {
	throw std::logic_error("NOT A VALID TYPE NAME");
}
void test_fails_throws(){
	ASSERT_THROWS(CUTE(no_exception)(),std::exception);
}
void test_that_fails_throws_failure(){
	ASSERT_THROWS(CUTE(test_fails_throws)(),cute::test_failure);
}
void test_that_throws_throws(){
	ASSERT_THROWS(CUTE(throws_std_exception)(),std::exception);
}
void test_that_doesnt_throw_fails_when_expected_to_throws(){
	ASSERT_THROWS(CUTE(test_doesntthrow)(),cute::test_failure);
}
}
cute::suite test_cute_expect() {
	cute::suite s;
//	cute::test fails=CUTE_EXPECT(CUTE(no_exception), std::exception);
//	s += CUTE_EXPECT(fails,cute::test_failure);
	s += CUTE(test_that_fails_throws_failure);
//	s += CUTE_EXPECT(CUTE(throws_std_exception),std::exception);
	s += CUTE(test_that_throws_throws);
	s += CUTE(test_throws);
//	s += CUTE_EXPECT(CUTE(test_doesntthrow),cute::test_failure);
	s += CUTE(test_that_doesnt_throw_fails_when_expected_to_throws);
	s += CUTE(test_throws_with_code);
	s += CUTE(test_throws_with_message);
	s.push_back(CUTE(test_throwing_with_demangle_failure));
	return s;
}

