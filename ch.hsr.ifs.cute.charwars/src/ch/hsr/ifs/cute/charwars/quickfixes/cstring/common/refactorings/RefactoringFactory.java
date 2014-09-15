package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context.ContextState;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.ArgMapping.Arg;

public class RefactoringFactory {
	public static Refactoring[] createRefactorings() {
		return new Refactoring[] {
			//various -> see ExpressionRefactoring class
			new ExpressionRefactoring(ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),

			//strcpy(a, b) -> a = b
			new OperatorRefactoring(Function.STRCPY, Function.OP_ASSIGNMENT, ContextState.CString),
			
			//strcat(a, b) -> a +=b
			new OperatorRefactoring(Function.STRCAT, Function.OP_PLUS_ASSIGNMENT, ContextState.CString),
			
			//strcmp(a, b) == 0 -> a == b
			new OperatorRefactoring(Function.STRCMP, Function.OP_EQUALS, ContextState.CString),
			
			//strcmp(a, b) != 0 -> a != b
			new OperatorRefactoring(Function.STRCMP, Function.OP_NOT_EQUALS, ContextState.CString),

			//strchr(a,b) == NULL -> a.find(b) == std::string::npos
			new ComparisonRefactoring(Function.STRCHR, Function.FIND, new ArgMapping(Arg.ARG_1), ContextState.CString),
						
			//strchr(a+n,b) == NULL -> a.find(b, n) == std::string::npos
			//alias: strchr(a, b) == NULL -> str.find(b, a) == std::string::npos
			new ComparisonRefactoring(Function.STRCHR, Function.FIND, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
						
			//strpbrk(a,b) == NULL -> a.find_first_of(b) == std::string::npos
			new ComparisonRefactoring(Function.STRPBRK, Function.FIND_FIRST_OF, new ArgMapping(Arg.ARG_1), ContextState.CString),
						
			//strpbrk(a+n,b) == NULL -> a.find_first_of(b, n) == std::string::npos
			//alias: strpbrk(a, b) == NULL -> str.find_first_of(b, a) == std::string::npos
			new ComparisonRefactoring(Function.STRPBRK, Function.FIND_FIRST_OF, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
						
			//strrchr(a,b) == NULL -> a.rfind(b) == std::string::npos
			new ComparisonRefactoring(Function.STRRCHR, Function.RFIND, new ArgMapping(Arg.ARG_1), ContextState.CString),
						
			//strstr(a,b) == NULL -> a.find(b) == std::string::npos
			new ComparisonRefactoring(Function.STRSTR, Function.FIND, new ArgMapping(Arg.ARG_1), ContextState.CString),
						
			//strstr(a+n,b) == NULL -> a.find(b, n) == std::string::npos
			//alias: strstr(a, b) == NULL -> str.find(b, a) == std::string::npos
			new ComparisonRefactoring(Function.STRSTR, Function.FIND, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
							
			//strcspn(a, b) == strlen(a) -> a.find_first_of(b) == std::string::npos
			new ComparisonRefactoring(Function.STRCSPN, Function.FIND_FIRST_OF, new ArgMapping(Arg.ARG_1), ContextState.CString),
						
			//strcspn(a+n, b) == strlen(a) -> a.find_first_of(b, n) == std::string::npos
			new ComparisonRefactoring(Function.STRCSPN, Function.FIND_FIRST_OF, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified),
							
			//strspn(a, b) == strlen(a) -> a.find_first_not_of(b) == std::string::npos
			new ComparisonRefactoring(Function.STRSPN, Function.FIND_FIRST_NOT_OF, new ArgMapping(Arg.ARG_1), ContextState.CString),
						
			//strspn(a+n, b) == strlen(a) -> a.find_first_not_of(b, n) == std::string::npos
			new ComparisonRefactoring(Function.STRSPN, Function.FIND_FIRST_NOT_OF, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified),
						
			//memchr(str, ch, count) == NULL -> std::find(str.begin(), str.end(), ch) == str.end()
			new ComparisonRefactoring(Function.MEMCHR, Function.STD_FIND, new ArgMapping(Arg.BEGIN, Arg.END, Arg.ARG_1), ContextState.CString),

			//strlen(str) -> str.size()
			new FunctionRefactoring(Function.STRLEN, Function.SIZE, new ArgMapping(), ContextState.CString),
				
			//strlen(str+off) -> (str.size() - off)
			//strlen(str) -> (str.size() - str_pos)
			new FunctionRefactoring(Function.STRLEN, Function.SIZE, new ArgMapping(), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
				
			//wcslen(wstr) -> wstr.size()
			new FunctionRefactoring(Function.WCSLEN, Function.SIZE, new ArgMapping(), ContextState.CString),
			
			//wcslen(wstr+off) -> (wstr.size() - off)
			//wcslen(wstr) -> (wstr.size() - wstr_pos)
			new FunctionRefactoring(Function.WCSLEN, Function.SIZE, new ArgMapping(), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
				
			//memcmp(a+off, b, n) -> a.compare(off, n, b, 0, n)
			//memcmp(a, b, n) -> a.compare(a_pos, n, b, 0, n)
			//memcmp(a+off, b, n) -> a.compare(a_pos+off, n, b, 0, n)
			new FunctionRefactoring(Function.MEMCMP, Function.COMPARE, new ArgMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified),

			//memcpy(a+off, b, n) -> a.replace(off, n, b, 0, n)
			//memcpy(a, b, n) -> a.replace(a_pos, n, b, 0, n)
			//memcpy(a+off, b, n) -> a.replace(a_pos+off, n, b, 0, n)
			new FunctionRefactoring(Function.MEMCPY, Function.REPLACE, new ArgMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified),
			
			//memmove(a+off, b, n) -> a.replace(off, n, b, 0, n)
			new FunctionRefactoring(Function.MEMMOVE, Function.REPLACE, new ArgMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified),
				
			//strcmp(a,b) -> a.compare(b)
			new FunctionRefactoring(Function.STRCMP, Function.COMPARE, new ArgMapping(Arg.ARG_1), ContextState.CString),
				
			//strcmp(a+off, b) -> a.compare(off, std::string::npos, b)
			//strcmp(a, b) -> a.compare(a_pos, std::string::npos, b)
			//strcmp(a+off, b) -> a.compare(a_pos+off, std::string::npos, b)
			new FunctionRefactoring(Function.STRCMP, Function.COMPARE, new ArgMapping(Arg.OFF_0, Arg.NPOS, Arg.ARG_1), ContextState.CString, ContextState.CStringModified),
				
			//strncmp(a+off, b, n) -> a.compare(off, n, b, 0, n)
			//strncmp(a, b, n) -> a.compare(a_pos, n, b, 0, n)
			//strncmp(a+off, b, n) -> a.compare(a_pos+off, n, b, 0, n)
			new FunctionRefactoring(Function.STRNCMP, Function.COMPARE, new ArgMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified),
				
			//strcpy(a+off, b) -> a.replace(off, std::string::npos, b)
			new FunctionRefactoring(Function.STRCPY, Function.REPLACE, new ArgMapping(Arg.OFF_0, Arg.NPOS, Arg.ARG_1), ContextState.CString, ContextState.CStringModified),
			
			//strncat(a, b, n) -> a.append(b, 0, n)
			new FunctionRefactoring(Function.STRNCAT, Function.APPEND, new ArgMapping(Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString),
			
			//strncpy(a+off, b, n) -> a.replace(off, n, b, 0, n)
			new FunctionRefactoring(Function.STRNCPY, Function.REPLACE, new ArgMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified),
			
			//alias: strchr(a, b) -> str.find(b, a) 
			new FunctionRefactoring(Function.STRCHR, Function.FIND, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CStringAlias),
			
			//alias: strstr(a, b) -> str.find(b, a) 
			new FunctionRefactoring(Function.STRSTR, Function.FIND, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CStringAlias),
			
			//alias: strpbrk(a, b) -> str.find(b, a)
			new FunctionRefactoring(Function.STRPBRK, Function.FIND_FIRST_OF, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CStringAlias),
				
			//atof(a) -> stod(a)
			new FunctionRefactoring(Function.ATOF, Function.STOD, new ArgMapping(Arg.ARG_0), ContextState.CString),
				
			//atoi(a) -> stoi(a)
			new FunctionRefactoring(Function.ATOI, Function.STOI, new ArgMapping(Arg.ARG_0), ContextState.CString),
			
			//atol(a) -> stol(a)
			new FunctionRefactoring(Function.ATOL, Function.STOL, new ArgMapping(Arg.ARG_0), ContextState.CString),
			
			//atoll(a) -> stoll(a)
			new FunctionRefactoring(Function.ATOLL, Function.STOLL, new ArgMapping(Arg.ARG_0), ContextState.CString),
			
			//char *found = strstr(str, "searchstr") -> char *found = strstr(&*str.begin(), "searchstr")
			//const char *found = strstr(str, "searchstr") -> const char *found = strstr(str.c_str(), "searchstr")
			new CStringConversionRefactoring(Function.STRSTR, ContextState.CString),
			
			//char *found = strrchr(str, '@') -> char *found = strrchr(&*str.begin(), '@')
			//const char *found = strrchr(str, '@') -> const char *found = strrchr(str.c_str(), '@')
			new CStringConversionRefactoring(Function.STRRCHR, ContextState.CString),
			
			//char *pos = strpbrk(str, "searchstr") -> char *found = strpbrk(&*str.begin(), "searchstr")
			//const char *pos = strpbrk(str, "searchstr") -> const char *found = strpbrk(str.c_str(), "searchstr")
			new CStringConversionRefactoring(Function.STRPBRK, ContextState.CString),
			
			//char *found = strchr(str, '@') -> char *found = strchr(&*str.begin(), '@')
			//const char *found = strchr(str, '@') -> const char *found = strchr(str.c_str(), '@')
			new CStringConversionRefactoring(Function.STRCHR, ContextState.CString),
			
			//free(str) -> <gets removed>
			new RemoveStatementRefactoring(Function.FREE, ContextState.CString),
			
			//various -> see NullRefactoring class
			new NullRefactoring(ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
			
			//various -> see DefaultRefactoring class
			new DefaultRefactoring(ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias)
		};	
	}
}
