//============================================================================
// Name        : container_detection.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================
#include <map>
#include <set>
#include <list>
#include <iostream>
#include <string>
#include <type_traits>
#include <vector>
#if 0
			template <class CONT>
			struct has_begin_end_const_member {
				struct hasit{char x[2];};
				template <typename T, T, T> struct type_check;
				template <typename C> static hasit test(
						type_check<typename C::const_iterator (C::*)()const,&C::begin, &C::end>*);
				template <typename C> static char test(...);
				enum e { value = (sizeof(char) != sizeof(test<CONT>(0)))
				};
			};
#else
#if 0
			// code provided by Jonathan Wakely, doesn't work with visual C++ 2012
			template <class CONT>
			struct has_begin_end_const_member
			{
			   template<int N> struct Int { char x[N]; };

			   typedef char subst_failure;

			   template <typename C1, typename C2>
			       static Int<2>
			       test2(C1 const *,typename C2::const_iterator (C1::*)()const);

			   template <typename C>
			       static Int<sizeof(test2((C const *)0,&C::begin))
			                + sizeof(test2((C const *)0,&C::end))>
			       test(int);

			   template <typename C>
			       static subst_failure
			       test(...);

			   enum e { value = (sizeof(subst_failure) != sizeof(test<CONT>(0))) };
			};

#else
			template<int N> struct Int { char x[N]; typedef char type; };
			template<> struct Int<0> { };
			template<> struct Int<1> { };

			typedef char subst_failure;


			template<typename C1 >
			Int<2> ty(int, typename C1::const_iterator (C1::*)() const,typename C1::const_iterator (C1::*)() const);
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
			char f(...);

			template <class CONT>
			struct has_begin_end_const_member
			{
			   enum e { value = (sizeof(subst_failure) != sizeof(f<CONT>(0))) };
			};

#endif
#endif

struct cont{
				typedef long const_iterator;
				const_iterator begin()const {
					return 0L;
				}
				const_iterator end()const {
					return 1L;
				}
			};
struct subcont: cont{};
struct onlybegin {
	typedef short const_iterator;
	const_iterator begin() const { return 2;}
};
template<class T> struct has_const_begin
{
    typedef char (&Yes)[2];
    typedef char (&No)[1];

    template<class U>
    static Yes test(U const * data,
                    typename std::enable_if<std::is_same<
                             typename U::const_iterator,
                             decltype(data->begin())
                    >::value>::type * = 0);
    static No test(...);
    static const bool value = sizeof(Yes) == sizeof(has_const_begin::test((typename std::remove_reference<T>::type*)0));
};

using namespace std;

int main() {
	cout << "has const begin subcont: "<<has_const_begin<subcont>::value<<endl;
	cout << "has const begin cont: "<<has_const_begin<cont>::value<<endl;
	cout << "has const begin onlybegin: "<<has_const_begin<onlybegin>::value<<endl;
	cout << "has const begin map: "<<has_const_begin<map<string,string> >::value<<endl;
	cout << "has const begin vector: "<<has_const_begin<vector<string> >::value<<endl;


	cout << "cont"<<has_begin_end_const_member<cont>::value << endl;
	cout << "subcont"<<has_begin_end_const_member<subcont>::value << endl;
	cout << "onlybegin"<<has_begin_end_const_member<onlybegin>::value << endl;
	cout << "map"<<has_begin_end_const_member<map<string,string>>::value << endl;
	cout << "vector"<<has_begin_end_const_member<vector<int>>::value << endl;
	cout << "set"<<has_begin_end_const_member<set<int>>::value << endl;
	cout << "list"<<has_begin_end_const_member<list<int>>::value << endl;
	cout << _MSC_VER +0 << endl;
	return 0;
}
