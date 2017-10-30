package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.runconfig;

import java.util.Map;

import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;


class MacOsXPreloadRunConfig extends AbstractPreloadRunStrategy {

   private static String DYLD_FORCE_FLAT_NAMESPACE = "DYLD_FORCE_FLAT_NAMESPACE";
   private static String DYLD_INSERT_LIBRARIES     = "DYLD_INSERT_LIBRARIES";
   private static String DYLD_LIBRARY_PATH         = "DYLD_LIBRARY_PATH";

   @Override
   public boolean hasPreloadConfig(String sharedLibPath, Map<String, String> envVariables) {
      String ldPreloadLibs = envVariables.get(DYLD_INSERT_LIBRARIES);
      String dyldLibPath = envVariables.get(DYLD_LIBRARY_PATH);

      if (ldPreloadLibs == null || dyldLibPath == null) return false;

      return ldPreloadLibs.contains(sharedLibPath) && dyldLibPath.contains(FileUtil.removeFilePart(sharedLibPath));
   }

   @Override
   public void addPreloadConfig(String sharedLibPath, Map<String, String> envVariables) {
      forceFlatNamespace(envVariables);
      insertLibrary(sharedLibPath, envVariables);
      putLibInPathIfNecessary(sharedLibPath, envVariables);
   }

   @Override
   public void removePreloadConfig(String sharedLibPath, Map<String, String> envVariables) {
      removeEntryFromEnv(sharedLibPath, DYLD_INSERT_LIBRARIES, envVariables);
   }

   private void putLibInPathIfNecessary(String pathToShLib, Map<String, String> envVariables) {
      String dyldLibPath = envVariables.get(DYLD_LIBRARY_PATH);
      String path = FileUtil.removeFilePart(pathToShLib);
      envVariables.put(DYLD_LIBRARY_PATH, appendToList(dyldLibPath, path));
   }

   private void insertLibrary(String pathToShLib, Map<String, String> envVariables) {
      String dyldInsertLibs = envVariables.get(DYLD_INSERT_LIBRARIES);
      envVariables.put(DYLD_INSERT_LIBRARIES, appendToList(dyldInsertLibs, pathToShLib));
   }

   private static void forceFlatNamespace(Map<String, String> envVariables) {
      envVariables.put(DYLD_FORCE_FLAT_NAMESPACE, "1");
   }
}
