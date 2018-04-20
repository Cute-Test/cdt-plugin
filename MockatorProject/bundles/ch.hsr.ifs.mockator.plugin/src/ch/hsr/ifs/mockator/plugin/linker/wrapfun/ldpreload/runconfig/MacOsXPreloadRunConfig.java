package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.runconfig;

import java.util.Map;

import ch.hsr.ifs.iltis.core.core.resources.FileUtil;


class MacOsXPreloadRunConfig extends AbstractPreloadRunStrategy {

   private static String DYLD_FORCE_FLAT_NAMESPACE = "DYLD_FORCE_FLAT_NAMESPACE";
   private static String DYLD_INSERT_LIBRARIES     = "DYLD_INSERT_LIBRARIES";
   private static String DYLD_LIBRARY_PATH         = "DYLD_LIBRARY_PATH";

   @Override
   public boolean hasPreloadConfig(final String sharedLibPath, final Map<String, String> envVariables) {
      final String ldPreloadLibs = envVariables.get(DYLD_INSERT_LIBRARIES);
      final String dyldLibPath = envVariables.get(DYLD_LIBRARY_PATH);

      if (ldPreloadLibs == null || dyldLibPath == null) { return false; }

      return ldPreloadLibs.contains(sharedLibPath) && dyldLibPath.contains(FileUtil.getPathWithoutFilename(sharedLibPath));
   }

   @Override
   public void addPreloadConfig(final String sharedLibPath, final Map<String, String> envVariables) {
      forceFlatNamespace(envVariables);
      insertLibrary(sharedLibPath, envVariables);
      putLibInPathIfNecessary(sharedLibPath, envVariables);
   }

   @Override
   public void removePreloadConfig(final String sharedLibPath, final Map<String, String> envVariables) {
      removeEntryFromEnv(sharedLibPath, DYLD_INSERT_LIBRARIES, envVariables);
   }

   private void putLibInPathIfNecessary(final String pathToShLib, final Map<String, String> envVariables) {
      final String dyldLibPath = envVariables.get(DYLD_LIBRARY_PATH);
      final String path = FileUtil.getPathWithoutFilename(pathToShLib);
      envVariables.put(DYLD_LIBRARY_PATH, appendToList(dyldLibPath, path));
   }

   private void insertLibrary(final String pathToShLib, final Map<String, String> envVariables) {
      final String dyldInsertLibs = envVariables.get(DYLD_INSERT_LIBRARIES);
      envVariables.put(DYLD_INSERT_LIBRARIES, appendToList(dyldInsertLibs, pathToShLib));
   }

   private static void forceFlatNamespace(final Map<String, String> envVariables) {
      envVariables.put(DYLD_FORCE_FLAT_NAMESPACE, "1");
   }
}
