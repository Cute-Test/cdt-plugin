package ch.hsr.ifs.mockator.plugin.refsupport.includes;

import static org.eclipse.cdt.ui.PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME;
import static org.eclipse.cdt.ui.PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_NAME;

import java.util.UUID;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ISourceRoot;
import org.eclipse.cdt.internal.core.dom.rewrite.ASTLiteralNode;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.utils.PathUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

import ch.hsr.ifs.iltis.core.exception.ILTISException;

import ch.hsr.ifs.mockator.plugin.MockatorConstants;


@SuppressWarnings("restriction")
public class IncludeGuardCreator {

   private final IResource file;
   private final ICProject cProject;
   private final String    includeGuardSymbol;

   public IncludeGuardCreator(final IResource file, final ICProject cProject) {
      this.file = file;
      this.cProject = cProject;
      includeGuardSymbol = generateIncludeGuardSymbol();
   }

   public ASTLiteralNode createIfNDef() {
      return new ASTLiteralNode(MockatorConstants.IFNDEF_DIRECTIVE + MockatorConstants.SPACE + includeGuardSymbol);
   }

   public ASTLiteralNode createDefine() {
      return new ASTLiteralNode(MockatorConstants.DEFINE_DIRECTIVE + MockatorConstants.SPACE + includeGuardSymbol);
   }

   public ASTLiteralNode createEndIf() {
      return new ASTLiteralNode(MockatorConstants.END_IF_DIRECTIVE);
   }

   private String generateIncludeGuardSymbol() {
      switch (getIncludeGuardScheme()) {
      case PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_PATH:
         return createIncludeGuardSymbolFromFileName(getFilePath().toString());
      case PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_NAME:
         return createIncludeGuardSymbolFromFileName(file.getName());
      case PreferenceConstants.CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_UUID:
         return createIncludeGuardSymbolFromUUID();
      default:
         throw new ILTISException("Unknown include guard scheme").rethrowUnchecked();
      }
   }

   private IPath getFilePath() {
      IPath path = file.getFullPath();
      final ISourceRoot root = cProject.findSourceRoot(file);

      if (root != null) {
         path = PathUtil.makeRelativePath(path, root.getPath());
      }

      return path;
   }

   private int getIncludeGuardScheme() {
      final int scheme = PreferenceConstants.getPreference(CODE_TEMPLATES_INCLUDE_GUARD_SCHEME, cProject,
               CODE_TEMPLATES_INCLUDE_GUARD_SCHEME_FILE_NAME);
      return scheme;
   }

   private static String createIncludeGuardSymbolFromFileName(final String fileName) {
      final StringBuilder includeGuard = new StringBuilder(fileName.length() + 1);

      for (int i = 0; i < fileName.length(); ++i) {
         final char ch = fileName.charAt(i);

         if (Character.isLetterOrDigit(ch)) {
            includeGuard.append(Character.toUpperCase(ch));
         } else if (includeGuard.length() > 0) {
            includeGuard.append('_');
         }
      }

      includeGuard.append('_');
      return includeGuard.toString();
   }

   private static String createIncludeGuardSymbolFromUUID() {
      final String uuid = generateRandomUuid();
      final StringBuilder includeGuard = new StringBuilder();
      includeGuard.append('H');

      for (int i = 0; i < uuid.length(); ++i) {
         final char ch = uuid.charAt(i);

         if (Character.isLetterOrDigit(ch)) {
            includeGuard.append(Character.toUpperCase(ch));
         } else {
            includeGuard.append('_');
         }
      }

      return includeGuard.toString();
   }

   private static String generateRandomUuid() {
      return UUID.randomUUID().toString();
   }
}
