package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings;

import ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings.ArgumentMapping.Arg;

public class MappingFactory {
	public static Mapping[] createOperatorRefactoringMappings() {
		Mapping mappings[] = new Mapping[]{
			new Mapping(FunctionDescription.STRCPY, FunctionDescription.OP_ASSIGNMENT, false, null),			//strcpy(a, b) -> a = b
			new Mapping(FunctionDescription.STRCAT, FunctionDescription.OP_PLUS_ASSIGNMENT, false, null),		//strcat(a, b) -> a +=b
			new Mapping(FunctionDescription.STRCMP, FunctionDescription.OP_EQUALS, false, null),				//strcmp(a, b) == 0 -> a == b
			new Mapping(FunctionDescription.STRCMP, FunctionDescription.OP_NOT_EQUALS, false, null)				//strcmp(a, b) != 0 -> a != b
		};
		return mappings;
	}
	
	public static Mapping[] createFunctionRefactoringMappings() {
		Mapping mappings[] = new Mapping[]{
			//strlen(str) -> str.size()
			//todo: if modified: strlen(str) -> (str.size() - str_pos)
			new Mapping(FunctionDescription.STRLEN, FunctionDescription.SIZE, false, new ArgumentMapping()),
			
			//wcslen(wstr) -> wstr.size()
			//todo: if modified: wcslen(wstr) -> (wstr.size() - wstr_pos)
			new Mapping(FunctionDescription.WCSLEN, FunctionDescription.SIZE, false, new ArgumentMapping()),
			
			//memcmp(a+off, b, n) -> a.compare(off, n, b, 0, n)
			//todo: if modified: memcmp(a, b, n) -> a.compare(a_pos, n, b, 0, n)
			new Mapping(FunctionDescription.MEMCMP, FunctionDescription.COMPARE, true, new ArgumentMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2)),

			//memcpy(a+off, b, n) -> a.replace(off, n, b, 0, n)
			//memcpy(a, b, n) -> a.replace(a_pos, n, b, 0, n)
			//memcpy(a+off, b, n) -> a.replace(a_pos+off, n, b, 0, n)
			new Mapping(FunctionDescription.MEMCPY, FunctionDescription.REPLACE, true, new ArgumentMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2)),
		
