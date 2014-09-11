package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings;

import ch.hsr.ifs.cute.charwars.constants.Algorithm;
import ch.hsr.ifs.cute.charwars.constants.CStdLib;
import ch.hsr.ifs.cute.charwars.constants.CString;
import ch.hsr.ifs.cute.charwars.constants.CWchar;
import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.constants.StdString;

public class Function {
	public final static Function STRCPY = createFunction(CString.STRCPY, Sentinel.NO_SENTINEL, CString.HEADER_NAME);
	public final static Function STRNCPY = createFunction(CString.STRNCPY, Sentinel.NO_SENTINEL, CString.HEADER_NAME);
	public final static Function STRCAT = createFunction(CString.STRCAT, Sentinel.NO_SENTINEL, CString.HEADER_NAME);
	public final static Function STRNCAT = createFunction(CString.STRNCAT, Sentinel.NO_SENTINEL, CString.HEADER_NAME);
	public final static Function STRLEN = createFunction(CString.STRLEN, Sentinel.NO_SENTINEL, CString.HEADER_NAME);
	public final static Function WCSLEN = createFunction(CWchar.WCSLEN, Sentinel.NO_SENTINEL, CWchar.HEADER_NAME);
	public final static Function STRCMP = createFunction(CString.STRCMP, Sentinel.NO_SENTINEL, CString.HEADER_NAME);
	public final static Function STRNCMP = createFunction(CString.STRNCMP, Sentinel.NO_SENTINEL, CString.HEADER_NAME);
	public final static Function STRCHR = createFunction(CString.STRCHR, Sentinel.NULL, CString.HEADER_NAME);
	public final static Function STRRCHR = createFunction(CString.STRRCHR, Sentinel.NULL, CString.HEADER_NAME);
	public final static Function STRSPN = createFunction(CString.STRSPN, Sentinel.STRLEN, CString.HEADER_NAME);
	public final static Function STRCSPN = createFunction(CString.STRCSPN, Sentinel.STRLEN, CString.HEADER_NAME);
	public final static Function STRPBRK = createFunction(CString.STRPBRK, Sentinel.NULL, CString.HEADER_NAME);
	public final static Function STRSTR = createFunction(CString.STRSTR, Sentinel.NULL, CString.HEADER_NAME);
	public final static Function MEMCHR = createFunction(CString.MEMCHR, Sentinel.NULL, CString.HEADER_NAME);
	public final static Function MEMCMP = createFunction(CString.MEMCMP, Sentinel.NO_SENTINEL, CString.HEADER_NAME);
	public final static Function MEMCPY = createFunction(CString.MEMCPY, Sentinel.NO_SENTINEL, CString.HEADER_NAME);
	public final static Function MEMMOVE = createFunction(CString.MEMMOVE, Sentinel.NO_SENTINEL, CString.HEADER_NAME);
	public final static Function FREE = createFunction(CStdLib.FREE, Sentinel.NO_SENTINEL, CStdLib.HEADER_NAME);
	public final static Function ATOF = createFunction(CStdLib.ATOF, Sentinel.NO_SENTINEL, CStdLib.HEADER_NAME);
	public final static Function ATOI = createFunction(CStdLib.ATOI, Sentinel.NO_SENTINEL, CStdLib.HEADER_NAME);
	public final static Function ATOL = createFunction(CStdLib.ATOL, Sentinel.NO_SENTINEL, CStdLib.HEADER_NAME);
	public final static Function ATOLL = createFunction(CStdLib.ATOLL, Sentinel.NO_SENTINEL, CStdLib.HEADER_NAME);
	public final static Function STOD = createFunction(Constants.STD_PREFIX+StdString.STOD, Sentinel.NO_SENTINEL, StdString.HEADER_NAME);
	public final static Function STOI = createFunction(Constants.STD_PREFIX+StdString.STOI, Sentinel.NO_SENTINEL, StdString.HEADER_NAME);
	public final static Function STOL = createFunction(Constants.STD_PREFIX+StdString.STOL, Sentinel.NO_SENTINEL, StdString.HEADER_NAME);
	public final static Function STOLL = createFunction(Constants.STD_PREFIX+StdString.STOLL, Sentinel.NO_SENTINEL, StdString.HEADER_NAME);
	public final static Function STD_FIND = createFunction(Algorithm.FIND, Sentinel.END, Algorithm.HEADER_NAME); 
	
	public final static Function OP_ASSIGNMENT = createMemberFunction(StdString.OP_ASSIGNMENT, Sentinel.NO_SENTINEL, StdString.HEADER_NAME);
	public final static Function OP_PLUS_ASSIGNMENT = createMemberFunction(StdString.OP_PLUS_ASSIGNMENT, Sentinel.NO_SENTINEL, StdString.HEADER_NAME);
	public final static Function OP_EQUALS = createMemberFunction(StdString.OP_EQUALS, Sentinel.NO_SENTINEL, StdString.HEADER_NAME);
	public final static Function OP_NOT_EQUALS = createMemberFunction(StdString.OP_NOT_EQUALS, Sentinel.NO_SENTINEL, StdString.HEADER_NAME);
	public final static Function EMPTY = createMemberFunction(StdString.EMPTY, Sentinel.NO_SENTINEL, StdString.HEADER_NAME);
	public final static Function SIZE = createMemberFunction(StdString.SIZE, Sentinel.NO_SENTINEL, StdString.HEADER_NAME);
	public final static Function APPEND = createMemberFunction(StdString.APPEND, Sentinel.NO_SENTINEL, StdString.HEADER_NAME);
	public final static Function COMPARE = createMemberFunction(StdString.COMPARE, Sentinel.NO_SENTINEL, StdString.HEADER_NAME);
	public final static Function REPLACE = createMemberFunction(StdString.REPLACE, Sentinel.NO_SENTINEL, StdString.HEADER_NAME);
	public final static Function FIND = createMemberFunction(StdString.FIND, Sentinel.NPOS, StdString.HEADER_NAME);
	public final static Function RFIND = createMemberFunction(StdString.RFIND, Sentinel.NPOS, StdString.HEADER_NAME);
	public final static Function FIND_FIRST_OF = createMemberFunction(StdString.FIND_FIRST_OF, Sentinel.NPOS, StdString.HEADER_NAME);
	public final static Function FIND_FIRST_NOT_OF = createMemberFunction(StdString.FIND_FIRST_NOT_OF, Sentinel.NPOS, StdString.HEADER_NAME);
	
	public enum Sentinel {
		NO_SENTINEL,
		NULL,
		STRLEN,
		NPOS,
		END
	}
	
	private String name;
	private Sentinel sentinel;
	private boolean isMemberFunction;
	private String header;
	
	private static Function createFunction(String name, Sentinel sentinel, String header) {
		return new Function(name, sentinel, false, header);
	}
	
	private static Function createMemberFunction(String name, Sentinel sentinel, String header) {
		return new Function(name, sentinel, true, header);
	}
	
	private Function(String name, Sentinel sentinel, boolean isMemberFunction, String header) {
		this.name = name;
		this.sentinel = sentinel;
		this.isMemberFunction = isMemberFunction;
		this.header = header;
	}
	
	public String getName() {
		return name;
	}
	
	public Sentinel getSentinel() {
		return sentinel;
	}
	
	public boolean isMemberFunction() {
		return isMemberFunction;
	}
	
	public String getHeader() {
		return header;
	}
}
