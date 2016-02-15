package ch.hsr.ifs.cute.constificator.visitors;

import static ch.hsr.ifs.cute.constificator.core.util.ast.Relation.*;
import static ch.hsr.ifs.cute.constificator.core.util.type.Cast.*;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;

import ch.hsr.ifs.cute.constificator.checkers.MemberFunctionChecker;
import ch.hsr.ifs.cute.constificator.core.deciders.classmembers.MemberFunctionDecider;

public class MemberFunctionVisitor extends ASTVisitor {

	private final MemberFunctionChecker parent;

	public MemberFunctionVisitor(MemberFunctionChecker parent) {
		shouldVisitDeclarators = true;
		this.parent = parent;
	}

	@Override
	public int visit(IASTDeclarator declarator) {

		if (declarator instanceof ICPPASTFunctionDeclarator) {
			if (getAncestorOf(ICPPASTFunctionDefinition.class, declarator) != null) {
				parent.add(MemberFunctionDecider.canConstify(as(ICPPASTFunctionDeclarator.class, declarator)));
			}
		}

		return PROCESS_CONTINUE;
	}

}
