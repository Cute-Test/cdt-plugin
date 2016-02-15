package ch.hsr.ifs.constificator.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.ICheckerWithPreferences;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;

import ch.hsr.ifs.constificator.core.deciders.decission.IDecision;
import ch.hsr.ifs.constificator.core.deciders.decission.NullDecision;
import ch.hsr.ifs.constificator.core.util.type.Truelean;

public abstract class AbstractClassDefinitionChecker extends AbstractIndexAstChecker
		implements ICheckerWithPreferences, IConstificatorChecker {

	private List<IDecision> decisions = new ArrayList<>();

	@Override
	public final void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof ICPPASTFunctionDefinition) {
					ICPPASTFunctionDefinition definition = (ICPPASTFunctionDefinition) declaration;
					IASTFunctionDeclarator declarator = definition.getDeclarator();
					IBinding binding = declarator.getName().resolveBinding();

					if (binding instanceof ICPPMethod) {
						processMember(declaration);
					}
				} else if (declaration instanceof IASTSimpleDeclaration) {
					IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) declaration;
					IASTDeclarator[] declarators = simpleDecl.getDeclarators();

					if(simpleDecl.getDeclSpecifier() instanceof ICPPASTCompositeTypeSpecifier) {
						return PROCESS_CONTINUE;
					}

					if (declarators.length != 1) {
						return PROCESS_CONTINUE;
					} else {
						IBinding binding = declarators[0].getName().resolveBinding();

						if (binding instanceof ICPPField) {
							processMember(declaration);
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}

	private final void processMember(IASTDeclaration member) {
		decisions.clear();
		member.accept(visitor());
		report();
	}

	public void add(IDecision decision) {
		decisions.add(decision);
	}

	private void report() {
		for (IDecision decision : decisions) {
			if (decision instanceof NullDecision) {
				continue;
			}

			IASTNode node = decision.node();
			IASTFileLocation location = node.getFileLocation();

			if (location != null && decision.get() != Truelean.NO) {

				String problem;
				if (decision.get() == Truelean.MAYBE) {
					problem = informationalID();
				} else {
					problem = definitiveID();
				}

				reportProblem(problem, node, "", location.getNodeLength(), decision.note());
			}
		}
	}

}
