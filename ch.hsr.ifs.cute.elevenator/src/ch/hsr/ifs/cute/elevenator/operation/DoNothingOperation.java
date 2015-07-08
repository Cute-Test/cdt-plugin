package ch.hsr.ifs.cute.elevenator.operation;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.cute.elevenator.definition.CPPVersion;
import ch.hsr.ifs.cute.elevenator.definition.IVersionModificationOperation;

public class DoNothingOperation implements IVersionModificationOperation {

	@Override
	public void perform(IProject project, CPPVersion version) {
	}

}
