package org.ginkgo.gcov.parser;

import org.eclipse.core.resources.IFile;

public interface IParser {

	public abstract void parse(IFile file);

}