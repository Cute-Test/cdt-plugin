/*******************************************************************************
 * Copyright (c) 2010 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 * Institute for Software (IFS)- initial API and implementation 
 ******************************************************************************/
package ch.hsr.ifs.cute.gcov.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Emanuel Graf IFS
 *
 */
public class Line {
	
	private Function function;
	private int nr;
	private CoverageStatus status;
	private List<Branch>branches;
	
	public Line(int nr, CoverageStatus status) {
		super();
		this.nr = nr;
		this.status = status;
	}
	
	public void addBranch(Branch b) {
		if(branches == null){
			branches = new ArrayList<Branch>();
		}
		branches.add(b);
		if(status == CoverageStatus.Covered && b.getStatus() == CoverageStatus.Uncovered) {
			status = CoverageStatus.PartiallyCovered;
		}
	}

	public Function getFunction() {
		return function;
	}

	public void setFunction(Function function) {
		this.function = function;
	}

	public int getNr() {
		return nr;
	}

	public CoverageStatus getStatus() {
		return status;
	}

	public List<Branch> getBranches() {
		if(branches == null) {
			return Collections.emptyList();
		}
		return branches;
	}

	@Override
	public String toString() {
		return nr + " " + status; //$NON-NLS-1$
	}
	
	
	
	

}
