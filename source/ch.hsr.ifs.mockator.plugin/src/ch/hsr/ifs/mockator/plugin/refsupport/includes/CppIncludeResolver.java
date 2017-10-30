package ch.hsr.ifs.mockator.plugin.refsupport.includes;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.unorderedMap;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._1;
import static ch.hsr.ifs.mockator.plugin.base.tuples.Tuple._2;

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

import ch.hsr.ifs.mockator.plugin.base.MockatorException;
import ch.hsr.ifs.mockator.plugin.base.tuples.Pair;
import ch.hsr.ifs.mockator.plugin.base.tuples.Tuple;


public class CppIncludeResolver {

   private static final Map<String, String> CPP_INCLUDES;

   static {
      CPP_INCLUDES = unorderedMap();
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

   public CppIncludeResolver(IASTTranslationUnit originTu, ICProject targetProject, IIndex index) {
      this.originFilePath = new Path(originTu.getFilePath()).removeLastSegments(1);
      this.targetProject = targetProject;
      this.index = index;
   }

   public AstIncludeNode resolveIncludeNode(String targetIncludePath) {
      Pair<String, Boolean> result = getIncludePath(targetIncludePath);
      return new AstIncludeNode(_1(result), _2(result));
   }

   public String resolveIncludePath(String targetIncludePath) {
      return _1(getIncludePath(targetIncludePath));
   }

   private Pair<String, Boolean> getIncludePath(String targetIncludePath) {
      try {
         IIndexFile indexFile = getIndexFile(targetIncludePath, targetProject, index);

         if (indexFile != null) {
            IIndexInclude[] includes = index.findIncludedBy(indexFile);

            if (includes.length > 0 && includes[0].isSystemInclude()) {
               final boolean isSystemInclude = true;
               return Tuple.from(toCppIncludeIfNecessary(includes[0].getFullName()), isSystemInclude);
            }
         }

         final boolean isSystemInclude = false;
         return Tuple.from(getBestRelativePath(targetIncludePath), isSystemInclude);
      }
      catch (CoreException e) {
         throw new MockatorException(e);
      }
   }

   private String getBestRelativePath(String includePath) throws CModelException {
      IPath pathToInclude = new Path(includePath);
      IPath bestRelativePath = pathToInclude.makeRelativeTo(originFilePath);
      int minPathLength = bestRelativePath.segmentCount();

      for (IIncludeReference ref : targetProject.getIncludeReferences()) {
         IPath relativePath = pathToInclude.makeRelativeTo(ref.getPath());

         if (relativePath.segmentCount() < minPathLength) {
            minPathLength = relativePath.segmentCount();
            bestRelativePath = relativePath;
         }
      }

      return bestRelativePath.toString();
   }

   private static String toCppIncludeIfNecessary(String includeCandidate) {
      String cppInclude = CPP_INCLUDES.get(includeCandidate);
      return cppInclude != null ? cppInclude : includeCandidate;
   }

   private static IIndexFile getIndexFile(String filePath, ICProject project, IIndex index) throws CoreException {
      return getIndexFile(IndexLocationFactory.getIFLExpensive(project, filePath), index);
   }

   private static IIndexFile getIndexFile(IIndexFileLocation fileLocation, IIndex index) throws CoreException {
      IIndexFile[] files = index.getFiles(fileLocation);
      return files.length != 0 ? files[0] : null;
   }
}
