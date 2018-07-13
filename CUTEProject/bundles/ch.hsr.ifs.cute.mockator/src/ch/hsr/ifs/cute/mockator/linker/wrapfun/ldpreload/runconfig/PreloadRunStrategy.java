package ch.hsr.ifs.cute.mockator.linker.wrapfun.ldpreload.runconfig;

import java.util.Map;


interface PreloadRunStrategy {

   boolean hasPreloadConfig(String sharedLibPath, Map<String, String> envVars);

   void addPreloadConfig(String sharedLibPath, Map<String, String> envVars);

   void removePreloadConfig(String sharedLibPath, Map<String, String> envVars);
}
