package ch.hsr.ifs.constificator.visitors;

import static ch.hsr.ifs.constificator.core.util.trait.Types.*;
import static ch.hsr.ifs.constificator.core.util.type.Cast.*;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;

import ch.hsr.ifs.constificator.checkers.LocalVariablesChecker;
import ch.hsr.ifs.constificator.core.deciders.decission.IDecision;
import ch.hsr.ifs.constificator.core.deciders.localvariables.NonPointerVariable;
import ch.hsr.ifs.constificator.core.deciders.localvariables.PointerVariable;

public class LocalVariablesVisitor extends ASTVisitor {

	private final LocalVariablesChecker parent;

	public LocalVariablesVisitor(LocalVariablesChecker parent) {
		shouldVisitDeclarators = true;
		this.parent = parent;
	}

	@Override
	public int visit(IASTDeclarator declarator) {
		ICPPASTDeclarator cppDeclarator = as(ICPPASTDeclarator.class, declarator);

		if (cppDeclarator == null) {
			return PROCESS_SKIP;
		}

		if (cppDeclarator instanceof ICPPASTFunctionDeclarator) {
			return PROCESS_CONTINUE;
		}

		IASTNodeLocation[] locations = cppDeclarator.getName().getNodeLocations();
		if(locations.length == 1 && locations[0] instanceof IASTMacroExpansionLocation) {
			return PROCESS_SKIP;
		}

		if (cppDeclarator.getParent() instanceof IASTSimpleDeclaration) {
			if(((IASTSimpleDeclaration)cppDeclarator.getParent()).getDeclarators().length > 1) {
				return PROCESS_CONTINUE;
			}

			ICPPVariable variable;
			if((variable = as(ICPPVariable.class, cppDeclarator.getName().resolveBinding())) == null) {
				return PROCESS_CONTINUE;
			}

			if (pointerLevels(variable.getType()) == 0 && !isReference(variable.getType())) {
				parent.add(NonPointerVariable.canConstify(cppDeclarator));
			} else {
				List<IDecision> decisions = PointerVariable.canConstify(cppDeclarator);
				for(IDecision decision : decisions) {
					parent.add(decision);
				}
			}
		}

		return PROCESS_CONTINUE;
	}

}
