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
//test class functor (cursor at function)
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