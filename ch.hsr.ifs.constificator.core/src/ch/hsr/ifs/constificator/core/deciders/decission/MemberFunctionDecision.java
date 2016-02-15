package ch.hsr.ifs.constificator.core.deciders.decission;

import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.constificator.core.util.type.Truelean;

public class MemberFunctionDecision implements IDecision {
	private final IASTFunctionDeclarator m_declarator;
	private Truelean m_decision = Truelean.NO;
	private boolean m_shadows = false;

	public MemberFunctionDecision(IASTFunctionDeclarator function) {
		m_declarator = function;
	}

	@Override
	public IASTName name() {
		return m_declarator.getName();
	}

	@Override
	public String note() {
		String note = "";

		if (m_decision == Truelean.MAYBE) {
			note = String.format("Adding const qualification will %s '%s' in at least one of its base classes",
					m_shadows ? "shadow" : "override", name().toString());
		}

		return note;
	}

	@Override
	public IASTNode node() {
		return m_declarator;
	}

	@Override
	public void decide(Truelean decision) {
		m_decision = decision;
	}

	@Override
	public Truelean get() {
		return m_decision;
	}

	public void setShadows(boolean shadows) {
		m_shadows = shadows;
	}


}
