package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.runconfig;

import java.util.Map;

import ch.hsr.ifs.mockator.plugin.base.util.FileUtil;


class LinuxPreloadRunConfig extends AbstractPreloadRunStrategy {

   private static final String LD_PRELOAD      = "LD_PRELOAD";
   private static final String LD_LIBRARY_PATH = "LD_LIBRARY_PATH";

   private void putLibInPathIfNecessary(String pathToShLib, Map<String, String> envVariables) {
      String ldLibraryPath = envVariables.get(LD_LIBRARY_PATH);
      String path = FileUtil.removeFilePart(pathToShLib);
      envVariables.put(LD_LIBRARY_PATH, appendToList(ldLibraryPath, path));
   }

   @Override
   public boolean hasPreloadConfig(String sharedLibPath, Map<String, String> envVariables) {
      String ldPreloadLibs = envVariables.get(LD_PRELOAD);
      String ldLibraryPath = envVariables.get(LD_LIBRARY_PATH);

      if (ldLibraryPath == null || ldPreloadLibs == null) return false;

      return ldPreloadLibs.contains(sharedLibPath) && ldLibraryPath.contains(FileUtil.removeFilePart(sharedLibPath));
   }

   @Override
   public void addPreloadConfig(String sharedLibPath, Map<String, String> envVariables) {
      addLibToLdPreload(sharedLibPath, envVariables);
      putLibInPathIfNecessary(FileUtil.removeFilePart(sharedLibPath), envVariables);
   }

   private void addLibToLdPreload(String sharedLibPath, Map<String, String> envVariables) {
      String ldPreloadLibs = envVariables.get(LD_PRELOAD);
      String updatedPreloadLibs = appendToList(ldPreloadLibs, sharedLibPath);
      envVariables.put(LD_PRELOAD, updatedPreloadLibs);
   }

   @Override
   public void removePreloadConfig(String sharedLibPath, Map<String, String> envVariables) {
      removeEntryFromEnv(sharedLibPath, LD_PRELOAD, envVariables);
   }
}
