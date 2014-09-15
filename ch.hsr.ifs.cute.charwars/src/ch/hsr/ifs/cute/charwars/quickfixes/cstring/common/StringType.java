package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;

import ch.hsr.ifs.cute.charwars.constants.StdString;

public class StringType {
	public final static StringType STRING = new StringType(StdString.STRING_SIZE_TYPE);
	public final static StringType WSTRING = new StringType(StdString.WSTRING_SIZE_TYPE);
	
	private String sizeType;
	
	public static StringType createFromDeclSpecifier(IASTDeclSpecifier declSpecifier) {
		if(declSpecifier instanceof IASTSimpleDeclSpecifier) {
			IASTSimpleDeclSpecifier simpleDeclSpecifier = (IASTSimpleDeclSpecifier)declSpecifier;
			if(simpleDeclSpecifier.getType() == IASTSimpleDeclSpecifier.t_wchar_t) {
				return WSTRING;
			}
		}
		return STRING;
	}
	
	private StringType(String sizeType) {
		this.sizeType = sizeType;
	}
	
	public String getSizeType() {
		return sizeType;
	}
}
