package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings;

import ch.hsr.ifs.cute.charwars.constants.Function;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context.ContextState;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.refactorings.ArgMapping.Arg;

public class RefactoringFactory {
	public static Refactoring[] createRefactorings() {
		return new Refactoring[] {
			//various -> see ExpressionRefactoring class
			new ExpressionRefactoring(ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),

			//strcpy(str, b) -> str = b
			new OperatorRefactoring(Function.STRCPY, Function.OP_ASSIGNMENT, ContextState.CString),
			
			//strcat(str, b) -> str +=b
			new OperatorRefactoring(Function.STRCAT, Function.OP_PLUS_ASSIGNMENT, ContextState.CString),
			
			//strcmp(str, b) == 0 -> str == b
			new OperatorRefactoring(Function.STRCMP, Function.OP_EQUALS, ContextState.CString),
			
			//wcscmp(wstr, b) == 0 -> wstr == b
			new OperatorRefactoring(Function.WCSCMP, Function.OP_EQUALS, ContextState.CString),
			
			//strcmp(str, b) != 0 -> str != b
			new OperatorRefactoring(Function.STRCMP, Function.OP_NOT_EQUALS, ContextState.CString),
			
			//wcscmp(wstr, b) != 0 -> wstr != b
			new OperatorRefactoring(Function.WCSCMP, Function.OP_NOT_EQUALS, ContextState.CString),

			//strchr(str,b) == NULL -> str.find(b) == std::string::npos
			new ComparisonRefactoring(Function.STRCHR, Function.FIND, new ArgMapping(Arg.ARG_1), ContextState.CString),
			
			//wcschr(wstr,b) == NULL -> wstr.find(b) == std::wstring::npos
			new ComparisonRefactoring(Function.WCSCHR, Function.FIND, new ArgMapping(Arg.ARG_1), ContextState.CString),
						
			//explicit offset: strchr(str+off,b) == NULL -> str.find(b, off) == std::string::npos
			//implicit offset: strchr(str, b) == NULL -> str.find(b, str_pos) == std::string::npos
			//alias: strchr(alias, b) == NULL -> str.find(b, alias) == std::string::npos
			new ComparisonRefactoring(Function.STRCHR, Function.FIND, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
						
			//explicit offset: wcschr(wstr+off,b) == NULL -> wstr.find(b, off) == std::wstring::npos
			//implicit offset: wcschr(wstr, b) == NULL -> wstr.find(b, wstr_pos) == std::wstring::npos
			//alias: wcschr(alias, b) == NULL -> wstr.find(b, alias) == std::wstring::npos
			new ComparisonRefactoring(Function.WCSCHR, Function.FIND, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
			
			//strpbrk(str,b) == NULL -> str.find_first_of(b) == std::string::npos
			new ComparisonRefactoring(Function.STRPBRK, Function.FIND_FIRST_OF, new ArgMapping(Arg.ARG_1), ContextState.CString),
			
			//wcspbrk(wstr,b) == NULL -> wstr.find_first_of(b) == std::wstring::npos
			new ComparisonRefactoring(Function.WCSPBRK, Function.FIND_FIRST_OF, new ArgMapping(Arg.ARG_1), ContextState.CString),
						
			//explicit offset: strpbrk(str+off,b) == NULL -> str.find_first_of(b, off) == std::string::npos
			//implicit offset: strprbk(str, b) == NULL -> str.find_first_of(b, str_pos) == std::string::npos
			//alias: strpbrk(alias, b) == NULL -> str.find_first_of(b, alias) == std::string::npos
			new ComparisonRefactoring(Function.STRPBRK, Function.FIND_FIRST_OF, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
			
			//explicit offset: wcspbrk(wstr+off,b) == NULL -> wstr.find_first_of(b, off) == std::wstring::npos
			//implicit offset: wcsprbk(wstr, b) == NULL -> wstr.find_first_of(b, wstr_pos) == std::wstring::npos
			//alias: wcspbrk(alias, b) == NULL -> wstr.find_first_of(b, alias) == std::wstring::npos
			new ComparisonRefactoring(Function.WCSPBRK, Function.FIND_FIRST_OF, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
						
			//strrchr(str,b) == NULL -> str.rfind(b) == std::string::npos
			new ComparisonRefactoring(Function.STRRCHR, Function.RFIND, new ArgMapping(Arg.ARG_1), ContextState.CString),
						
			//strstr(str,b) == NULL -> str.find(b) == std::string::npos
			new ComparisonRefactoring(Function.STRSTR, Function.FIND, new ArgMapping(Arg.ARG_1), ContextState.CString),
			
			//wcsstr(wstr,b) == NULL -> wstr.find(b) == std::wstring::npos
			new ComparisonRefactoring(Function.WCSSTR, Function.FIND, new ArgMapping(Arg.ARG_1), ContextState.CString),
						
			//explicit offset: strstr(str+off,b) == NULL -> str.find(b, off) == std::string::npos
			//implicit offset: strstr(str, b) == NULL -> str.find(b, str_pos) == std::string::npos
			//alias: strstr(alias, b) == NULL -> str.find(b, alias) == std::string::npos
			new ComparisonRefactoring(Function.STRSTR, Function.FIND, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
			
			//explicit offset: wcsstr(wstr+off,b) == NULL -> wstr.find(b, off) == std::wstring::npos
			//implicit offset: wcsstr(wstr, b) == NULL -> wstr.find(b, wstr_pos) == std::wstring::npos
			//alias: wcsstr(alias, b) == NULL -> wstr.find(b, alias) == std::wstring::npos
			new ComparisonRefactoring(Function.WCSSTR, Function.FIND, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
			
			//strcspn(str, b) == strlen(str) -> str.find_first_of(b) == std::string::npos
			new ComparisonRefactoring(Function.STRCSPN, Function.FIND_FIRST_OF, new ArgMapping(Arg.ARG_1), ContextState.CString),
						
			//strcspn(str+n, b) == strlen(str) -> str.find_first_of(b, n) == std::string::npos
			new ComparisonRefactoring(Function.STRCSPN, Function.FIND_FIRST_OF, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified),
							
			//strspn(str, b) == strlen(str) -> str.find_first_not_of(b) == std::string::npos
			new ComparisonRefactoring(Function.STRSPN, Function.FIND_FIRST_NOT_OF, new ArgMapping(Arg.ARG_1), ContextState.CString),
						
			//strspn(str+n, b) == strlen(str) -> str.find_first_not_of(b, n) == std::string::npos
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
				
			//explicit offset: memcmp(str+off, b, n) -> str.compare(off, n, b, 0, n)
			//implicit offset: memcmp(str, b, n) -> str.compare(str_pos, n, b, 0, n)
			//alias: memcmp(alias, b, n) -> str.compare(alias, n, b, 0, n)
			new FunctionRefactoring(Function.MEMCMP, Function.COMPARE, new ArgMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),

			//explicit offset: memcpy(str+off, b, n) -> str.replace(off, n, b, 0, n)
			//implicit offset: memcpy(str, b, n) -> str.replace(str_pos, n, b, 0, n)
			new FunctionRefactoring(Function.MEMCPY, Function.REPLACE, new ArgMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified),
			
			//memmove(str+off, b, n) -> str.replace(off, n, b, 0, n)
			new FunctionRefactoring(Function.MEMMOVE, Function.REPLACE, new ArgMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified),
				
			//strcmp(str,b) -> str.compare(b)
			new FunctionRefactoring(Function.STRCMP, Function.COMPARE, new ArgMapping(Arg.ARG_1), ContextState.CString),
			
			//wcscmp(wstr,b) -> wstr.compare(b)
			new FunctionRefactoring(Function.WCSCMP, Function.COMPARE, new ArgMapping(Arg.ARG_1), ContextState.CString),
				
			//explicit offset: strcmp(str+off, b) -> str.compare(off, std::string::npos, b)
			//implicit offset: strcmp(str, b) -> str.compare(str_pos, std::string::npos, b)
			//alias: strcmp(alias, b) -> str.compare(alias, std::string::npos, b)
			new FunctionRefactoring(Function.STRCMP, Function.COMPARE, new ArgMapping(Arg.OFF_0, Arg.NPOS, Arg.ARG_1), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
			
			//explicit offset: wcscmp(wstr+off, b) -> wstr.compare(off, std::string::npos, b)
			//implicit offset: wcscmp(wstr, b) -> wstr.compare(wstr_pos, std::string::npos, b)
			//alias: wcscmp(alias, b) -> wstr.compare(alias, std::string::npos, b)
			new FunctionRefactoring(Function.WCSCMP, Function.COMPARE, new ArgMapping(Arg.OFF_0, Arg.NPOS, Arg.ARG_1), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),

			//explicit offset: strncmp(str+off, b, n) -> str.compare(off, n, b, 0, n)
			//implicit offset: strncmp(str, b, n) -> str.compare(str_pos, n, b, 0, n)
			//alias: strncmp(alias, b, n) -> str.compare(alias, n, b, 0, n)
			new FunctionRefactoring(Function.STRNCMP, Function.COMPARE, new ArgMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
				
			//explicit offset: strcpy(str+off, b) -> str.replace(off, std::string::npos, b)
			new FunctionRefactoring(Function.STRCPY, Function.REPLACE, new ArgMapping(Arg.OFF_0, Arg.NPOS, Arg.ARG_1), ContextState.CString, ContextState.CStringModified),
			
			//strncat(str, b, n) -> str.append(b, 0, n)
			new FunctionRefactoring(Function.STRNCAT, Function.APPEND, new ArgMapping(Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString),
			
			//strncpy(str+off, b, n) -> str.replace(off, n, b, 0, n)
			new FunctionRefactoring(Function.STRNCPY, Function.REPLACE, new ArgMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified),
			
			//alias: strchr(alias, b) -> str.find(b, alias) 
			new FunctionRefactoring(Function.STRCHR, Function.FIND, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CStringAlias),
			
			//alias: strstr(alias, b) -> str.find(b, alias) 
			new FunctionRefactoring(Function.STRSTR, Function.FIND, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CStringAlias),
			
			//alias: strpbrk(alias, b) -> str.find(b, alias)
			new FunctionRefactoring(Function.STRPBRK, Function.FIND_FIRST_OF, new ArgMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CStringAlias),
				
			//atof(str) -> stod(str)
			new FunctionRefactoring(Function.ATOF, Function.STOD, new ArgMapping(Arg.ARG_0), ContextState.CString),
				
			//atoi(str) -> stoi(str)
			new FunctionRefactoring(Function.ATOI, Function.STOI, new ArgMapping(Arg.ARG_0), ContextState.CString),
			
			//atol(str) -> stol(str)
			new FunctionRefactoring(Function.ATOL, Function.STOL, new ArgMapping(Arg.ARG_0), ContextState.CString),
			
			//atoll(str) -> stoll(str)
			new FunctionRefactoring(Function.ATOLL, Function.STOLL, new ArgMapping(Arg.ARG_0), ContextState.CString),
			
			//free(str) -> <gets removed>
			new RemoveStatementRefactoring(Function.FREE, ContextState.CString),
			
			//various -> see NullRefactoring class
			new NullRefactoring(ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
			
			//various -> see DefaultRefactoring class
			new DefaultRefactoring(ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias)
		};	
	}
}
