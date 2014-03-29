package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.runconfig;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.orderPreservingSet;

import java.util.Map;
import java.util.Set;

import ch.hsr.ifs.mockator.plugin.base.util.StringUtil;

abstract class AbstractPreloadRunStrategy implements PreloadRunStrategy {

  protected String appendToList(String list, String newVal) {
    if (list == null || list.trim().isEmpty())
      return newVal;

    if (list.contains(newVal))
      return list;

    return String.format("%s:%s", list, newVal);
  }

  protected void removeEntryFromEnv(String sharedLibPath, String envName, Map<String, String> env) {
    Set<String> ldPreloadLibs = orderPreservingSet(env.get(envName).split(":"));
    ldPreloadLibs.remove(sharedLibPath);
    env.put(envName, StringUtil.join(ldPreloadLibs, ":"));
  }
}
