package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings;

import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.Context.ContextState;
import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.ArgumentMapping.Arg;

public class MappingFactory {
	public static Mapping[] createOperatorRefactoringMappings() {
		Mapping mappings[] = new Mapping[]{
			new Mapping(Function.STRCPY, Function.OP_ASSIGNMENT, null),			//strcpy(a, b) -> a = b
			new Mapping(Function.STRCAT, Function.OP_PLUS_ASSIGNMENT, null),		//strcat(a, b) -> a +=b
			new Mapping(Function.STRCMP, Function.OP_EQUALS, null),				//strcmp(a, b) == 0 -> a == b
			new Mapping(Function.STRCMP, Function.OP_NOT_EQUALS, null)			//strcmp(a, b) != 0 -> a != b
		};
		return mappings;
	}
	
	public static Mapping[] createFunctionRefactoringMappings() {
		Mapping mappings[] = new Mapping[]{
			//strlen(str) -> str.size()
			new Mapping(Function.STRLEN, Function.SIZE, new ArgumentMapping(), ContextState.CString),
			
			//strlen(str+off) -> (str.size() - off)
			//strlen(str) -> (str.size() - str_pos)
			new Mapping(Function.STRLEN, Function.SIZE, new ArgumentMapping(), ContextState.CString, ContextState.CStringModified),
			
			//wcslen(wstr) -> wstr.size()
			//todo: if modified: wcslen(wstr) -> (wstr.size() - wstr_pos)
			new Mapping(Function.WCSLEN, Function.SIZE, new ArgumentMapping(), ContextState.CString),
			
			//memcmp(a+off, b, n) -> a.compare(off, n, b, 0, n)
			//memcmp(a, b, n) -> a.compare(a_pos, n, b, 0, n)
			//memcmp(a+off, b, n) -> a.compare(a_pos+off, n, b, 0, n)
			new Mapping(Function.MEMCMP, Function.COMPARE, new ArgumentMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified),

			//memcpy(a+off, b, n) -> a.replace(off, n, b, 0, n)
			//memcpy(a, b, n) -> a.replace(a_pos, n, b, 0, n)
			//memcpy(a+off, b, n) -> a.replace(a_pos+off, n, b, 0, n)
			new Mapping(Function.MEMCPY, Function.REPLACE, new ArgumentMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified),
		
			//memmove(a+off, b, n) -> a.replace(off, n, b, 0, n)
			new Mapping(Function.MEMMOVE, Function.REPLACE, new ArgumentMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified),
			
			//strcmp(a,b) -> a.compare(b)
			new Mapping(Function.STRCMP, Function.COMPARE, new ArgumentMapping(Arg.ARG_1), ContextState.CString),
			
			//strcmp(a+off, b) -> a.compare(off, std::string::npos, b)
			//strcmp(a, b) -> a.compare(a_pos, std::string::npos, b)
			//strcmp(a+off, b) -> a.compare(a_pos+off, std::string::npos, b)
			new Mapping(Function.STRCMP, Function.COMPARE, new ArgumentMapping(Arg.OFF_0, Arg.NPOS, Arg.ARG_1), ContextState.CString, ContextState.CStringModified),
			
			//strncmp(a+off, b, n) -> a.compare(off, n, b, 0, n)
			//strncmp(a, b, n) -> a.compare(a_pos, n, b, 0, n)
			//strncmp(a+off, b, n) -> a.compare(a_pos+off, n, b, 0, n)
			new Mapping(Function.STRNCMP, Function.COMPARE, new ArgumentMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified),
			
			//strcpy(a+off, b) -> a.replace(off, std::string::npos, b)
			new Mapping(Function.STRCPY, Function.REPLACE, new ArgumentMapping(Arg.OFF_0, Arg.NPOS, Arg.ARG_1), ContextState.CString, ContextState.CStringModified),
		
			//strncat(a, b, n) -> a.append(b, 0, n)
			new Mapping(Function.STRNCAT, Function.APPEND, new ArgumentMapping(Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString),
			
			//strncpy(a+off, b, n) -> a.replace(off, n, b, 0, n)
			new Mapping(Function.STRNCPY, Function.REPLACE, new ArgumentMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2), ContextState.CString, ContextState.CStringModified),
			
			//alias: strchr(a, b) -> str.find(b, a) 
			new Mapping(Function.STRCHR, Function.FIND, new ArgumentMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CStringAlias),
			
			//atof(a) -> stod(a)
			new Mapping(Function.ATOF, Function.STOD, new ArgumentMapping(Arg.ARG_0), ContextState.CString),
			
			//atoi(a) -> stoi(a)
			new Mapping(Function.ATOI, Function.STOI, new ArgumentMapping(Arg.ARG_0), ContextState.CString),
			
			//atol(a) -> stol(a)
			new Mapping(Function.ATOL, Function.STOL, new ArgumentMapping(Arg.ARG_0), ContextState.CString),
			
			//atoll(a) -> stoll(a)
			new Mapping(Function.ATOLL, Function.STOLL, new ArgumentMapping(Arg.ARG_0), ContextState.CString)
		};
		return mappings;
	}
	
