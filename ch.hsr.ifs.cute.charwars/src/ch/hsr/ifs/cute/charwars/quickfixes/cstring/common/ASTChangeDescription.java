package ch.hsr.ifs.cute.charwars.quickfixes.cstring.common;

import java.util.ArrayList;
import java.util.List;

public class ASTChangeDescription {
	private List<String> headersToInclude;
	private boolean removeStatement;
	private boolean statementHasChanged;
	
	public ASTChangeDescription() {
		headersToInclude = new ArrayList<String>();
		removeStatement = false;
		statementHasChanged = false;
	}
	
	public void addHeaderToInclude(String headerName) {
		headersToInclude.add(headerName);
	}
	
	public List<String> getHeadersToInclude() {
		return headersToInclude;
	}
	
	public void setRemoveStatement(boolean removeStatement) {
		this.removeStatement = removeStatement;
	}
	
	public boolean shouldRemoveStatement() {
		return removeStatement;
	}
	
	public void setStatementHasChanged(boolean statementHasChanged) {
		this.statementHasChanged = statementHasChanged;
	}
	
	public boolean statementHasChanged() {
		return statementHasChanged;
	}
}