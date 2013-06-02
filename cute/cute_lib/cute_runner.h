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
 * Copyright 2006 Peter Sommerlad
 *
 *********************************************************************************/

#ifndef CUTE_RUNNER_H_
#define CUTE_RUNNER_H_
#include "cute_test.h"
#include "cute_suite.h"
#include "cute_listener.h"
#include <algorithm>
#include <iterator>
#include <set>
namespace cute {
	namespace runner_aux {
	struct prefixMatcher// : std::unary_function<std::string const &,bool>
	{
		prefixMatcher(std::string const &prefix):prefix(prefix){}
		bool operator()(std::string const &s) const {
			size_t found=s.find(prefix);
			return found==0 && (s.size()==prefix.size() || s[prefix.size()]=='#');
		}
	private:
		std::string const prefix;
	};
	struct prefixCutter //: std::unary_function<std::string, std::string>
	{
		prefixCutter(std::string const &prefix):prefix(prefix){}
		std::string operator()(std::string s) const {
			size_t found=s.find(prefix);
			if ( found==0 && s.size()>prefix.size() && s[prefix.size()]=='#'){
				s = s.substr(prefix.size()+1);
			} else {
				s.clear(); // either no match, or no individual test
			}
			return s;
		}
	private:
		std::string const prefix;
	};

	struct ArgvTestFilter:std::unary_function<const test& ,bool>
	{
	    ArgvTestFilter(int argc, const char *const *const argv)
	    :argc(argc), argv(argv)
	    {
	        if(needsFiltering()){
	        	args.reserve(argc-1);
	            std::copy(argv + 1, argv + argc,back_inserter(args));
	        }
	    }

	    bool operator ()(const test & t) const
	    {
	        return !shouldRun(t.name());
	    }

	    bool shouldRun(const std::string & name) const
	    {
	        return match.empty() || match.count(name);
	    }

	    bool shouldRunSuite(std::string info)
	    {
	        match.clear();
	        if(!needsFiltering() || !info.size())
	            return true;
	        if(args.end() != find_if(args.begin(), args.end(), prefixMatcher(info))){
	           std::transform(args.begin(), args.end(), std::inserter(match,match.begin()),prefixCutter(info));
	           match.erase(std::string()); // get rid of empty string
	           return true;
	        }
	        return false;
	    }

	    bool needsFiltering() const
	    {
	        return argc > 1 && argv;
	    }

	private:
	    std::set<std::string> match;
	    std::vector<std::string> args;
	    const int argc;
	    const char * const * const argv;
	};
	} // namespace runner_aux
	template <typename Listener=null_listener>
	struct runner : Listener{

		runner_aux::ArgvTestFilter filter;
		runner():Listener(),filter(0,0){}
		runner(Listener &s, int argc = 0, const char *const *argv = 0):Listener(s),filter(argc,argv){}
		bool operator()(const test & t)
	    {
	        return runit(t);
	    }

	    bool operator ()(suite s, const char *info = "") // copy intentional for filtering support
	    { // copy elision should happen with good compiler, because this will be inlined
	        bool result = true;
	        if(filter.shouldRunSuite(info)){ // side effect on filter
	            filterSuite(s); // to make size correct in output, otherwise superfluous, but do not want to change Listener API
	            Listener::begin(s, info);
	            for(suite::const_iterator it = s.begin();it != s.end();++it){
	                result = this->runit(*it) && result;
	            }
	            Listener::end(s, info);
	        }

	        return result;
	    }
	private:
	    void filterSuite(suite & s)
	    {
	    	if (filter.needsFiltering() && s.end()!=find_if(s.begin(),s.end(),std::not1(filter))){
	    		s.erase(std::remove_if(s.begin(),s.end(),filter),s.end());
	    	}
	    }

	    bool runit(const test & t)
	    {
	        try {
	            Listener::start(t);
	            t();
	            Listener::success(t, "OK");
	            return true;
	        } catch(const cute::test_failure & e){
	            Listener::failure(t, e);
	        } catch(const std::exception & exc){
	            Listener::error(t, demangle(exc.what()).c_str());
	        } catch(std::string & s){
	            Listener::error(t, s.c_str());
	        } catch(const char *&cs) {
				Listener::error(t,cs);
			} catch(...) {
				Listener::error(t,"unknown exception thrown");
			}
			return false;
		}
	};
	template <typename Listener>
	runner<Listener> makeRunner(Listener &s, int argc = 0, const char *const *argv = 0){
		return runner<Listener>(s,argc,argv);
	}
}
#endif /*CUTE_RUNNER_H_*/
