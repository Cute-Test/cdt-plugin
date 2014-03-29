package ch.hsr.ifs.mockator.plugin.preprocessor;

import static ch.hsr.ifs.mockator.plugin.MockatorConstants.HEADER_SUFFIX;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.MOCKED_TRACE_PREFIX;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.SOURCE_SUFFIX;
import static ch.hsr.ifs.mockator.plugin.MockatorConstants.TRACE_FOLDER;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import ch.hsr.ifs.mockator.plugin.base.util.PathProposalUtil;

class TraceFileNameCreator {
  private final String funName;
  private final IProject project;

  public TraceFileNameCreator(String funName, IProject project) {
    this.funName = funName;
    this.project = project;
  }

  public IPath getSourceFilePath() {
    return getPathForNewFile(SOURCE_SUFFIX);
  }

  public IPath getHeaderFilePath() {
    return getPathForNewFile(HEADER_SUFFIX);
  }

  private IPath getPathForNewFile(String suffix) {
    IFolder traceFolder = project.getFolder(TRACE_FOLDER);
    PathProposalUtil proposal = new PathProposalUtil(traceFolder.getFullPath());
    return proposal.getUniquePathForNewFile(MOCKED_TRACE_PREFIX + funName, suffix);
  }
}
