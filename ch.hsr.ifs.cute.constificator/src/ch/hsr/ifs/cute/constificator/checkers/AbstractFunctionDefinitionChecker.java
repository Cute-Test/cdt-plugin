package ch.hsr.ifs.cute.constificator.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.codan.core.cxx.model.AbstractAstFunctionChecker;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.cute.constificator.core.deciders.decision.IDecision;
import ch.hsr.ifs.cute.constificator.core.deciders.decision.NullDecision;
import ch.hsr.ifs.cute.constificator.core.util.type.Truelean;

public abstract class AbstractFunctionDefinitionChecker extends AbstractAstFunctionChecker
		implements IChecker, IConstificatorChecker {

	private final List<IDecision> decisions = new ArrayList<>();

	@Override
	protected void processFunction(IASTFunctionDefinition definition) {
		decisions.clear();
		definition.accept(visitor());
		report();
	}

	public void add(IDecision decision) {
		decisions.add(decision);
	}

	private void report() {
		for (IDecision decision : decisions) {
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

					ProblemReport report = new ProblemReport(this.getFile(), problem, node, "", location.getNodeLength());
					String problemID = report.getProblemID();
					IProblemLocation problemLocation = report.getProblemLocation();
					Object[] args = report.getArgs();
					reportProblem(problemID, problemLocation, args);
				}
			}
		}
	}

}
