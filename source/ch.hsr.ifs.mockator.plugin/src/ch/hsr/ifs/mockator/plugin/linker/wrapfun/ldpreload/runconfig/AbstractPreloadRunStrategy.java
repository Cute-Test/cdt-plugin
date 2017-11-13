package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.runconfig;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;


abstract class AbstractPreloadRunStrategy implements PreloadRunStrategy {

   protected String appendToList(final String list, final String newVal) {
      if (list == null || list.trim().isEmpty()) return newVal;

      if (list.contains(newVal)) return list;

      return String.format("%s:%s", list, newVal);
   }

   protected void removeEntryFromEnv(final String sharedLibPath, final String envName, final Map<String, String> env) {
      final Set<String> ldPreloadLibs = orderPreservingSet(env.get(envName).split(":"));
      ldPreloadLibs.remove(sharedLibPath);
      env.put(envName, ldPreloadLibs.stream().collect(Collectors.joining(":")));
   }
}
