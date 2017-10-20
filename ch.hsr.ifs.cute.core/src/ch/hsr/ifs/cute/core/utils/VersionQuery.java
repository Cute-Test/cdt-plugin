/*******************************************************************************
 * Copyright (c) 2007-2017, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 * 
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/

package ch.hsr.ifs.cute.core.utils;

import org.eclipse.core.resources.IProject;

import com.cevelop.elevenator.definition.CPPVersion;

/**
 * @author Hansruedi Patzen IFS
 * @since 5.3
 * 
 */
public class VersionQuery {

	public static boolean isCPPVersionAboveOrEqualEleven(IProject project) {
		CPPVersion version = CPPVersion.getForProject(project);
		return version != CPPVersion.CPP_98 && version != CPPVersion.CPP_03;
	}
}
