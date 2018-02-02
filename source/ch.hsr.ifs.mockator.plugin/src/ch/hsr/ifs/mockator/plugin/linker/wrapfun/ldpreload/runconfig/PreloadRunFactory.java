package ch.hsr.ifs.mockator.plugin.linker.wrapfun.ldpreload.runconfig;

import java.util.Optional;

import org.eclipse.core.resources.IProject;

import ch.hsr.ifs.iltis.core.functional.OptionalUtil;

import ch.hsr.ifs.mockator.plugin.project.cdt.toolchains.ToolChain;


class PreloadRunFactory {

   public Optional<PreloadRunStrategy> getRunConfig(final IProject project) {
      return OptionalUtil.returnIfPresentElseEmpty(ToolChain.fromProject(project), (tc) -> {
         switch (tc) {
         case GnuLinux:
            return Optional.of(new LinuxPreloadRunConfig());
         case GnuMacOSX:
            return Optional.of(new MacOsXPreloadRunConfig());
         case GnuCygWin:
         case GnuMinGw:
         case ClangLinux:
         default:
            return Optional.of(null);
         }
      });
   }
}
