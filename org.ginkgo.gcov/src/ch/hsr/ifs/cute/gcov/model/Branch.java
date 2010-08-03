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

/**
 * @author Emanuel Graf IFS
 *
 */
public class Branch {
	
	private CoverageStatus status = CoverageStatus.Uncovered;
	private int taken;
	
	
	public Branch(int taken) {
		super();
		if(taken < 0 || taken > 100) {
			throw new IllegalArgumentException("Coverage must be between 0 and 100%"); //$NON-NLS-1$
		}
		this.taken = taken;
		switch (taken) {
		case 0:
			status = CoverageStatus.Uncovered;
			break;
		case 100:
			status = CoverageStatus.Covered;
			break;
		default:
			status = CoverageStatus.PartiallyCovered;
		}
	}


	public CoverageStatus getStatus() {
		return status;
	}


	public int getCovered() {
		return taken;
	}


	@Override
	public String toString() {
		return "Branch taken: " + taken + " " + status; //$NON-NLS-1$ //$NON-NLS-2$
	}

	

}
