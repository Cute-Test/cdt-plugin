///////////////////////////////////////
//test addmember SMEMFUN 
class foo{
public:
	void cow4(){
		std::cout<<"cow4\n";
		ASSERTM("cow4()", false);
	};
}
void runSuite(){
	cute::suite s;
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
class foo{
public:
	void cow4(){
		std::cout<<"cow4\n";
		ASSERTM("cow4()", false);
	};
}
void runSuite(){
	cute::suite s;
	s.push_back(CUTE_SMEMFUN(foo,cow4));
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test addmember MEMFUN 
class foo{
public:
	void cow4(){
		std::cout<<"cow4\n";
		ASSERTM("cow4()", false);
	};
}
void runSuite(){
	foo abc;
	cute::suite s;
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
class foo{
public:
	void cow4(){
		std::cout<<"cow4\n";
		ASSERTM("cow4()", false);
	};
}
void runSuite(){
	foo abc;
	cute::suite s;
	s.push_back(CUTE_MEMFUN(abc,foo,cow4));
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}