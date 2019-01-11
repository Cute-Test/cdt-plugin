package ch.hsr.ifs.cute.mockator.preprocessor;

import static ch.hsr.ifs.cute.mockator.MockatorConstants.HEADER_SUFFIX;
import static ch.hsr.ifs.cute.mockator.MockatorConstants.MOCKED_TRACE_PREFIX;
import static ch.hsr.ifs.cute.mockator.MockatorConstants.SOURCE_SUFFIX;
import static ch.hsr.ifs.cute.mockator.MockatorConstants.TRACE_FOLDER;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import ch.hsr.ifs.cute.mockator.base.util.PathProposalUtil;


class TraceFileNameCreator {

    private final String   funName;
    private final IProject project;

    public TraceFileNameCreator(final String funName, final IProject project) {
        this.funName = funName;
        this.project = project;
    }

    public IPath getSourceFilePath() {
        return getPathForNewFile(SOURCE_SUFFIX);
    }

    public IPath getHeaderFilePath() {
        return getPathForNewFile(HEADER_SUFFIX);
    }

    private IPath getPathForNewFile(final String suffix) {
        final IFolder traceFolder = project.getFolder(TRACE_FOLDER);
        final PathProposalUtil proposal = new PathProposalUtil(traceFolder.getFullPath());
        return proposal.getUniquePathForNewFile(MOCKED_TRACE_PREFIX + funName, suffix);
    }
}
