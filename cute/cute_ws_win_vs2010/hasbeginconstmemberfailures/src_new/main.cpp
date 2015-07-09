#include <iostream>
#include <set>
#include <map>
#include <list>
#include <vector>

struct A{
			typedef void const_iterator;
			const_iterator begin()const;
			const_iterator end()const;
		};
struct B:A{};

struct Y {
   char begin() const;
};
struct U {};


namespace mikael2 {
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


}

int main(){
using namespace std;
	using std::cout;using std::set; using std::vector;
using namespace mikael2;
cout << "set:"<< std::boolalpha <<
		bool(has_begin_end_const_member<set<int> >::value ) <<"\n";
cout << "vector:"<< std::boolalpha <<
		bool(has_begin_end_const_member<vector<int> >::value ) <<"\n";
static_assert(has_begin_end_const_member<A>::value,"mikael A");
static_assert(has_begin_end_const_member<B>::value,"mikael A");
#if 0
static_assert(!has_begin_end_const_member<::Y>::value,"mikael Y");
static_assert(!has_begin_end_const_member<::U>::value,"mikael Y");
#endif
static_assert(has_begin_end_const_member<A>::value,"mikael A");
static_assert(has_begin_end_const_member<B>::value,"mikael A");
cout << "A:"<< std::boolalpha <<
		bool(has_begin_end_const_member<A >::value ) <<"\n";
cout << "B:"<< std::boolalpha <<
		bool(has_begin_end_const_member<B >::value ) <<"\n";
#if 0
cout << "Y:"<< std::boolalpha <<
		bool(has_begin_end_const_member<Y >::value ) <<"\n";
cout << "U:"<< std::boolalpha <<
		bool(has_begin_end_const_member<U >::value ) <<"\n";
static_assert(!has_begin_end_const_member<::Y>::value," Y");
static_assert(!has_begin_end_const_member<::U>::value," U");
#endif
static_assert( has_begin_end_const_member<std::map<int,int>>::value == true, "error1" );
static_assert( has_begin_end_const_member<std::vector<int>>::value == true, "error1" );
static_assert( has_begin_end_const_member<std::list<int>>::value == true, "error1" );
#if 0
static_assert( has_begin_end_const_member<U>::value == false, "error3" );
static_assert( has_begin_end_const_member<Y>::value == false, "error4" );
#endif
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
   static_assert( ! has_begin_end_const_member<A>::value == false, "error2" );
   static_assert( has_begin_end_const_member<B>::value == true, "error2" );
#if 0
   static_assert( ! has_begin_end_const_member<U>::value, "error3" );
   static_assert( has_begin_end_const_member<Y>::value == false, "error4" );
#endif
   static_assert( ! has_begin_end_const_member<int>::value, "int" ); // if uncommented -> C1004
}
