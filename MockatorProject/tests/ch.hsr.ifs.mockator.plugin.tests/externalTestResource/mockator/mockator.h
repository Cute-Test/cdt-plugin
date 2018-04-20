#ifndef MOCKATOR_H_
#define MOCKATOR_H_

#include "cute.h"

namespace mockator {
   struct call {
     template<typename ... Param>
     call(std::string const& funSig, Param const& ... params) {
     }

     std::string getTrace() const {
       return "";
     }
   };
   typedef std::vector<call> calls;
}

#if !defined(USE_STD11)
#define USE_BOOST_NS using namespace boost::assign;
#else
#define USE_BOOST_NS
#endif

#define INIT_MOCKATOR() using namespace mockator;\
    USE_BOOST_NS\
    static size_t mockCounter_ = 0;

namespace mockator {
    size_t reserveNextCallId(std::vector<calls> & allCalls) {
      size_t counter = allCalls.size();
      allCalls.push_back(calls());
      return counter;
    }
    inline bool operator<(call const& lhs, call const& rhs) {
        return lhs.getTrace() < rhs.getTrace();
    }

    inline bool operator==(call const& lhs, call const& rhs) {
        return lhs.getTrace() == rhs.getTrace();
    }

    inline bool operator!=(call const& lhs, call const& rhs) {
        return !(lhs == rhs);
    }
}

#endif
