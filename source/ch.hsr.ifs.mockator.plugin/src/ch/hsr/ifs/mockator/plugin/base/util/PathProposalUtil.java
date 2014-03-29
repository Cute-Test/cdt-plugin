package ch.hsr.ifs.mockator.plugin.base.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

public class PathProposalUtil {
  private final IPath path;

  public PathProposalUtil(IPath path) {
    this.path = path;
  }

  public IPath getUniquePathForNewFile(String filePrefix, String fileSuffix) {
    filePrefix = filePrefix.replaceAll("\\s", "");
    IPath proposal = path.append(filePrefix + fileSuffix);

    for (int i = 1; isFileAlreadyExisting(proposal); ++i) {
      proposal = path.append(filePrefix + i + fileSuffix);
    }

    return proposal;
  }

  private static boolean isFileAlreadyExisting(IPath proposal) {
    IFile file = ProjectUtil.getWorkspaceRoot().getFile(proposal);
    return file.getLocation().toFile().exists();
  }
}
