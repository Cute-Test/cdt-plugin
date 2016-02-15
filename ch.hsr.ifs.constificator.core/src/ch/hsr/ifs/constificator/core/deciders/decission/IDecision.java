package ch.hsr.ifs.constificator.core.deciders.decission;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import ch.hsr.ifs.constificator.core.util.type.Truelean;

public interface IDecision {

	/**
	 * @return The note associated with this decision
	 */
	public String note();

	/**
	 * @return Which AST node this decision describes
	 */
	public IASTNode node();

	/**
	 * @return The AST name associated with this decision
	 */
	public IASTName name();

	/**
	 * @param decision One of @{link Truelean#YES}, @{link Truelean#NO}, @{link Truelean#MAYBE}
	 */
	public void decide(Truelean decision);

	/**
	 * @return The @{link Truelean} value of the decision
	 */
	public Truelean get();

}
