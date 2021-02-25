package ch.hsr.ifs.cute.mockator.base.util;

import org.eclipse.core.runtime.IPath;

import ch.hsr.ifs.iltis.core.resources.FileUtil;


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
        return FileUtil.toIFile(proposal).exists();
    }
}
