package ch.hsr.ifs.cute.charwars.asttools;

import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;

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
				if(!TypeAnalyzer.isStdStringType(f2Parameter.getType())) {
					return false;
				}
			}
			else if(!parameterTypesMatch(f1Parameter, f2Parameter)) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean matchingReturnTypes(ICPPFunction originalOverload, ICPPFunction possibleOverload) {
		IType originalOverloadReturnType = originalOverload.getType().getReturnType();
		IType possibleOverloadReturnType = possibleOverload.getType().getReturnType();
		return TypeAnalyzer.matchingTypes(originalOverloadReturnType, possibleOverloadReturnType);
	}
	
	private static boolean parameterTypesMatch(ICPPParameter originalOverloadParameter, ICPPParameter possibleOverloadParameter) {
		return TypeAnalyzer.matchingTypes(originalOverloadParameter.getType(), possibleOverloadParameter.getType());
	}
}
