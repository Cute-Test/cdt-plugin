package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.array;
import static ch.hsr.ifs.mockator.plugin.base.collections.CollectionHelper.zipMap;
import static ch.hsr.ifs.mockator.plugin.base.util.PlatformUtil.PATH_SEGMENT_SEPARATOR;
import static ch.hsr.ifs.mockator.plugin.base.util.StringUtil.pythonFormat;

import org.eclipse.core.resources.IResource;

public class ProjRelPathGenerator {

  // Examples: ${workspace_loc:/${ProjName}/trace/mockator_getYear.h}
  // ${workspace_loc:/${ProjName}/mockator}
  public static <T extends IResource> String getProjectRelativePath(T folder) {
    String projRelPath = folder.getProjectRelativePath().toString();
    return pythonFormat("${workspace_loc:%(pathSep)s${ProjName}%(pathSep)s%(projRelPath)s}",
        zipMap(array("pathSep", "projRelPath"), array(PATH_SEGMENT_SEPARATOR, projRelPath)));
  }
}
