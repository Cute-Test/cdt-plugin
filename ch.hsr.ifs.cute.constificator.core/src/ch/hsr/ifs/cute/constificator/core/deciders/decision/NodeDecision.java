package ch.hsr.ifs.cute.constificator.core.deciders.decision;

import static ch.hsr.ifs.cute.constificator.core.util.ast.Relation.*;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;

import ch.hsr.ifs.cute.constificator.core.util.type.Truelean;

public class NodeDecision implements IDecision {

	private final IASTNode node;
	private Truelean decision = Truelean.NO;

	public NodeDecision(IASTNode node) {
		this.node = node;
	}

	@Override
	public String note() {
		return null;
	}

	@Override
	public IASTNode node() {
		return node;
	}

	@Override
	public IASTName name() {
		if (!(node instanceof ICPPASTDeclarator)) {
			ICPPASTDeclarator declarator;
			if ((declarator = getAncestorOf(ICPPASTDeclarator.class, node)) == null) {
				return null;
			} else {
				return declarator.getName();
			}
		} else {
			return ((ICPPASTDeclarator) node).getName();
		}
	}

	@Override
	public void decide(Truelean decision) {
		this.decision = decision;
	}

	@Override
	public Truelean get() {
		return decision;
	}

}