	public static Mapping[] createComparisonRefactoringMappings() {
		Mapping mappings[] = new Mapping[]{
			//strchr(a,b) == NULL -> a.find(b) == std::string::npos
			//strchr(a,b) != NULL -> a.find(b) != std::string::npos
			new Mapping(Function.STRCHR, Function.FIND, new ArgumentMapping(Arg.ARG_1), ContextState.CString),
			
			//strchr(a+n,b) == NULL -> a.find(b, n) == std::string::npos
			//strchr(a+n,b) != NULL -> a.find(b, n) != std::string::npos
			new Mapping(Function.STRCHR, Function.FIND, new ArgumentMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified, ContextState.CStringAlias),
			
			//strpbrk(a,b) == NULL -> a.find_first_of(b) == std::string::npos
			//strpbrk(a,b) != NULL -> a.find_first_of(b) != std::string::npos
			new Mapping(Function.STRPBRK, Function.FIND_FIRST_OF, new ArgumentMapping(Arg.ARG_1), ContextState.CString),
			
			//strpbrk(a+n,b) == NULL -> a.find_first_of(b, n) == std::string::npos
			//strpbrk(a+n,b) != NULL -> a.find_first_of(b, n) != std::string::npos
			new Mapping(Function.STRPBRK, Function.FIND_FIRST_OF, new ArgumentMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified),
			
			//strrchr(a,b) == NULL -> a.rfind(b) == std::string::npos
			//strrchr(a,b) != NULL -> a.rfind(b) != std::string::npos
			new Mapping(Function.STRRCHR, Function.RFIND, new ArgumentMapping(Arg.ARG_1), ContextState.CString),
			
			//strstr(a,b) == NULL -> a.find(b) == std::string::npos
			//strstr(a,b) != NULL -> a.find(b) != std::string::npos
			new Mapping(Function.STRSTR, Function.FIND, new ArgumentMapping(Arg.ARG_1), ContextState.CString),
			
			//strstr(a+n,b) == NULL -> a.find(b, n) == std::string::npos
			//strstr(a+n,b) != NULL -> a.find(b, n) != std::string::npos
			new Mapping(Function.STRSTR, Function.FIND, new ArgumentMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified),
				
			//strcspn(a, b) == strlen(a) -> a.find_first_of(b) == std::string::npos
			//strcspn(a, b) != strlen(a) -> a.find_first_of(b) != std::string::npos
			new Mapping(Function.STRCSPN, Function.FIND_FIRST_OF, new ArgumentMapping(Arg.ARG_1), ContextState.CString),
			
			//strcspn(a+n, b) == strlen(a) -> a.find_first_of(b, n) == std::string::npos
			//strcspn(a+n, b) != strlen(a) -> a.find_first_of(b, n) != std::string::npos
			new Mapping(Function.STRCSPN, Function.FIND_FIRST_OF, new ArgumentMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified),
				
			//strspn(a, b) == strlen(a) -> a.find_first_not_of(b) == std::string::npos
			//strspn(a, b) != strlen(a) -> a.find_first_not_of(b) != std::string::npos
			new Mapping(Function.STRSPN, Function.FIND_FIRST_NOT_OF, new ArgumentMapping(Arg.ARG_1), ContextState.CString),
			
			//strspn(a+n, b) == strlen(a) -> a.find_first_not_of(b, n) == std::string::npos
			//strspn(a+n, b) != strlen(a) -> a.find_first_not_of(b, n) != std::string::npos
			new Mapping(Function.STRSPN, Function.FIND_FIRST_NOT_OF, new ArgumentMapping(Arg.ARG_1, Arg.OFF_0), ContextState.CString, ContextState.CStringModified),
			
			//memchr(str, ch, count) == NULL -> std::find(str.begin(), str.end(), ch) == str.end()
			//memchr(str, ch, count) != NULL -> std::find(str.begin(), str.end(), ch) != str.end()
			new Mapping(Function.MEMCHR, Function.STD_FIND, new ArgumentMapping(Arg.BEGIN, Arg.END, Arg.ARG_1), ContextState.CString)
		};
		return mappings;
	}
	
	public static Mapping[] createCStringConversionRefactoringMappings() {
		Mapping mappings[] = new Mapping[]{
			//char *found = strstr(str, "searchstr") -> char *found = strstr(&*str.begin(), "searchstr")
			//const char *found = strstr(str, "searchstr") -> const char *found = strstr(str.c_str(), "searchstr")
			new Mapping(Function.STRSTR, Function.STRSTR, null, ContextState.CString),
			
			//char *found = strrchr(str, '@') -> char *found = strrchr(&*str.begin(), '@')
			//const char *found = strrchr(str, '@') -> const char *found = strrchr(str.c_str(), '@')
			new Mapping(Function.STRRCHR, Function.STRRCHR, null, ContextState.CString),
			
			//char *pos = strpbrk(str, "searchstr") -> char *found = strpbrk(&*str.begin(), "searchstr")
			//const char *pos = strpbrk(str, "searchstr") -> const char *found = strpbrk(str.c_str(), "searchstr")
			new Mapping(Function.STRPBRK, Function.STRPBRK, null, ContextState.CString),
			
			//char *found = strchr(str, '@') -> char *found = strchr(&*str.begin(), '@')
			//const char *found = strchr(str, '@') -> const char *found = strchr(str.c_str(), '@')
			new Mapping(Function.STRCHR, Function.STRCHR, null, ContextState.CString)
		};
		return mappings;
	}
	
	public static Mapping[] createRemoveStatementRefactoringMappings() {
		Mapping mappings[] = new Mapping[]{
			//free(str) -> <gets removed>
			new Mapping(Function.FREE, null, null, ContextState.CString)
		};
		return mappings;
	}
}
