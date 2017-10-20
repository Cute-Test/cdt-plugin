/*********************************************************************************
 * This file is part of CUTE.
 *
 * Copyright (c) 2007-2017 Peter Sommerlad, IFS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *********************************************************************************/

#ifndef ECLIPSE_LISTENER_H_
#define ECLIPSE_LISTENER_H_
#include "ostream_listener.h"
#include <iostream>
#include <iterator>
#include <algorithm>
namespace cute {

	class eclipse_listener
	{
	protected:
		struct blankToUnderscore{
            char operator()(char in){
			if (in == ' ') return '_';
			return in;
		}
        };
		std::string maskBlanks(const std::string &in) {
			std::string result;
			std::transform(in.begin(),in.end(),std::back_inserter(result),blankToUnderscore());
			return result;
		}
	public:
		eclipse_listener() {}
		void start(test const &t){
			std::cout << std::endl << "#starting " <<t.name() << std::endl;
		}

		void begin(suite const &t,char const *info){
			std::cout << std::dec << std::endl << "#beginning " << info << " " << t.size() << std::endl;
		}
		void end(suite const &t, char const *info){
			std::cout << std::endl << "#ending " << info << std::endl;
		}
		void success(test const &t, char const *msg){
			std::cout << std::endl << "#success " << maskBlanks(t.name()) << " " << msg << std::endl;
		}
		void failure(test const &t,test_failure const &e){
			std::cout << std::dec << std::endl << "#failure " << maskBlanks(t.name()) << " " << e.filename << ":" << e.lineno << " " <<e.reason << std::endl;
		}
		void error(test const &t, char const *what){
			std::cout << std::endl << "#error " << maskBlanks(t.name()) << " " << what << std::endl;
		}
	};
}
#endif /*ECLIPSE_LISTENER_H_*/
