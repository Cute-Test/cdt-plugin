package ch.hsr.ifs.cute.charwars.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.core.resources.IFile;

import ch.hsr.ifs.cute.charwars.asttools.DeclaratorAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;

public class CStringProblemGenerator {
	public static List<ProblemReport> generate(IFile file, IASTNode node) {
		List<ProblemReport> problemReports = new ArrayList<ProblemReport>();
		if(node instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) node;
			for(IASTDeclarator declarator : simpleDeclaration.getDeclarators()) {
				if(DeclaratorAnalyzer.isCString(declarator)) {
					IASTName name = declarator.getName();
					problemReports.add(new ProblemReport(file, ProblemIDs.C_STRING_PROBLEM, name, name.toString()));
				}
			}
		}
		return problemReports;
	}
}
