package ch.hsr.ifs.constificator.core.deciders.util;

import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionInstance;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPUnknownBinding;

import ch.hsr.ifs.constificator.core.util.ast.Node;
import ch.hsr.ifs.constificator.core.util.ast.Relation;

@SuppressWarnings("restriction")
public class Common {

	public static boolean isUsedInTemplateSpecialization(ICPPASTName name) {
		IASTName[] references = Node.getReferences(name);

		for (IASTName reference : references) {
			IASTFunctionCallExpression call;
			if ((call = Relation.getAncestorOf(IASTFunctionCallExpression.class, reference)) != null) {
				ICPPASTName functionName = Node.getNameForFunction(call.getFunctionNameExpression());
				if(functionName != null && functionName.resolveBinding() instanceof ICPPFunctionInstance) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isUsedInVaragsFunction(ICPPASTName name) {
		IASTName[] references = Node.getReferences(name);

		for (IASTName reference : references) {
			IASTFunctionCallExpression call;
			if ((call = Relation.getAncestorOf(IASTFunctionCallExpression.class, reference)) != null) {
				ICPPASTName functionName = Node.getNameForFunction(call.getFunctionNameExpression());
				if (functionName != null && functionName.resolveBinding() instanceof ICPPFunction
						&& ((ICPPFunction) functionName.resolveBinding()).takesVarArgs()) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isUsedInUnknownFunction(ICPPASTName name) {
		IASTName[] references = Node.getReferences(name);

		for (IASTName reference : references) {
			IASTFunctionCallExpression call;
			if ((call = Relation.getAncestorOf(IASTFunctionCallExpression.class, reference)) != null) {
				ICPPASTName functionName = Node.getNameForFunction(call.getFunctionNameExpression());
				if(functionName != null && functionName.resolveBinding() instanceof ICPPUnknownBinding) {
					return true;
				}

			}
		}

		return false;
	}
}
