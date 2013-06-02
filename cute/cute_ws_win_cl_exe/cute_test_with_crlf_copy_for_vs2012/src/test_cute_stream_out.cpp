#include "cute_stream_out.h"
#include "test_cute_stream_out.h"
#include "cute.h"
#include "cute_suite.h"
#include <set>
#include <map>
using cute::cute_to_string::to_string;
void test_simple_to_string(){
	std::string res=cute::cute_to_string::to_string(42);
	ASSERT_EQUAL("42",res);
}
void test_simple_string_to_string(){
	ASSERT_EQUAL("hallo Peter\n",to_string("hallo Peter\n"));
}
void test_simple_char_array_to_string(){
	char a[]="Hello\tWorld\n";
	ASSERT_EQUAL(a,to_string(a));
}

void test_int_array_to_string(){
	int a[3];
	a[0]=1;
	a[1]=2;
	a[2]=3;
	ASSERT_EQUAL("int[3]{\n1,\n2,\n3}",to_string(a));
}

void test_pair_to_string(){
	ASSERT_EQUAL("[1 -> hallo]",to_string(std::make_pair(1,std::string("hallo"))));
}

void test_vector_pair_to_string(){
	typedef std::vector<std::pair<int,int> > Vec;
	Vec v;
	v.push_back(std::make_pair(42,4));
	ASSERT_EQUAL("std::vector<std::pair<int, int>, std::allocator<std::pair<int, int> > >{\n[42 -> 4]}",to_string(v));
}

void test_set_to_string(){
	std::set<int> s;
	s.insert(42); s.insert(1); s.insert(100);
	ASSERT_EQUAL("std::set<int, std::less<int>, std::allocator<int> >{\n1,\n42,\n100}",to_string(s));
}

void test_map_to_string(){
	std::map<std::string,std::string> m;
	m["one"]="two";
	m["three"]="four";
	m["five"]="six";
	std::string exp="std::map<std::string, std::string, std::less<std::string>, std::allocator<std::pair<std::string const, std::string> > >{"
"\n[five -> six],\n[one -> two],\n[three -> four]}";
	std::string res=to_string(m);
#ifndef _MSC_VER
	ASSERT_EQUAL(exp,res);
#endif
// MS VC++ will not detect std::map/std::set as a container, because their begin/end member functions are defined in their superclass... :-(
}








cute::suite test_cute_stream_out(){
	cute::suite s;
	s.push_back(CUTE(test_simple_to_string));
	s.push_back(CUTE(test_simple_string_to_string));
	s.push_back(CUTE(test_simple_char_array_to_string));
	s.push_back(CUTE(test_int_array_to_string));
	s.push_back(CUTE(test_pair_to_string));
	s.push_back(CUTE(test_vector_pair_to_string));
	s.push_back(CUTE(test_set_to_string));
	s.push_back(CUTE(test_map_to_string));
	return s;

}




