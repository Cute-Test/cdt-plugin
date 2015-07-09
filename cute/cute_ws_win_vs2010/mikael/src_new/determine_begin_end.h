/*
 * determine_begin_end.h
 *
 *  Created on: 02.06.2013
 *      Author: Administrator
 */

#ifndef DETERMINE_BEGIN_END_H_
#define DETERMINE_BEGIN_END_H_
#include <string>
#include <algorithm>

namespace cute {
namespace cute_to_string {
		static inline std::string backslashQuoteTabNewline(std::string const &input){
			std::string result;
			result.reserve(input.size());
			for (std::string::size_type i=0; i < input.length() ; ++i){
				switch(input[i]) {
					case '\n': result += "\\n"; break;
					case '\t': result += "\\t"; break;
					case '\\': result += "\\\\"; break;
					case '\r': result += "\\r"; break;
					default: result += input[i];
				}
			}
			return result;
		}
		// common overloads of interface that work without an ostream
		static inline std::string to_string(char const *const &s){
			return s;
		}
		static inline std::string to_string(std::string const &s){
			return s;
		}

	}
}
#ifndef DONT_USE_IOSTREAM
#include <ostream>
#include <sstream>
namespace cute {
namespace cute_to_string {
template <typename T>
std::ostream &to_stream(std::ostream &os,T const &t); // recursion needs forward

// the following code was stolen and adapted from Boost Exception library.
// it avoids compile errors, if a type used with ASSERT_EQUALS doesn't provide an output shift operator
namespace to_string_detail {
	template <class T,class CharT,class Traits>
	char operator<<( std::basic_ostream<CharT,Traits> &, T const & );
	template <class T,class CharT,class Traits>
	struct is_output_streamable_impl {
		static std::basic_ostream<CharT,Traits> & f();
		static T const & g();
		enum e { value = (sizeof(char) != sizeof(f()<<g())) }; // assumes sizeof(char)!=sizeof(ostream&)
	};
	// specialization for pointer types to map char * to operator<<(std::ostream&,char const *)
	template <class T,class CharT,class Traits>
	struct is_output_streamable_impl<T*,CharT,Traits> {
		static std::basic_ostream<CharT,Traits> & f();
		static T const * g();
		enum e { value = (sizeof(char) != sizeof(f()<<g())) }; // assumes sizeof(char)!=sizeof(ostream&)
	};

#ifndef _MSC_VER
			template <class CONT>
			struct has_begin_end_const_member {
				template <typename T, T, T> struct type_check;
				template <typename C> static typename C::const_iterator test(
						type_check<typename C::const_iterator (C::*)()const,&C::begin, &C::end>*);
				template <typename C> static char test(...);
				enum e { value = (sizeof(char) != sizeof(test<CONT>(0)))
				};
			};
#else
		namespace has_begin_const_member_detail {
		// parts of this code provided by Mikael Kilpeläinen
		template<int N> struct Int { char x[N]; typedef char type; };
		template<> struct Int<0> { };
		template<> struct Int<1> { };
		typedef char subst_failure;
		template<typename C1 >
		Int<2> ty(int, typename C1::const_iterator (C1::*)() const,typename C1::const_iterator (C1::*)() const);
		// check if the following is really needed:
//			template<typename C1 >
//			Int<2> ty(long, typename C1::iterator (C1::*p)());
		template<typename C1>
		char ty( int, ... );
		template<typename T, int S >
		struct helper {
		   enum { value = sizeof( ty<T>( 0, &T::begin, &T::end ) ) };
		};
		template<typename C>
		Int<2> f( typename Int<helper<C, sizeof( ty<C>( 0, &C::begin, &C::end ) )>::value>::type*);
		template<typename C>
		subst_failure f(...);
		}
		template <class CONT>
		struct has_begin_end_const_member
		{
		   enum e { value = (sizeof(has_begin_const_member_detail::subst_failure) != sizeof(has_begin_const_member_detail::f<CONT>(0))) };
		};
#endif
	}
}
}
#endif //dont use ostream
#endif /* DETERMINE_BEGIN_END_H_ */
