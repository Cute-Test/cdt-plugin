package ch.hsr.ifs.cute.constificator.core.deciders.decision;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.constificator.core.util.type.Truelean;

public class NullDecision implements IDecision {

	@Override
	public String note() {
		return "";
	}

	@Override
	public IASTNode node() {
		return null;
	}

	@Override
	public IASTName name() {
		return null;
	}

	@Override
	public void decide(Truelean decision) {
	}

	@Override
	public Truelean get() {
		return null;
	}


}
