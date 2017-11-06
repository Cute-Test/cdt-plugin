package ch.hsr.ifs.mockator.plugin.base.util;

import java.io.File;
import java.net.URI;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import ch.hsr.ifs.iltis.cpp.resources.CPPResourceHelper;

import ch.hsr.ifs.mockator.plugin.base.dbc.Assert;


public abstract class FileUtil {

   public static IFile toIFile(final String filePath) {
      return toIFile(new File(filePath).toURI());
   }

   private static IFile toIFile(final URI fileURI) {
      final IFile[] files = CPPResourceHelper.getWorkspaceRoot().findFilesForLocationURI(fileURI);

      if (files.length == 1) return files[0];

      for (final IFile file : files) {
         if (fileURI.getPath().endsWith(file.getFullPath().toString())) return file;
      }

      return null;
   }

   public static IFile toIFile(final IPath filePath) {
      return CPPResourceHelper.getWorkspaceRoot().getFile(filePath);
   }

   public static String getFilenameWithoutExtension(final String filename) {
      final int begin = filename.lastIndexOf(PlatformUtil.PATH_SEGMENT_SEPARATOR) + 1;
      int end = filename.lastIndexOf('.');
      if (end < 0) {
         end = filename.length();
      }
      return filename.substring(begin, end);
   }

   public static IFile getFile(final IASTNode node) {
      return CPPResourceHelper.getWorkspaceRoot().getFileForLocation(new Path(node.getFileLocation().getFileName()));
   }

   public static IASTFileLocation getNodeFileLocation(final IASTNode node) {
      return node.getFileLocation() != null ? node.getFileLocation() : getNodeFileLocation(node.getParent());
   }

   public static String getFilePart(final String filePath) {
      final Path path = new Path(filePath);
      Assert.isTrue(path.segmentCount() > 0, "Path elements must not be empty");
      return path.segment(path.segmentCount() - 1);
   }

   public static Path getPath(final IFile file) {
      final String pathOfFile = file.getFullPath().toOSString();
      return new Path(removeFilePart(pathOfFile));
   }

   public static String removeFilePart(final String filePath) {
      return filePath.replaceAll("(\\w)*\\.(\\w)*", "");
   }

   public static URI stringToUri(final String fileString) {
      return new File(fileString).toURI();
   }
}
