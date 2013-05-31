//============================================================================
// Name        : tt.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include <functional>
#include <string>
using namespace std;
struct test{
	void operator()()const{ theTest(); }
	std::string name()const{ return name_;}


	// (real) functor types can (almost) spell their name
	// but a name can also be given explicitely, e.g. for CUTE() macro
	// for simple test functions
	template <typename VoidFunctor>
	test(VoidFunctor const &t, std::string sname =(typeid(VoidFunctor).name()))
	:theTest(t),name_(sname){}

private:
	std::function<void()> theTest;
	std::string name_;
};


struct t{
	t(int x){}
	void operator ()(){}
};
template <typename TestFunctor, typename ContextObject>
struct test_incarnate_with_context {
	test_incarnate_with_context(ContextObject context):theContext(context)
	{}
	void operator()(){
		TestFunctor t(theContext);// wouldn't create temporary to call with ()()
		t();
	}
	ContextObject theContext;
};
template <typename TestFunctor,typename ContextObject>
test make_incarnate_with_context(ContextObject obj){
	return test(test_incarnate_with_context<TestFunctor,ContextObject>(obj),(typeid(TestFunctor).name()));
}

int main() {
	test f = make_incarnate_with_context<t>(1);
	cout << "!!!Hello World!!!" << endl; // prints !!!Hello World!!!
	return 0;
}
