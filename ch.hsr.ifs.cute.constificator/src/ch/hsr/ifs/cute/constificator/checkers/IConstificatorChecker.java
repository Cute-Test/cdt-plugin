package ch.hsr.ifs.cute.constificator.checkers;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;

public interface IConstificatorChecker {

	public ASTVisitor visitor();

	public String definitiveID();

	public String informationalID();

}