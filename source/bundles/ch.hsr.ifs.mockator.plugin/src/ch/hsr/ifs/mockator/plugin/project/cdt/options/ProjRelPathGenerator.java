package ch.hsr.ifs.mockator.plugin.project.cdt.options;

import static ch.hsr.ifs.mockator.plugin.base.util.PlatformUtil.PATH_SEGMENT_SEPARATOR;

import org.eclipse.core.resources.IResource;


public class ProjRelPathGenerator {

   // Examples: ${workspace_loc:/${ProjName}/trace/mockator_getYear.h}
   // ${workspace_loc:/${ProjName}/mockator}
   public static <T extends IResource> String getProjectRelativePath(final T folder) {
      final String projRelPath = folder.getProjectRelativePath().toString();
      return String.format("${workspace_loc:%s${ProjName}%1$s%2$s}", PATH_SEGMENT_SEPARATOR, projRelPath);
   }
}
