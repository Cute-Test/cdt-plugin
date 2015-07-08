#include <assert.h>
#include <sstream>
#include <string>
#include <map>
#include <vector>
#include <list>
#include <iostream>

namespace detail {
template<int Nelements> struct Int {
	char x[Nelements];
	typedef char type;
};
template<> struct Int<0> {};
template<> struct Int<1> {};
typedef char subst_failure;
struct test_base {
	template<typename C1> static Int<2> ty(int,	typename C1::const_iterator (C1::*p)() const);
	template<typename C1> static subst_failure ty(long,	typename C1::iterator (C1::*p)());
	template<typename C1> static subst_failure ty(int, ...);
	template<typename T, int S>
	struct helper {
		enum {	begin_value = sizeof(ty<T>(0, &T::begin)) };
		enum {	end_value = sizeof(ty<T>(0, &T::end)) };
	};
	template<typename C> static Int<2> begin_f(typename Int<helper<C, sizeof(ty<C>(0, &C::begin))>::begin_value>::type*);
	template<typename C> static subst_failure begin_f(...);
	template<typename C> static Int<2> end_f(typename Int<helper<C, sizeof(ty<C>(0, &C::end))>::end_value>::type*);
	template<typename C> static subst_failure end_f(...);
};

template<typename T>
struct is_class_type {
	template<typename C> static char func(char C::*p);
	template<typename C> static int func(...);
	enum { value = sizeof(func<T>(0)) == 1 };
};

template<class CONTainer>
struct has_begin_end_const_member {
	enum e { value = sizeof(detail::subst_failure)!= sizeof(detail::test_base::begin_f<CONTainer>(0))
		            && sizeof(detail::subst_failure)!= sizeof(detail::test_base::end_f<CONTainer>(0)) };
};
}

template<class CONTainer, bool = detail::is_class_type<CONTainer>::value>
struct has_begin_end_const_member {
	enum { value = detail::has_begin_end_const_member<CONTainer>::value };
};

template<class CONTainer>
struct has_begin_end_const_member<CONTainer, false>
{
	enum { value = false };
};

template<typename T>
struct A {
	typedef char const_iterator;
	typedef int iterator;
	const_iterator begin() const;
	iterator begin();
	iterator end();
};

template<typename T>
struct B: A<T> {
	using A<T>::end;
	const_iterator end() const;
};

struct Y {
	char begin() const;
};

struct U {};

int main() {
	   using namespace std; // moving up triggers an internal compiler error VS2010-> seems that lookup during template instantiation and sfinae is broken
	static_assert( has_begin_end_const_member<std::map<int,int>>::value == true, "error1" );
	static_assert( has_begin_end_const_member<std::vector<int>>::value == true, "error1" );
	static_assert( has_begin_end_const_member<std::list<int>>::value == true, "error1" );
	static_assert( has_begin_end_const_member<A<int>>::value == false, "error2" );
	static_assert( has_begin_end_const_member<B<int>>::value == true, "error2" );
	static_assert( has_begin_end_const_member<U>::value == false, "error3" );
	static_assert( has_begin_end_const_member<Y>::value == false, "error4" );
	static_assert( has_begin_end_const_member<Y>::value == false, "error4" );
	static_assert( has_begin_end_const_member<Y>::value == false, "error4" );
	static_assert( has_begin_end_const_member<int>::value == false, "int" );
	static_assert( has_begin_end_const_member<short>::value == false, "int" );
	static_assert( has_begin_end_const_member<float>::value == false, "int" );
	static_assert( has_begin_end_const_member<int*>::value == false, "int" );
	static_assert( has_begin_end_const_member<int**>::value == false, "int" );
	static_assert( has_begin_end_const_member<int***>::value == false, "int" );
	static_assert( detail::is_class_type<int>::value == false, ".." );
	static_assert( detail::is_class_type<U>::value == true, ".." );
	static_assert( detail::is_class_type<std::list<int>>::value == true, ".." );
	   static_assert( has_begin_end_const_member<std::map<int,int>>::value == true, "error1" );
	   static_assert( has_begin_end_const_member<std::vector<int>>::value == true, "error1" );
	   static_assert( has_begin_end_const_member<std::list<int>>::value == true, "error1" );
	   static_assert( has_begin_end_const_member<A<int>>::value == false, "error2" );
	   static_assert( has_begin_end_const_member<B<int>>::value == true, "error2" );
	   static_assert( ! has_begin_end_const_member<U>::value, "error3" );
		static_assert( has_begin_end_const_member<Y>::value == false, "error4" );
	   static_assert( has_begin_end_const_member<Y>::value == false, "error4" );
	   static_assert( ! has_begin_end_const_member<int>::value, "int" ); // if uncommented -> C1004
cout << _MSC_VER+0 << " detail " << _MSC_FULL_VER<<"\n";
}

