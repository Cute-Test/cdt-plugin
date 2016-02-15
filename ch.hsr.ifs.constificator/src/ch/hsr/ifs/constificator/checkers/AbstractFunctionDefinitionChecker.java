package ch.hsr.ifs.constificator.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.codan.core.cxx.model.AbstractAstFunctionChecker;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.constificator.core.deciders.decission.IDecision;
import ch.hsr.ifs.constificator.core.deciders.decission.NullDecision;
import ch.hsr.ifs.constificator.core.util.type.Truelean;

public abstract class AbstractFunctionDefinitionChecker extends AbstractAstFunctionChecker
		implements IChecker, IConstificatorChecker {

	private final List<IDecision> m_decisions = new ArrayList<>();

	@Override
	protected void processFunction(IASTFunctionDefinition definition) {
		m_decisions.clear();
		definition.accept(visitor());
		report();
	}

	public void add(IDecision decision) {
		m_decisions.add(decision);
	}

	private void report() {
		for (IDecision decision : m_decisions) {
			if (!(decision instanceof NullDecision)) {
				IASTNode node = decision.node();
				IASTFileLocation location = node.getFileLocation();

				if (location != null && decision.get() != Truelean.NO) {
					String problem;
					if (decision.get() == Truelean.MAYBE) {
						problem = informationalID();
					} else {
						problem = definitiveID();
					}

					reportProblem(problem, node, "", location.getNodeLength());
				}
			}
		}
	}

}
