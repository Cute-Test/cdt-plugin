/*********************************************************************************
 * This file is part of CUTE.
 *
 * Copyright (c) 2009-2017 Peter Sommerlad, IFS
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

#ifndef CUTE_DEMANGLE_H_
#define CUTE_DEMANGLE_H_
#include <string>
// needs adaptation for different compilers
// dependency to demangle is a given,
// otherwise we have to use macros everywhere
#ifdef __GNUG__
#include <cxxabi.h> // __cxa_demangle
#include <cstdlib> // ::free() 
namespace cute {

inline std::string demangle(char const *name){
	if (!name) return "unknown";
	char *toBeFreed = abi::__cxa_demangle(name,0,0,0);
	std::string result(toBeFreed?toBeFreed:name);
	::free(toBeFreed);
	return result;
}
}
#else
namespace cute {
// this default works reasonably with MSVC71 and 8, hopefully for others as well
inline std::string demangle(char const *name){
	return std::string(name?name:"unknown");
}
}
#endif

#endif /* CUTE_DEMANGLE_H_ */
