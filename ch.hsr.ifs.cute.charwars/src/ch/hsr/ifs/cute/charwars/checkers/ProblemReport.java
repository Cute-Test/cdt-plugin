package ch.hsr.ifs.cute.charwars.checkers;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.core.resources.IFile;

public class ProblemReport {
	private String problemID;
	private IProblemLocation problemLocation;
	private Object[] args;
	
	public ProblemReport(IFile file, String problemID, IASTNode markedNode, Object... args) {
		this.problemID = problemID;
		this.problemLocation = createProblemLocation(file, markedNode);
		this.args = args;
	}
	
	private IProblemLocation createProblemLocation(IFile file, IASTNode markedNode) {
		IASTNodeLocation[] nodeLocations = markedNode.getNodeLocations();
		IASTNodeLocation firstLoc = nodeLocations[0];
		
		int start = firstLoc.getNodeOffset();
		int end = firstLoc.getNodeOffset() + markedNode.getRawSignature().length();
		int line = firstLoc.asFileLocation().getStartingLineNumber();
	
		IProblemLocationFactory problemLocationFactory = CodanRuntime.getInstance().getProblemLocationFactory();
		return problemLocationFactory.createProblemLocation(file, start, end, line);	
	}
	
	public String getProblemID() { return problemID; }
	public IProblemLocation getProblemLocation() { return problemLocation; }
	public Object[] getArgs() { return args; }
}
