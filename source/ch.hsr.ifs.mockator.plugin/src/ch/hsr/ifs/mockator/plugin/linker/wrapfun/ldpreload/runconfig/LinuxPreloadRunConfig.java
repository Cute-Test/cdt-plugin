package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.runconfig;

import java.util.Map;

import ch.hsr.ifs.iltis.core.resources.FileUtil;


class LinuxPreloadRunConfig extends AbstractPreloadRunStrategy {

   private static final String LD_PRELOAD      = "LD_PRELOAD";
   private static final String LD_LIBRARY_PATH = "LD_LIBRARY_PATH";

   private void putLibInPathIfNecessary(final String pathToShLib, final Map<String, String> envVariables) {
      final String ldLibraryPath = envVariables.get(LD_LIBRARY_PATH);
      final String path = FileUtil.getPathWithoutFilename(pathToShLib);
      envVariables.put(LD_LIBRARY_PATH, appendToList(ldLibraryPath, path));
   }

   @Override
   public boolean hasPreloadConfig(final String sharedLibPath, final Map<String, String> envVariables) {
      final String ldPreloadLibs = envVariables.get(LD_PRELOAD);
      final String ldLibraryPath = envVariables.get(LD_LIBRARY_PATH);

      if (ldLibraryPath == null || ldPreloadLibs == null) {
         return false;
      }

      return ldPreloadLibs.contains(sharedLibPath) && ldLibraryPath.contains(FileUtil.getPathWithoutFilename(sharedLibPath));
   }

   @Override
   public void addPreloadConfig(final String sharedLibPath, final Map<String, String> envVariables) {
      addLibToLdPreload(sharedLibPath, envVariables);
      putLibInPathIfNecessary(FileUtil.getPathWithoutFilename(sharedLibPath), envVariables);
   }

   private void addLibToLdPreload(final String sharedLibPath, final Map<String, String> envVariables) {
      final String ldPreloadLibs = envVariables.get(LD_PRELOAD);
      final String updatedPreloadLibs = appendToList(ldPreloadLibs, sharedLibPath);
      envVariables.put(LD_PRELOAD, updatedPreloadLibs);
   }

   @Override
   public void removePreloadConfig(final String sharedLibPath, final Map<String, String> envVariables) {
      removeEntryFromEnv(sharedLibPath, LD_PRELOAD, envVariables);
   }
}
