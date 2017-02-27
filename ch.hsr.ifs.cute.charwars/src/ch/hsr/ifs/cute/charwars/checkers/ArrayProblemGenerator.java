package ch.hsr.ifs.cute.charwars.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.core.resources.IFile;

import ch.hsr.ifs.cute.charwars.asttools.DeclaratorAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.utils.analyzers.DeclaratorTypeAnalyzer;

public class ArrayProblemGenerator {
	public static List<ProblemReport> generate(IFile file, IASTNode node) {
		List<ProblemReport> problemReports = new ArrayList<ProblemReport>();
		if(node instanceof IASTSimpleDeclaration) {
			IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration)node;
			for(IASTDeclarator declarator : simpleDeclaration.getDeclarators()) {
				if(DeclaratorTypeAnalyzer.isArray(declarator) && !DeclaratorAnalyzer.isCString(declarator)) {
					ProblemReport report = ProblemReport.create(file, ProblemIDs.ARRAY_PROBLEM, declarator);
					if(report != null) {
						problemReports.add(report);	
					}
				}
			}
		}
		return problemReports;
	}
}
