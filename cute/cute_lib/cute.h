#ifndef CUTE_H_
#define CUTE_H_
#include <string>
namespace cute{
struct cute_exception {
	const std::string reason;
	const std::string filename;
	const int lineno;

	cute_exception(std::string const &r,char const *f, int line)
	:reason(r),filename(f),lineno(line)
	{ 	}
	std::string what() const ;
};
}
#define t_assertm(msg,cond) if (!(cond)) throw cute::cute_exception((msg),__FILE__,__LINE__)
#define t_assert(cond) t_assertm(#cond,cond)
#define t_fail() t_assertm("fail()",false)
#define t_failm(msg) t_assertm(msg,false)

#endif /*CUTE_H_*/
