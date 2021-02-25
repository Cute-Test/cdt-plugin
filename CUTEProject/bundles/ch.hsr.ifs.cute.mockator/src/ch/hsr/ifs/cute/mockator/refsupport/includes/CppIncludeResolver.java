package ch.hsr.ifs.cute.mockator.refsupport.includes;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexInclude;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.iltis.core.data.AbstractPair;
import ch.hsr.ifs.iltis.core.exception.ILTISException;


public class CppIncludeResolver {

    private static final Map<String, String> CPP_INCLUDES;

    static {
        CPP_INCLUDES = new HashMap<>();
        CPP_INCLUDES.put("assert.h", "cassert"); //$NON-NLS-2$
        CPP_INCLUDES.put("ctype.h", "cctype"); //$NON-NLS-2$
        CPP_INCLUDES.put("errno.h", "cerrno"); //$NON-NLS-2$
        CPP_INCLUDES.put("float.h", "cfloat"); //$NON-NLS-2$
        CPP_INCLUDES.put("iso646.h", "ciso646"); //$NON-NLS-2$
        CPP_INCLUDES.put("limits.h", "climits"); //$NON-NLS-2$
        CPP_INCLUDES.put("locale.h", "clocale"); //$NON-NLS-2$
        CPP_INCLUDES.put("math.h", "cmath"); //$NON-NLS-2$
        CPP_INCLUDES.put("complex.h", "ccomplex"); //$NON-NLS-2$
        CPP_INCLUDES.put("setjmp.h", "csetjmp"); //$NON-NLS-2$
        CPP_INCLUDES.put("signal.h", "csignal"); //$NON-NLS-2$
        CPP_INCLUDES.put("stdarg.h", "cstdarg"); //$NON-NLS-2$
        CPP_INCLUDES.put("stddef.h", "cstddef"); //$NON-NLS-2$
        CPP_INCLUDES.put("stdio.h", "cstdio"); //$NON-NLS-2$
        CPP_INCLUDES.put("stdlib.h", "cstdlib"); //$NON-NLS-2$
        CPP_INCLUDES.put("string.h", "cstring"); //$NON-NLS-2$
        CPP_INCLUDES.put("time.h", "ctime"); //$NON-NLS-2$
        CPP_INCLUDES.put("wchar.h", "cwchar"); //$NON-NLS-2$
        CPP_INCLUDES.put("wctype.h", "cwctype"); //$NON-NLS-2$
    }

    private final IPath     originFilePath;
    private final ICProject targetProject;
    private final IIndex    index;

    public CppIncludeResolver(final IASTTranslationUnit originTu, final ICProject targetProject, final IIndex index) {
        originFilePath = new Path(originTu.getFilePath()).removeLastSegments(1);
        this.targetProject = targetProject;
        this.index = index;
    }

    public AstIncludeNode resolveIncludeNode(final String targetIncludePath) {
        final IncludeInfo result = getIncludeInfo(targetIncludePath);
        return new AstIncludeNode(result.getIncludePath(), result.isSystemInclude());
    }

    public String resolveIncludePath(final String targetIncludePath) {
        return getIncludeInfo(targetIncludePath).getIncludePath();
    }

    private IncludeInfo getIncludeInfo(final String targetIncludePath) {
        try {
            final IIndexFile indexFile = getIndexFile(targetIncludePath, targetProject, index);

            if (indexFile != null) {
                final IIndexInclude[] includes = index.findIncludedBy(indexFile);

                if (includes.length > 0 && includes[0].isSystemInclude()) {
                    final boolean isSystemInclude = true;
                    return new IncludeInfo(toCppIncludeIfNecessary(includes[0].getFullName()), isSystemInclude);
                }
            }

            final boolean isSystemInclude = false;
            return new IncludeInfo(getBestRelativePath(targetIncludePath), isSystemInclude);
        } catch (final CoreException e) {
            throw new ILTISException(e).rethrowUnchecked();
        }
    }

    private String getBestRelativePath(final String includePath) throws CModelException {
        final IPath pathToInclude = new Path(includePath);
        IPath bestRelativePath = pathToInclude.makeRelativeTo(originFilePath);
        int minPathLength = bestRelativePath.segmentCount();

        for (final IIncludeReference ref : targetProject.getIncludeReferences()) {
            final IPath relativePath = pathToInclude.makeRelativeTo(ref.getPath());

            if (relativePath.segmentCount() < minPathLength) {
                minPathLength = relativePath.segmentCount();
                bestRelativePath = relativePath;
            }
        }

        return bestRelativePath.toString();
    }

    private static String toCppIncludeIfNecessary(final String includeCandidate) {
        final String cppInclude = CPP_INCLUDES.get(includeCandidate);
        return cppInclude != null ? cppInclude : includeCandidate;
    }

    private static IIndexFile getIndexFile(final String filePath, final ICProject project, final IIndex index) throws CoreException {
        return getIndexFile(IndexLocationFactory.getIFLExpensive(project, filePath), index);
    }

    private static IIndexFile getIndexFile(final IIndexFileLocation fileLocation, final IIndex index) throws CoreException {
        final IIndexFile[] files = index.getFiles(fileLocation);
        return files.length != 0 ? files[0] : null;
    }

    private class IncludeInfo extends AbstractPair<String, Boolean> {

        public IncludeInfo(final String first, final Boolean second) {
            super(first, second);
        }

        public String getIncludePath() {
            return first;
        }

        public boolean isSystemInclude() {
            return second;
        }

    }
}
