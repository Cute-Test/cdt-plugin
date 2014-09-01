package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common.mappings;

import ch.hsr.ifs.cute.charwars.constants.Algorithm;
import ch.hsr.ifs.cute.charwars.constants.CStdLib;
import ch.hsr.ifs.cute.charwars.constants.CString;
import ch.hsr.ifs.cute.charwars.constants.CWchar;
import ch.hsr.ifs.cute.charwars.constants.Constants;
import ch.hsr.ifs.cute.charwars.constants.StdString;

public class FunctionDescription {
	public final static FunctionDescription STRCPY = new FunctionDescription(CString.STRCPY, Sentinel.NO_SENTINEL, false, CString.HEADER_NAME);
	public final static FunctionDescription STRNCPY = new FunctionDescription(CString.STRNCPY, Sentinel.NO_SENTINEL, false, CString.HEADER_NAME);
	public final static FunctionDescription STRCAT = new FunctionDescription(CString.STRCAT, Sentinel.NO_SENTINEL, false, CString.HEADER_NAME);
	public final static FunctionDescription STRNCAT = new FunctionDescription(CString.STRNCAT, Sentinel.NO_SENTINEL, false, CString.HEADER_NAME);
	public final static FunctionDescription STRLEN = new FunctionDescription(CString.STRLEN, Sentinel.NO_SENTINEL, false, CString.HEADER_NAME);
	public final static FunctionDescription WCSLEN = new FunctionDescription(CWchar.WCSLEN, Sentinel.NO_SENTINEL, false, CWchar.HEADER_NAME);
	public final static FunctionDescription STRCMP = new FunctionDescription(CString.STRCMP, Sentinel.NO_SENTINEL, false, CString.HEADER_NAME);
	public final static FunctionDescription STRNCMP = new FunctionDescription(CString.STRNCMP, Sentinel.NO_SENTINEL, false, CString.HEADER_NAME);
	public final static FunctionDescription STRCHR = new FunctionDescription(CString.STRCHR, Sentinel.NULL, false, CString.HEADER_NAME);
	public final static FunctionDescription STRRCHR = new FunctionDescription(CString.STRRCHR, Sentinel.NULL, false, CString.HEADER_NAME);
	public final static FunctionDescription STRSPN = new FunctionDescription(CString.STRSPN, Sentinel.STRLEN, false, CString.HEADER_NAME);
	public final static FunctionDescription STRCSPN = new FunctionDescription(CString.STRCSPN, Sentinel.STRLEN, false, CString.HEADER_NAME);
	public final static FunctionDescription STRPBRK = new FunctionDescription(CString.STRPBRK, Sentinel.NULL, false, CString.HEADER_NAME);
	public final static FunctionDescription STRSTR = new FunctionDescription(CString.STRSTR, Sentinel.NULL, false, CString.HEADER_NAME);
	public final static FunctionDescription MEMCHR = new FunctionDescription(CString.MEMCHR, Sentinel.NULL, false, CString.HEADER_NAME);
	public final static FunctionDescription MEMCMP = new FunctionDescription(CString.MEMCMP, Sentinel.NO_SENTINEL, false, CString.HEADER_NAME);
	public final static FunctionDescription MEMCPY = new FunctionDescription(CString.MEMCPY, Sentinel.NO_SENTINEL, false, CString.HEADER_NAME);
	public final static FunctionDescription MEMMOVE = new FunctionDescription(CString.MEMMOVE, Sentinel.NO_SENTINEL, false, CString.HEADER_NAME);
	public final static FunctionDescription FREE = new FunctionDescription(CStdLib.FREE, Sentinel.NO_SENTINEL, false, CStdLib.HEADER_NAME);
	public final static FunctionDescription ATOF = new FunctionDescription(CStdLib.ATOF, Sentinel.NO_SENTINEL, false, CStdLib.HEADER_NAME);
	public final static FunctionDescription ATOI = new FunctionDescription(CStdLib.ATOI, Sentinel.NO_SENTINEL, false, CStdLib.HEADER_NAME);
	public final static FunctionDescription ATOL = new FunctionDescription(CStdLib.ATOL, Sentinel.NO_SENTINEL, false, CStdLib.HEADER_NAME);
	public final static FunctionDescription ATOLL = new FunctionDescription(CStdLib.ATOLL, Sentinel.NO_SENTINEL, false, CStdLib.HEADER_NAME);
	public final static FunctionDescription STOD = new FunctionDescription(Constants.STD_PREFIX+StdString.STOD, Sentinel.NO_SENTINEL, false, StdString.HEADER_NAME);
	public final static FunctionDescription STOI = new FunctionDescription(Constants.STD_PREFIX+StdString.STOI, Sentinel.NO_SENTINEL, false, StdString.HEADER_NAME);
	public final static FunctionDescription STOL = new FunctionDescription(Constants.STD_PREFIX+StdString.STOL, Sentinel.NO_SENTINEL, false, StdString.HEADER_NAME);
	public final static FunctionDescription STOLL = new FunctionDescription(Constants.STD_PREFIX+StdString.STOLL, Sentinel.NO_SENTINEL, false, StdString.HEADER_NAME);
	public final static FunctionDescription STD_FIND = new FunctionDescription(Algorithm.FIND, Sentinel.END, false, Algorithm.HEADER_NAME); 
	
