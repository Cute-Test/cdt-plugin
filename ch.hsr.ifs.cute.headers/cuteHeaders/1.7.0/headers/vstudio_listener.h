/*********************************************************************************
 * This file is part of CUTE.
 *
 * Copyright (c) 2006-2017 Peter Sommerlad, IFS
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

#ifndef VSTUDIO_LISTENER_H
#define VSTUDIO_LISTENER_H
// Windows listener for debug mode: allows selection of assert failing source line
// TODO: vstudio_listener is broken for VS later than 2003, because OutputDebugString no longer works as before
#ifndef __GNUG__
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <sstream>
#include <iostream>
namespace cute{
	class vstudio_listener
	{
	public:
		void begin(suite const &t,char const *info){
		}
		void end(suite const &t, char const *info){
		}
		void start(test const &t){
		}
		void success(test const &t, char const *msg){
			std::cerr <<  t.name() <<" " << msg << std::endl;
		}
		void failure(test const &t,test_failure const &e){
			std::ostringstream out;
			out << std::dec << e.filename << "(" << e.lineno << ") : testcase failed: " << e.reason << " in " << t.name() << std::endl;
			OutputDebugString(out.str().c_str());
			std::cerr << out.str() << std::flush;
		}
		void error(test const &t, char const *what){
			std::ostringstream out;
			out << what << " in " << t.name() << std::endl;
			OutputDebugString(out.str().c_str());
			std::cerr << out.str() << std::flush;
		}
	};
}
#else
// cheat for gnu use ostream_listener instead, so the type is defined
#include "ostream_listener.h"
namespace cute{
	typedef cute::ostream_listener vstudio_listener;
}
#endif
#endif
