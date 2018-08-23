/*******************************************************************************
 * Copyright (c) 2007-2018, IFS Institute for Software, HSR Rapperswil,
 * Switzerland, http://ifs.hsr.ch
 *
 * Permission to use, copy, and/or distribute this software for any
 * purpose without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 ******************************************************************************/

package ch.hsr.ifs.cute.headers.utils;

import java.util.Optional;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.iltis.cpp.versionator.definition.CPPVersion;

/**
 * @author Hansruedi Patzen IFS
 * @since 5.3
 * 
 */
public class VersionQuery {

	public static boolean isCPPVersionAboveOrEqualEleven(IProject project) {
		Optional<CPPVersion> cppVersion = Optional.ofNullable(CPPVersion.getForProject(project));
		return cppVersion.map(version -> version.ordinal() >= CPPVersion.CPP_11.ordinal()).orElse(true);
	}
}