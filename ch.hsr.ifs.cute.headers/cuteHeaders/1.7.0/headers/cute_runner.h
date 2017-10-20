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

#ifndef CUTE_RUNNER_H_
#define CUTE_RUNNER_H_
#include "cute_test.h"
#include "cute_suite.h"
#include "cute_listener.h"
namespace cute {
	template <typename Listener=null_listener>
	struct runner : Listener{
		runner():Listener(){}
		runner(Listener &s):Listener(s){}
		bool operator()(test const &t){
			return runit(t);
		}
		bool operator()(suite const &s,char const *info=""){
			Listener::begin(s,info);
			bool result=true;
			for(suite::const_iterator it=s.begin();
			    it != s.end();
			    ++it){
			    	result = this->runit(*it) && result;
			    }
			Listener::end(s,info);
			return result;
		}
	private:
		bool runit(test const &t){
			try {
				Listener::start(t);
				t();
				Listener::success(t,"OK");
				return true;
			} catch (cute::test_failure const &e){
				Listener::failure(t,e);
			} catch (std::exception const &exc){
				Listener::error(t,demangle(exc.what()).c_str());
			} catch (std::string &s){
				Listener::error(t,s.c_str());
			} catch (char const *&cs) {
				Listener::error(t,cs);
			} catch(...) {
				Listener::error(t,"unknown exception thrown");
			}
			return false;
		}
	};
	template <typename Listener>
	runner<Listener> makeRunner(Listener &s){
		return runner<Listener>(s);
	}
}
#endif /*CUTE_RUNNER_H_*/
