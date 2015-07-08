package ch.hsr.ifs.cute.elevenator.definition;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.elevenator.CPPVersion;

public interface IVersionModificationOperation {
	void perform(IProject project, CPPVersion selectedVersion);
}
