package ch.hsr.ifs.cute.constificator.core.deciders.decision;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.constificator.core.util.type.Truelean;

public class MemberFunctionDecision implements IDecision {
	private final IASTFunctionDeclarator declarator;
	private Truelean decision = Truelean.NO;
	private boolean shadows = false;

	public MemberFunctionDecision(IASTFunctionDeclarator function) {
		declarator = function;
	}

	@Override
	public IASTName name() {
		return declarator.getName();
	}

	@Override
	public String note() {
		String note = "";

		if (decision == Truelean.MAYBE) {
			note = String.format("Adding const qualification will %s '%s' in at least one of its base classes",
					shadows ? "shadow" : "override", name().toString());
		}

		return note;
	}

	@Override
	public IASTNode node() {
		return declarator;
	}

	@Override
	public void decide(Truelean decision) {
		this.decision = decision;
	}

	@Override
	public Truelean get() {
		return decision;
	}

	public void setShadows(boolean shadows) {
		this.shadows = shadows;
	}


}
