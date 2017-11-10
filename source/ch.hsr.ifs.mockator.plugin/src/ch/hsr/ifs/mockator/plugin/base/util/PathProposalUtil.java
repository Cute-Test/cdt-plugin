package ch.hsr.ifs.mockator.plugin.base.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

import ch.hsr.ifs.iltis.cpp.resources.CProjectUtil;


public class PathProposalUtil {

   private final IPath path;

   public PathProposalUtil(final IPath path) {
      this.path = path;
   }

   public IPath getUniquePathForNewFile(String filePrefix, final String fileSuffix) {
      filePrefix = filePrefix.replaceAll("\\s", "");
      IPath proposal = path.append(filePrefix + fileSuffix);

      for (int i = 1; isFileAlreadyExisting(proposal); ++i) {
         proposal = path.append(filePrefix + i + fileSuffix);
      }

      return proposal;
   }

   private static boolean isFileAlreadyExisting(final IPath proposal) {
      final IFile file = CProjectUtil.getWorkspaceRoot().getFile(proposal);
      return file.getLocation().toFile().exists();
   }
}
