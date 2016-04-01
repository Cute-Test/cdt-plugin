package ch.hsr.ifs.templator.plugin.asttools.resolving;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;

public class FindAllNamesVisitor extends ASTVisitor {

	private List<IASTName> allNames = new ArrayList<>();

	public FindAllNamesVisitor() {
		super(true);
	}

	@Override
	public int visit(IASTName name) {
		allNames.add(name);
		return super.visit(name);
	}

	public List<IASTName> getAllNames() {
		return allNames;
	}
}