	public final static FunctionDescription OP_ASSIGNMENT = new FunctionDescription(StdString.OP_ASSIGNMENT, Sentinel.NO_SENTINEL, true, StdString.HEADER_NAME);
	public final static FunctionDescription OP_PLUS_ASSIGNMENT = new FunctionDescription(StdString.OP_PLUS_ASSIGNMENT, Sentinel.NO_SENTINEL, true, StdString.HEADER_NAME);
	public final static FunctionDescription OP_EQUALS = new FunctionDescription(StdString.OP_EQUALS, Sentinel.NO_SENTINEL, true, StdString.HEADER_NAME);
	public final static FunctionDescription OP_NOT_EQUALS = new FunctionDescription(StdString.OP_NOT_EQUALS, Sentinel.NO_SENTINEL, true, StdString.HEADER_NAME);
	public final static FunctionDescription EMPTY = new FunctionDescription(StdString.EMPTY, Sentinel.NO_SENTINEL, true, StdString.HEADER_NAME);
	public final static FunctionDescription SIZE = new FunctionDescription(StdString.SIZE, Sentinel.NO_SENTINEL, true, StdString.HEADER_NAME);
	public final static FunctionDescription APPEND = new FunctionDescription(StdString.APPEND, Sentinel.NO_SENTINEL, true, StdString.HEADER_NAME);
	public final static FunctionDescription COMPARE = new FunctionDescription(StdString.COMPARE, Sentinel.NO_SENTINEL, true, StdString.HEADER_NAME);
	public final static FunctionDescription REPLACE = new FunctionDescription(StdString.REPLACE, Sentinel.NO_SENTINEL, true, StdString.HEADER_NAME);
	public final static FunctionDescription FIND = new FunctionDescription(StdString.FIND, Sentinel.NPOS, true, StdString.HEADER_NAME);
	public final static FunctionDescription RFIND = new FunctionDescription(StdString.RFIND, Sentinel.NPOS, true, StdString.HEADER_NAME);
	public final static FunctionDescription FIND_FIRST_OF = new FunctionDescription(StdString.FIND_FIRST_OF, Sentinel.NPOS, true, StdString.HEADER_NAME);
	public final static FunctionDescription FIND_FIRST_NOT_OF = new FunctionDescription(StdString.FIND_FIRST_NOT_OF, Sentinel.NPOS, true, StdString.HEADER_NAME);
	
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
	
	public FunctionDescription(String name, Sentinel sentinel, boolean isMemberFunction, String header) {
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
