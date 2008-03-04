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
///////////////////////////////////////
//test class functor
cla^ss ExternalDecl//: public TFunctor
{
	public:
		virtual void operator()();
		virtual void Call(const char* string){}
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
class ExternalDecl//: public TFunctor
{
	public:
		virtual void operator()();
		virtual void Call(const char* string){}
};
void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(ExternalDecl());
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test class functor (cursor at visbility)
class ExternalDecl//: public TFunctor
{
	public^:
		virtual void operator()();
		virtual void Call(const char* string){}
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
//in terms of whether the method is already implemented or not,
//is deferred to the compiler for checking
class ExternalDecl//: public TFunctor
{
	public:
		virtual void operator()();
		virtual void Call(const char* string){}
};
void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(ExternalDecl());
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test class functor (cursor at function declaration)
class ExternalDecl//: public TFunctor
{
	public:
		virtual void opera^tor()();
		virtual void Call(const char* string){}
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
class ExternalDecl//: public TFunctor
{
	public:
		virtual void operator()();
		virtual void Call(const char* string){}
};
void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(ExternalDecl());
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test class functor (cursor at non virtual function)
class ExternalDecl//: public TFunctor
{
	public:
		virtual void operator()();
		virtual void Call(const char* string){}
		int theCo^inIsin();
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
class ExternalDecl//: public TFunctor
{
	public:
		virtual void operator()();
		virtual void Call(const char* string){}
		int theCoinIsin();
};
void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(ExternalDecl());
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test class functor (cursor at function definition)
class ExternalDecl//: public TFunctor
{
	public:
		virtual void operator()();
		virtual void Call(const char* string){}
		int theCoinIsin();
		int foo(){ret^urn 0;}
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
class ExternalDecl//: public TFunctor
{
	public:
		virtual void operator()();
		virtual void Call(const char* string){}
		int theCoinIsin();
		int foo(){return 0;}
};
void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(ExternalDecl());
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test class functor (operator with parameters)
class ExternalDecl//: public TFunctor
{
	public:
		virtual void o^perator()(int x);
		virtual void Call(const char* string){}
		int theCoinIsin();
		int foo(){return 0;}
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
class ExternalDecl//: public TFunctor
{
	public:
		virtual void operator()(int x);
		virtual void Call(const char* string){}
		int theCoinIsin();
		int foo(){return 0;}
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test template class functor (no changes expected)
templ^ate <class TClass> class TSpecificFunctor : public TFunctor
   {
   private:
      void (TClass::*fpt)(const char*);   // pointer to member function
      TClass* pt2Object;                  // pointer to object
   public:
      // constructor - takes pointer to an object and pointer to a member and stores
      // them in two private variables
      TSpecificFunctor(TClass* _pt2Object, void(TClass::*_fpt)(const char*))
         { pt2Object = _pt2Object;  fpt=_fpt; };

      virtual void operator()(const char* string)
       { (*pt2Object.*fpt)(string);};              // execute member function
           // execute member function
   };
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
template <class TClass> class TSpecificFunctor : public TFunctor
   {
   private:
      void (TClass::*fpt)(const char*);   // pointer to member function
      TClass* pt2Object;                  // pointer to object
   public:
      // constructor - takes pointer to an object and pointer to a member and stores
      // them in two private variables
      TSpecificFunctor(TClass* _pt2Object, void(TClass::*_fpt)(const char*))
         { pt2Object = _pt2Object;  fpt=_fpt; };

      virtual void operator()(const char* string)
       { (*pt2Object.*fpt)(string);};              // execute member function
           // execute member function
   };
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test template class functor (cursor at visbility, no changes expected)
template <class TClass> class TSpecificFunctor : public TFunctor
   {
   private:
      void (TClass::*fpt)(const char*);   // pointer to member function
      TClass* pt2Object;                  // pointer to object
   publ^ic:
      // constructor - takes pointer to an object and pointer to a member and stores
      // them in two private variables
      TSpecificFunctor(TClass* _pt2Object, void(TClass::*_fpt)(const char*))
         { pt2Object = _pt2Object;  fpt=_fpt; };

      virtual void operator()(const char* string)
       { (*pt2Object.*fpt)(string);};              // execute member function
           // execute member function
   };
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
template <class TClass> class TSpecificFunctor : public TFunctor
   {
   private:
      void (TClass::*fpt)(const char*);   // pointer to member function
      TClass* pt2Object;                  // pointer to object
   public:
      // constructor - takes pointer to an object and pointer to a member and stores
      // them in two private variables
      TSpecificFunctor(TClass* _pt2Object, void(TClass::*_fpt)(const char*))
         { pt2Object = _pt2Object;  fpt=_fpt; };

      virtual void operator()(const char* string)
       { (*pt2Object.*fpt)(string);};              // execute member function
           // execute member function
   };
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test class functor adding a normal function
void hel^o(){int d=0;}
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
void helo(){int d=0;}
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test cursor at preprocessor
#incl^ude "helo.h"
void hel^o(){int d=0;}
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
#include "helo.h"
void helo(){int d=0;}
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test class functor with constructor taking one parameter
class ExternalDecl//: public TFunctor
{
	int value;
	public:
		ExternalDecl(int x):value(x){}
		virtual void o^perator()();
		virtual void Call(const char* string){}
		int theCoinIsin();
		int foo(){return 0;}
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
class ExternalDecl//: public TFunctor
{
	int value;
	public:
		ExternalDecl(int x):value(x){}
		virtual void operator()();
		virtual void Call(const char* string){}
		int theCoinIsin();
		int foo(){return 0;}
};
void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(ExternalDecl(pArAmEtRs_ReQuIrEd));
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test class functor with constructor (function defn)taking one parameter (cursor at class)
class simplecons^tructor1{
public:
	simpleconstructor1(int b){int c=b+1;}
	void operator()();
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
class simpleconstructor1{
public:
	simpleconstructor1(int b){int c=b+1;}
	void operator()();
};
void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(simpleconstructor1(pArAmEtRs_ReQuIrEd));
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test class functor with constructor (function declarations)taking one parameter (cursor at class)
class simplecons^tructor2{
public:
	simpleconstructor2(int b);
	void operator()(){};
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
class simpleconstructor2{
public:
	simpleconstructor2(int b);
	void operator()(){};
};
void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(simpleconstructor2(pArAmEtRs_ReQuIrEd));
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test struct functor with constructor (function definition)taking one parameter 
struct aStructWithoutVisiblity{
	aStructWithoutVisi^blity(int d){d=0;}
	bool operator() (){ASSERTM("start writing tests", false);return false;}
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
struct aStructWithoutVisiblity{
	aStructWithoutVisiblity(int d){d=0;}
	bool operator() (){ASSERTM("start writing tests", false);return false;}
};
void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(aStructWithoutVisiblity(pArAmEtRs_ReQuIrEd));
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test struct functor with constructor (function declaration)taking one parameter 
struct aStructWithoutVisiblity{
	aStructWithoutVisi^blity(int d);
	bool operator() (){ASSERTM("start writing tests", false);return false;}
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
struct aStructWithoutVisiblity{
	aStructWithoutVisiblity(int d);
	bool operator() (){ASSERTM("start writing tests", false);return false;}
};
void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(aStructWithoutVisiblity(pArAmEtRs_ReQuIrEd));
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test struct functor with parameter constructor (SimpleDeclaration)
struct aStructWithoutVisiblity{
	aStructWithoutVisiblity(){d=0;}
	aStr^uctWithoutVisiblity(int d);
	bool operator() (){ASSERTM("start writing tests", false);return false;}
	void a();
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
struct aStructWithoutVisiblity{
	aStructWithoutVisiblity(){d=0;}
	aStructWithoutVisiblity(int d);
	bool operator() (){ASSERTM("start writing tests", false);return false;}
	void a();
};
void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(aStructWithoutVisiblity(pArAmEtRs_ReQuIrEd));
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
///////////////////////////////////////
//test struct functor with parameter constructor (function defn) 
struct aStructWithoutVisiblity{
	aStru^ctWithoutVisiblity(int d){d=0;}
	aStructWithoutVisiblity();
	bool operator() (){ASSERTM("start writing tests", false);return false;}
	void a();
};
void runTest(){
	cute::suite s=make_suite_s();
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}
//expected 
struct aStructWithoutVisiblity{
	aStructWithoutVisiblity(int d){d=0;}
	aStructWithoutVisiblity();
	bool operator() (){ASSERTM("start writing tests", false);return false;}
	void a();
};
void runTest(){
	cute::suite s=make_suite_s();
	s.push_back(aStructWithoutVisiblity(pArAmEtRs_ReQuIrEd));
	cute::ide_listener lis;
	cute::makeRunner(lis)(s, "The Suite");
}