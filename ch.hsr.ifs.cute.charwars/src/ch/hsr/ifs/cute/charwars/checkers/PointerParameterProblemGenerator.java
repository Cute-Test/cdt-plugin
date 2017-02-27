package ch.hsr.ifs.cute.charwars.checkers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.core.resources.IFile;

import ch.hsr.ifs.cute.charwars.asttools.ASTAnalyzer;
import ch.hsr.ifs.cute.charwars.constants.ProblemIDs;
import ch.hsr.ifs.cute.charwars.utils.analyzers.DeclaratorTypeAnalyzer;

public class PointerParameterProblemGenerator {
	public static List<ProblemReport> generate(IFile file, IASTParameterDeclaration parameterDeclaration) {
		List<ProblemReport> problemReports = new ArrayList<ProblemReport>();
		IASTDeclarator declarator = parameterDeclaration.getDeclarator();
		if(DeclaratorTypeAnalyzer.isPointer(declarator) && !DeclaratorTypeAnalyzer.isArray(declarator)
			&& ASTAnalyzer.isFunctionDefinitionParameterDeclaration(parameterDeclaration) 
			&& !DeclaratorTypeAnalyzer.hasCStringType(declarator, false)
			&& !DeclaratorTypeAnalyzer.hasCStringType(declarator, true)) {
			IASTName name = declarator.getName();
			
			ProblemReport report = ProblemReport.create(file, ProblemIDs.POINTER_PARAMETER_PROBLEM, name, name.toString());
			if(report != null) {
				problemReports.add(report);	
			}
		}
		return problemReports;
	}

}
