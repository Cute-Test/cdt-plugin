package ch.hsr.ifs.constificator.core.deciders.decission;

import static ch.hsr.ifs.constificator.core.util.ast.Relation.*;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;

import ch.hsr.ifs.constificator.core.util.type.Truelean;

public class NodeDecision implements IDecision {

	private final IASTNode m_node;
	private Truelean m_decision = Truelean.NO;

	public NodeDecision(IASTNode node) {
		m_node = node;
	}

	@Override
	public String note() {
		return null;
	}

	@Override
	public IASTNode node() {
		return m_node;
	}

	@Override
	public IASTName name() {
		if (!(m_node instanceof ICPPASTDeclarator)) {
			ICPPASTDeclarator declarator;
			if ((declarator = getAncestorOf(ICPPASTDeclarator.class, m_node)) == null) {
				return null;
			} else {
				return declarator.getName();
			}
		} else {
			return ((ICPPASTDeclarator) m_node).getName();
		}
	}

	@Override
	public void decide(Truelean decision) {
		m_decision = decision;
	}

	@Override
	public Truelean get() {
		return m_decision;
	}

}
