package ch.hsr.ifs.cute.charwars.checkers.cstr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IndexFilter;

import ch.hsr.ifs.cute.charwars.asttools.FunctionBindingAnalyzer;

public class OverloadChecker {
	public ICPPFunction[] getValidOverloads(IASTName name, int strArgIndex) {
		List<ICPPFunction> validOverloads = new ArrayList<ICPPFunction>();
		
		IIndex index = name.getTranslationUnit().getIndex();
		ICPPFunction originalOverload = getFunctionBindingFromName(name, index);
		
		if(isOperatorOverloadMemberFunction(name, originalOverload) && strArgIndex == 1) {
			strArgIndex--;
		}
		
		try {
			IIndexBinding[] bindings = index.findBindings(originalOverload.getQualifiedNameCharArray(), IndexFilter.ALL_DECLARED, null);		
			
			for(IIndexBinding binding : bindings) {
				if(binding instanceof ICPPFunction) {
					ICPPFunction possibleOverload = (ICPPFunction)binding;
					if(FunctionBindingAnalyzer.isValidOverload(originalOverload, possibleOverload, strArgIndex)) {
						validOverloads.add(possibleOverload);
					}
				}
			}
		}
		catch(Exception e) {
			return new ICPPFunction[]{};
		}
		return validOverloads.toArray(new ICPPFunction[]{});
	}
		
	private boolean isOperatorOverloadMemberFunction(IASTName name, ICPPFunction function) {
		return name instanceof IASTImplicitName && function instanceof ICPPMethod;
	}
	
	private ICPPFunction getFunctionBindingFromName(IASTName name, IIndex index) {
		ICPPFunction functionBinding = (ICPPFunction)index.adaptBinding(name.resolveBinding());
		if(functionBinding instanceof ICPPSpecialization) {
			ICPPSpecialization specialization = (ICPPSpecialization)functionBinding;
			functionBinding = (ICPPFunction)specialization.getSpecializedBinding();
		}
		return functionBinding;
	}
}