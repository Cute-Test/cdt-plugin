/*******************************************************************************
 * Copyright (c) 2007-2011, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/
package ch.hsr.ifs.cute.ui.project.wizard;

import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

public class CuteBuildPropertyValue implements IBuildPropertyValue{

	public String getId() {
		return ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_EXE;
	}

	
	public String getName() {
		return "Cute Project"; //$NON-NLS-1$
	}
	
}