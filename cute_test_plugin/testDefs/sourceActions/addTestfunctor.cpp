//test struct functor
struct aStru^ctWithoutVisiblity{
	bool operator() (){ASSERTM("start writing tests", false);return false;}
};

void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
struct aStructWithoutVisiblity{
	bool operator() (){ASSERTM("start writing tests", false);return false;}
};

void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(aStructWithoutVisiblity());
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test struct functor already added
stru^ct aStructWithoutVisiblity{
	bool operator() (){ASSERTM("start writing tests", false);return false;}
};

void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(aStructWithoutVisiblity());
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
struct aStructWithoutVisiblity{
	bool operator() (){ASSERTM("start writing tests", false);return false;}
};

void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(aStructWithoutVisiblity());
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test struct functor with visbility
struct aStruct{
pu^blic:
	bool operator() (){ASSERTM("start writing tests", false);return false;}
};

void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
struct aStruct{
public:
	bool operator() (){ASSERTM("start writing tests", false);return false;}
};

void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(aStruct());
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}