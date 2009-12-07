package org.ginkgo.gcov.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.ginkgo.gcov.model.CoverageData;

public interface ICoverageListener {

	public abstract void addCoverageData(IProject project, CoverageData cov)
			throws CoreException;

}