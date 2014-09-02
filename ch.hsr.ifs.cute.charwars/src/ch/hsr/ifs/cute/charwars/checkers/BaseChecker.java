package ch.hsr.ifs.cute.charwars.checkers;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public abstract class BaseChecker extends AbstractIndexAstChecker {
	protected ASTVisitor astVisitor = null;
	
	protected void reportProblemForDeclarator(String problemID, IASTDeclarator declarator) {
		IASTName name = declarator.getName();
		reportProblemForNode(problemID, name, name.toString());
	}
	
	protected void reportProblemForNode(String problemID, IASTNode node, String messagePatternArg) {
		IASTNodeLocation[] nodeLocations = node.getNodeLocations();
		IASTNodeLocation firstLoc = nodeLocations[0];
		
		int start = firstLoc.getNodeOffset();
		int end = firstLoc.getNodeOffset() + node.getRawSignature().length();
		int line = firstLoc.asFileLocation().getStartingLineNumber();
	
		IProblemLocationFactory problemLocationFactory = CodanRuntime.getInstance().getProblemLocationFactory();
		IProblemLocation problemLocation = problemLocationFactory.createProblemLocation(getFile(), start, end, line);
		reportProblem(problemID, problemLocation, messagePatternArg);
	}
	
	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(this.astVisitor);
	}
}