			//memmove(a+off, b, n) -> a.replace(off, n, b, 0, n)
			new Mapping(FunctionDescription.MEMMOVE, FunctionDescription.REPLACE, true, new ArgumentMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2)),
			
			//strcmp(a,b) -> a.compare(b)
			new Mapping(FunctionDescription.STRCMP, FunctionDescription.COMPARE, false, new ArgumentMapping(Arg.ARG_1)),
			
			//strcmp(a+off, b) -> a.compare(off, std::string::npos, b)
			//todo: if modified: strcmp(a,b) -> a.compare(a+off, std::string::npos, b)
			new Mapping(FunctionDescription.STRCMP, FunctionDescription.COMPARE, true, new ArgumentMapping(Arg.OFF_0, Arg.NPOS, Arg.ARG_1)),
			
			//strncmp(a+off, b, n) -> a.compare(off, n, b, 0, n)
			//strncmp(a, b, n) -> a.compare(a_pos, n, b, 0, n)
			//strncmp(a+off, b, n) -> a.compare(a_pos+off, n, b, 0, n)
			new Mapping(FunctionDescription.STRNCMP, FunctionDescription.COMPARE, true, new ArgumentMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2)),
			
			//strcpy(a+off, b) -> a.replace(off, std::string::npos, b)
			new Mapping(FunctionDescription.STRCPY, FunctionDescription.REPLACE, true, new ArgumentMapping(Arg.OFF_0, Arg.NPOS, Arg.ARG_1)),
		
			//strncat(a, b, n) -> a.append(b, 0, n)
			new Mapping(FunctionDescription.STRNCAT, FunctionDescription.APPEND, false, new ArgumentMapping(Arg.ARG_1, Arg.ZERO, Arg.ARG_2)),
			
			//strncpy(a+off, b, n) -> a.replace(off, n, b, 0, n)
			new Mapping(FunctionDescription.STRNCPY, FunctionDescription.REPLACE, true, new ArgumentMapping(Arg.OFF_0, Arg.ARG_2, Arg.ARG_1, Arg.ZERO, Arg.ARG_2)),
			
			//atof(a) -> stod(a)
			new Mapping(FunctionDescription.ATOF, FunctionDescription.STOD, false, new ArgumentMapping(Arg.ARG_0)),
			
			//atoi(a) -> stoi(a)
			new Mapping(FunctionDescription.ATOI, FunctionDescription.STOI, false, new ArgumentMapping(Arg.ARG_0)),
			
			//atol(a) -> stol(a)
			new Mapping(FunctionDescription.ATOL, FunctionDescription.STOL, false, new ArgumentMapping(Arg.ARG_0)),
			
			//atoll(a) -> stoll(a)
			new Mapping(FunctionDescription.ATOLL, FunctionDescription.STOLL, false, new ArgumentMapping(Arg.ARG_0))
		};
		return mappings;
	}
	
	public static Mapping[] createComparisonRefactoringMappings() {
		Mapping mappings[] = new Mapping[]{
			//strchr(a,b) == NULL -> a.find(b) == std::string::npos
			//strchr(a,b) != NULL -> a.find(b) != std::string::npos
			new Mapping(FunctionDescription.STRCHR, FunctionDescription.FIND, false, new ArgumentMapping(Arg.ARG_1)),
			
			//strchr(a+n,b) == NULL -> a.find(b, n) == std::string::npos
			//strchr(a+n,b) != NULL -> a.find(b, n) != std::string::npos
			new Mapping(FunctionDescription.STRCHR, FunctionDescription.FIND, true, new ArgumentMapping(Arg.ARG_1, Arg.OFF_0)),
			
			//strpbrk(a,b) == NULL -> a.find_first_of(b) == std::string::npos
			//strpbrk(a,b) != NULL -> a.find_first_of(b) != std::string::npos
			new Mapping(FunctionDescription.STRPBRK, FunctionDescription.FIND_FIRST_OF, false, new ArgumentMapping(Arg.ARG_1)),
			
			//strrchr(a,b) == NULL -> a.rfind(b) == std::string::npos
			//strrchr(a,b) != NULL -> a.rfind(b) != std::string::npos
			new Mapping(FunctionDescription.STRRCHR, FunctionDescription.RFIND, false, new ArgumentMapping(Arg.ARG_1)),
			
			//strstr(a,b) == NULL -> a.find(b) == std::string::npos
			//strstr(a,b) != NULL -> a.find(b) != std::string::npos
			new Mapping(FunctionDescription.STRSTR, FunctionDescription.FIND, false, new ArgumentMapping(Arg.ARG_1)),
				
			//strcspn(a, b) == strlen(a) -> a.find_first_of(b) == std::string::npos
			//strcspn(a, b) != strlen(a) -> a.find_first_of(b) != std::string::npos
			new Mapping(FunctionDescription.STRCSPN, FunctionDescription.FIND_FIRST_OF, false, new ArgumentMapping(Arg.ARG_1)),
				
			//strspn(a, b) == strlen(a) -> a.find_first_not_of(b) == std::string::npos
			//strspn(a, b) != strlen(a) -> a.find_first_not_of(b) != std::string::npos
			new Mapping(FunctionDescription.STRSPN, FunctionDescription.FIND_FIRST_NOT_OF, false, new ArgumentMapping(Arg.ARG_1)),
			
			//memchr(str, ch, count) == NULL -> std::find(str.begin(), str.end(), ch) == str.end()
			//memchr(str, ch, count) != NULL -> std::find(str.begin(), str.end(), ch) != str.end()
			new Mapping(FunctionDescription.MEMCHR, FunctionDescription.STD_FIND, false, new ArgumentMapping(Arg.BEGIN, Arg.END, Arg.ARG_1))
		};
		return mappings;
	}
	
	public static Mapping[] createCStringConversionRefactoringMappings() {
		Mapping mappings[] = new Mapping[]{
			//char *found = strstr(str, "searchstr") -> char *found = strstr(&*str.begin(), "searchstr")
			//const char *found = strstr(str, "searchstr") -> const char *found = strstr(str.c_str(), "searchstr")
			new Mapping(FunctionDescription.STRSTR, FunctionDescription.STRSTR, false, null),
			
			//char *found = strrchr(str, '@') -> char *found = strrchr(&*str.begin(), '@')
			//const char *found = strrchr(str, '@') -> const char *found = strrchr(str.c_str(), '@')
			new Mapping(FunctionDescription.STRRCHR, FunctionDescription.STRRCHR, false, null),
			
			//char *pos = strpbrk(str, "searchstr") -> char *found = strpbrk(&*str.begin(), "searchstr")
			//const char *pos = strpbrk(str, "searchstr") -> const char *found = strpbrk(str.c_str(), "searchstr")
			new Mapping(FunctionDescription.STRPBRK, FunctionDescription.STRPBRK, false, null),
			
			//char *found = strchr(str, '@') -> char *found = strchr(&*str.begin(), '@')
			//const char *found = strchr(str, '@') -> const char *found = strchr(str.c_str(), '@')
			new Mapping(FunctionDescription.STRCHR, FunctionDescription.STRCHR, false, null)
		};
		return mappings;
	}
	
	public static Mapping[] createRemoveStatementRefactoringMappings() {
		Mapping mappings[] = new Mapping[]{
			//free(str) -> <gets removed>
			new Mapping(FunctionDescription.FREE, null, false, null)
		};
		return mappings;
	}
}
