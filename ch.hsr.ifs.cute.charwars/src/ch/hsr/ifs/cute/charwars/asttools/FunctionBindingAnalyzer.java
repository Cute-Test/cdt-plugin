package ch.hsr.ifs.cute.charwars.asttools;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

import ch.hsr.ifs.cute.charwars.constants.StdString;

public class FunctionBindingAnalyzer {
	public static boolean isValidOverload(ICPPFunction originalOverload, ICPPFunction possibleOverload, int strArgIndex) {
		if(!originalOverload.getName().equals(possibleOverload.getName())) {
			return false;
		}
		
		boolean matchingVisibility = matchingVisibility(originalOverload, possibleOverload);
		boolean matchingParameters = matchingParameters(originalOverload, possibleOverload, strArgIndex);
		boolean matchingReturnTypes = matchingReturnTypes(originalOverload, possibleOverload);
		return  matchingVisibility && matchingParameters && matchingReturnTypes;
	}
	
	private static boolean matchingVisibility(ICPPFunction originalOverload, ICPPFunction possibleOverload) {
		if(!(originalOverload instanceof ICPPMember) && !(possibleOverload instanceof ICPPMember)) {
			return true;
		}
		
		if(originalOverload instanceof ICPPMember) {
			int originalOverloadVisibility = ((ICPPMember)originalOverload).getVisibility();
			if(possibleOverload instanceof ICPPMember) {
				int possibleOverloadVisibility = ((ICPPMember)possibleOverload).getVisibility();
				return originalOverloadVisibility == possibleOverloadVisibility;
			}
		}
		return false;
	}
	
	private static boolean matchingParameters(ICPPFunction originalOverload, ICPPFunction possibleOverload, int strArgIndex) {
		ICPPParameter[] parameters1 = originalOverload.getParameters();
		ICPPParameter[] parameters2 = possibleOverload.getParameters();
		
		if(parameters1.length != parameters2.length) {
			return false;
		}
		
		for(int i = 0; i < parameters1.length; ++i) {
			ICPPParameter f1Parameter = parameters1[i];
			ICPPParameter f2Parameter = parameters2[i];
			
			if(i == strArgIndex) {
				if(!isStdStringParameter(f2Parameter)) {
					return false;
				}
			}
			else if(!parameterTypesMatch(f1Parameter, f2Parameter)) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean isStdStringParameter(ICPPParameter parameter) {
		IType type = parameter.getType();
		String typeStr = ASTTypeUtil.getType(type);
		boolean isStdString = typeStr.contains(StdString.STRING) && !typeStr.contains("&") && !typeStr.contains("*");
		boolean isConstStdStringReference = typeStr.contains(StdString.STRING) && typeStr.contains("&") && typeStr.contains("const");
		return isStdString || isConstStdStringReference;
	}
	
	private static boolean matchingReturnTypes(ICPPFunction originalOverload, ICPPFunction possibleOverload) {
		return matchingTypes(originalOverload.getType().getReturnType(), possibleOverload.getType().getReturnType());
	}
	
	private static boolean parameterTypesMatch(ICPPParameter originalOverloadParameter, ICPPParameter possibleOverloadParameter) {
		return matchingTypes(originalOverloadParameter.getType(), possibleOverloadParameter.getType());
	}
	
	private static boolean matchingTypes(IType type1, IType type2) {
		final String pattern = "<.*>";
		String type1Str = ASTTypeUtil.getType(type1).replaceAll(pattern, "");
		String type2Str = ASTTypeUtil.getType(type2).replaceAll(pattern, "");
		return type1Str.equals(type2Str);
	}
}
