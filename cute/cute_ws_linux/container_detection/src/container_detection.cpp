#include <map>
#include <set>
#include <list>
#include <iostream>
#include <string>
#include <type_traits>
#include <vector>
#if 0 // my original code...
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
			// code provided by Jonathan Wakely, doesn't work with visual C++ 2012
			template <class CONT>
			struct has_begin_end_const_member
			{
			   template<int N> struct Int { char x[N]; };

			   typedef char subst_failure;
			   template <typename C1, typename C2>
			          static Int<2>
			          test2(typename C1::const_iterator (C2::*)()const);

			      template <typename C>
			          static Int<sizeof(test2<C>(&C::begin)) + sizeof(test2<C>(&C::end))>
			          test(int);

			   template <typename C>
			       static subst_failure
			       test(...);

			   enum e { value = (sizeof(subst_failure) != sizeof(test<CONT>(0))) };
			};
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
	cout << "has const begin map: "<<has_const_begin<map<string,string> >::value<<endl;
	cout << "has const begin vector: "<<has_const_begin<vector<string> >::value<<endl;


	cout << sizeof(has_begin_end_const_member<subcont>::test<subcont>(nullptr))<<endl;
	cout << sizeof(has_begin_end_const_member<cont>::test<cont>(nullptr))<<endl;
	cout << "cont"<<has_begin_end_const_member<cont>::value << endl; // prints !!!Hello World!!!
	cout << "subcont"<<has_begin_end_const_member<subcont>::value << endl; // prints !!!Hello World!!!
	cout << "map"<<has_begin_end_const_member<map<string,string>>::value << endl; // prints !!!Hello World!!!
	cout << "vector"<<has_begin_end_const_member<vector<int>>::value << endl; // prints !!!Hello World!!!
	cout << "set"<<has_begin_end_const_member<set<int>>::value << endl; // prints !!!Hello World!!!
	cout << "list"<<has_begin_end_const_member<list<int>>::value << endl; // prints !!!Hello World!!!
	return 0;
}
